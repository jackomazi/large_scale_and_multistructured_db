import requests
import re
from datetime import datetime
import json
from faker import Faker

class lichess_interface:
    @staticmethod
    def get_players_usernames(team : str) -> list:
        """ Fetches player usernames from a given Lichess team.
        url example: https://lichess.org/api/team/lichess-swis/users
        """
        url = f"https://lichess.org/api/team/{team}/users"
        headers = {"Accept": "application/x-ndjson"}
        print(f"Fetching team {team} from {url}")
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            print(f"Errore fetching team {team}: {response.status_code}")
            return []
        usernames = []
        for line in response.iter_lines():
            if line:
                user_data = line.decode('utf-8')
                match = re.search(r'"name":"(.*?)"', user_data)
                if match:
                    usernames.append(match.group(1))
        return usernames
    
    @staticmethod
    def get_player_infos(username: str) -> dict:
        """Fetches player information for a given Lichess username.
        url example: https://lichess.org/api/user/harshitsuperboy?profile=true
        """
        url = f"https://lichess.org/api/user/{username}?profile=true&rank=true"
        headers = {"Accept": "application/json"}
        response = requests.get(url,headers=headers)
        return response.json()

    @staticmethod
    def get_lichess_games(user : str, n: int) -> list:
        """Fetches Lichess games from a given URL in NDJSON format.
        url example: https://lichess.org/api/games/user/{username}?max=20&format=ndjson&opening=true&pgnInJson=true
        """
        url = f"https://lichess.org/api/games/user/{user}?max={n}&format=ndjson&opening=true&pgnInJson=true"
        headers = {"Accept": "application/x-ndjson"}
        games = []
        response = requests.get(url, headers=headers, stream =True)
        if response.status_code == 200:
            for line in response.iter_lines():
                if line:
                    game = json.loads(line)
                    games.append(game)
            return games
        else:
            return []
    
    @staticmethod
    def get_teams_list(page: int) -> list:
        """Fetches a list of Lichess teams from a specific page.
        url example: https://lichess.org/api/team/all?page=1
        """
        url = f"https://lichess.org/api/team/all?page={page}"
        print(f"Fetching teams from page {page} at {url}")
        headers = {"Accept": "application/json"}
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            print(f"Error fetching teams from page {page}: {response.status_code}")
            return []
        teams = []
        data = response.json()
        for team in data['currentPageResults']:
            teams.append(team)
        return teams

    @staticmethod
    def get_n_users_from_team(team: str, n: int) -> list:
        """Fetches up to n player usernames from a given Lichess team.
        url example: https://lichess.org/api/team/lichess-swis/users?full=false
        """
        url = f"https://lichess.org/api/team/{team}/users?full=false"
        print(f"Fetching up to {n} users from team {team} at {url}")
        headers = {"Accept": "application/x-ndjson"}
        response = requests.get(url, headers=headers, stream=True)
        if response.status_code != 200:
            print(f"Errore fetching team {team}: {response.status_code}")
            return []
        usernames = []
        for i, line in enumerate(response.iter_lines()):
            if i >= n:
                break
            if line:
                user = json.loads(line)
                usernames.append(user["name"])
        return usernames

    @staticmethod
    def format_lichess_game(game: dict) -> dict:
        """Formats a Lichess game dictionary into a structured format for MongoDB storage."""
        white = game.get("players", {}).get("white", {})
        black = game.get("players", {}).get("black", {})
        winner = game.get("winner")

        game_url = f"https://lichess.org/{game.get('id')}" if game.get('id') else None
        result_white = "win" if winner == "white" else "loss" if winner == "black" else "draw" 
        result_black = "win" if winner == "black" else "loss" if winner == "white" else "draw"

        if game.get("lastMoveAt"):
            end_time = datetime.fromtimestamp(game.get("lastMoveAt")/1000)
        else:
            end_time = None
        
        eco = game.get("opening", {}).get("eco") if game.get("opening") else None
        eco_url = f"https://www.365chess.com/eco/{eco}" if eco else None
        return {
            "_id": game_url,
            "url": game_url,
            "white_player": white.get("user", {}).get("name"),
            "black_player": black.get("user", {}).get("name"),
            "white_rating": white.get("rating"),
            "black_rating": black.get("rating"),
            "result_white": result_white,
            "result_black": result_black,
            "eco_url": eco_url,
            "opening": game.get("opening", {}).get("name") if game.get("opening") else None,
            "moves": game.get("moves"),
            "time_class": game.get("speed"),
            "rated": game.get("rated"),
            "end_time": end_time
        }
    
    @staticmethod
    def format_lichess_player_infos(user_info: dict) -> dict:
        # Useless data
        user_info.pop("id", None)
        user_info.pop("url", None)
        user_info.pop("count", None)
        user_info.pop("playTime", None)

        # Location fetching
        user_info["country"] = user_info.get("profile").get("flag")

        # Date modification
        date = user_info.get("seenAt") / 1000
        user_info["last_online"] =  datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')
        date = user_info.get("createdAt") / 1000
        user_info["joined"] = datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')

        # Adding empty array/object to comply with MongoDB data structure constraints
        user_info["games"] = []
        user_info["stats"] = {}

        # Modifiying "perf" value for Chess.com compability
        game_mods_in_common = ["bullet","blitz","rapid"]
        stats = {}
        for game_mod in game_mods_in_common:
            stat = user_info.get("perfs").get(game_mod)
            stat.pop("games", None)
            stat.pop("rd", None)
            stat.pop("prog", None)
            stat.pop("prov", None)
            stats[game_mod] = stat

        user_info["stats"] = stats

        # Adding some basic fake informations
        fake = Faker()
        # Missing name form Chess.com
        user_info["name"] = fake.name()
        # Fake data for 'login' pourposes
        # Adding fake mail
        user_info["mail"] = fake.email()
        # Adding fake password hashed
        user_info["password"] = fake.sha256()

        return user_info



