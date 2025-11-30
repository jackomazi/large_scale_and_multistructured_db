import requests
import re
from datetime import datetime
import json
class lichess_interface:

    @staticmethod
    # Scraping player usernames from teams
    # https://lichess.org/api/team/lichess-swis/users
    def get_players_usernames(team : str) -> list:
        """ Scrapes the usernames of all players in a given Lichess team. """
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
    # Scraping player infos
    def get_player_infos(username: str) -> dict:
        # Standard api call
        url = f"https://lichess.org/api/user/{username}?profile=true"
        headers = {"Accept": "application/json"}
        response = requests.get(url,headers=headers)
        return response.json()

    @staticmethod
    def get_lichess_games(url : str) -> list:
        headers = {"Accept": "application/x-ndjson"}
        games = []
        response = requests.get(url, headers=headers, stream =True)
        if response.status_code == 200:
            for line in response.iter_lines():
                if line:
                    game = json.loads(line) # transformo la riga in un dizionario
                    games.append(game)
            return games
        else:
            return []
        
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




