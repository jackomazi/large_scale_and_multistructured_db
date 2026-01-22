import requests
import re
from datetime import datetime, timedelta
import json
from faker import Faker
from dateutil.parser import isoparse
import time
import random
from requests.exceptions import ReadTimeout
from typing import List, Dict
import math



class lichess_interface:
    @staticmethod
    # Scraping player usernames from teams
    def get_players_usernames(team : str) -> list:
        """ Fetches player usernames from a given Lichess team.
        url example: https://lichess.org/api/team/lichess-swis/users
        """
        # Endpoint URL
        url = f"https://lichess.org/api/team/{team}/users"
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers)
        # Handle non-200 responses
        if response.status_code != 200:
            return []
        usernames = []
        # Parse NDJSON response
        for line in response.iter_lines():
            if line:
                user = json.loads(line)
                usernames.append(user["name"]) # extract username
        return usernames
    
    @staticmethod
    def get_players_ids(team : str) -> list:
        """ Fetches player IDs from a given Lichess team.
        url example: https://lichess.org/api/team/lichess-swis/users
        """
        # Endpoint URL
        url = f"https://lichess.org/api/team/{team}/users"
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers)
        # Handle non-200 responses
        if response.status_code != 200:
            return []
        ids = []
        # Parse NDJSON response
        for line in response.iter_lines():
            if line:
                user = json.loads(line)
                ids.append(user["id"]) # extract id
        return ids


    @staticmethod
    def get_player_infos(username: str) -> dict:
        """Fetches player information for a given Lichess username.
        url example: https://lichess.org/api/user/harshitsuperboy?profile=true
        """
        url = f"https://lichess.org/api/user/{username}?profile=true&rank=true"
        
        headers = {"Accept": "application/json",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url,headers=headers)
        
        if response.status_code == 429:
            print("\nRate limit exceeded.")
            return {}
        elif response.status_code != 200:
            return {}
        return response.json()

    @staticmethod
    def get_lichess_games(user : str, n: int) -> list:
        """Fetches Lichess games from a given URL in NDJSON format.
        url example: https://lichess.org/api/games/user/harshitsuperboy?max=1&format=ndjson&opening=true&pgnInJson=true
        """
        url = f"https://lichess.org/api/games/user/{user}?max={n}&format=ndjson&opening=true&pgnInJson=true"
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        games = []
        response = requests.get(url, headers=headers, stream =True)
        if response.status_code == 200:
            # Parse NDJSON response
            for line in response.iter_lines():
                # For each line in the response, parse it as JSON
                if line:
                    game = json.loads(line)
                    # Append the parsed game to the list
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
        headers = {"Accept": "application/json",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            return []
        teams = []
        data = response.json()
        # Extract team information
        for team in data['currentPageResults']:
            teams.append(team)
        return teams

    @staticmethod
    def get_team_infos(team: str) -> dict:
        """Fetches Lichess team information for a given team ID.
        url example: https://lichess.org/api/team/lichess-swis
        """
        url = f"https://lichess.org/api/team/{team}"
        headers = {"Accept": "application/json",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            return {}
        return response.json()
    
    @staticmethod
    def format_team_infos(team_info: dict, countries: list) -> dict:
        """Formats a Lichess team information dictionary into a structured format for MongoDB storage."""
        team_info.pop("id", None)
        team_info.pop("open", None)
        team_info.pop("nbMembers", None)
        team_info.pop("leaders", None)
        team_info.pop("joined", None)
        team_info.pop("requested", None)
        team_info.pop("flair", None)
        team_info["admin"] = team_info["leader"]["name"] if team_info.get("leader") else None
        team_info["country"] = random.choice(countries)
        if team_info["admin"] == "Lichess":
            team_info["admin"] = "admin"
        team_info.pop("leader", None)
        
        
        start_date = datetime(2006, 1, 1)
        end_date = datetime(2010, 12, 31, 23, 59, 59)
        
        delta_seconds = int((end_date - start_date).total_seconds())
        random_seconds = random.randint(0, delta_seconds)
        random_date = start_date + timedelta(seconds=random_seconds)
        
        team_info["creation_date"] = random_date.strftime('%Y-%m-%d %H:%M:%S')
        
        return team_info

    @staticmethod
    def get_n_users_from_team(team: str, n: int) -> list:
        """Fetches up to n player usernames from a given Lichess team.
        url example: https://lichess.org/api/team/lichess-swis/users
        """
        url = f"https://lichess.org/api/team/{team}/users"
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers, stream=True)
        if response.status_code != 200:
            return []
        usernames = []
        for i, line in enumerate(response.iter_lines()):
            if i >= n: # if reached n users, exit loop
                break
            if line:
                user = json.loads(line)
                usernames.append(user["name"]) # extract username
        return usernames
    
    @staticmethod
    def get_n_ids_from_team(team: str, n: int) -> list:
        """Fetches up to n player IDs from a given Lichess team.
        url example: https://lichess.org/api/team/lichess-swis/users
        """
        url = f"https://lichess.org/api/team/{team}/users"
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers, stream=True)
        if response.status_code != 200:
            return []
        ids = []
        for i, line in enumerate(response.iter_lines()):
            if i >= n: # if reached n users, exit loop
                break
            if line:
                user = json.loads(line)
                ids.append(user["id"]) # extract id
        return ids

    @staticmethod
    def format_lichess_game(game: dict, openings: list) -> dict:
        """Formats a Lichess game dictionary into a structured format for MongoDB storage."""
        white = game.get("players", {}).get("white", {})
        black = game.get("players", {}).get("black", {})
        winner = game.get("winner")

        game_url = f"https://lichess.org/{game.get('id')}" if game.get('id') else None
        result_white = "win" if winner == "white" else "loss" if winner == "black" else "draw" 
        result_black = "win" if winner == "black" else "loss" if winner == "white" else "draw"

        if game.get("lastMoveAt"):
            time = datetime.fromtimestamp(game.get("lastMoveAt")/1000)
            end_time = time.strftime("%Y-%m-%d %H:%M:%S")
        else:
            end_time = None
        
        opening = game.get("opening")

        if isinstance(opening, dict):
            eco = opening.get("eco")
            opening_name = opening.get("name")
        else:
            opening_name = random.choice(openings)

        return {
            "_id": game_url,
            "white_player": white.get("user", {}).get("name"),
            "black_player": black.get("user", {}).get("name"),
            "white_rating": (
                math.floor(white.get("rating") * 0.75)
                if white.get("rating") is not None
                else None
            ),
            "black_rating": (
                math.floor(black.get("rating") * 0.75)
                if black.get("rating") is not None
                else None
            ),
            "result_white": result_white,
            "result_black": result_black,
            "opening": opening_name,
            "moves": game.get("moves"),
            "time_class": game.get("speed"),
            "end_time": end_time
        }
    
    @staticmethod
    def format_lichess_game_essentials(game_mongo_id: str, formatted_game: dict, is_blank: bool) -> dict:
        """Formats essential information of a Lichess game for user document storage."""
        if is_blank:
            return {"_id": None,
                    "white": "name",
                    "black": "name",
                    "opening": "name",
                    "winner": "name",
                    "date": "2026-01-22 15:50:41"
                    }
        # if result_white is win then winner is white, else if result_black is win then winner is black, else draw
        if formatted_game.get("result_white") == "win":
            winner = formatted_game.get("white_player")
        elif formatted_game.get("result_black") == "win":
            winner = formatted_game.get("black_player")
        else:
            winner = "draw"
        return {
            "_id": game_mongo_id,
            "white": formatted_game.get("white_player"),
            "black": formatted_game.get("black_player"),
            "opening": formatted_game.get("opening"),
            "winner": winner,
            "date": formatted_game.get("end_time")
        }
    
    @staticmethod
    def format_lichess_player_infos(user_info: dict, countries: list) -> dict:
        if user_info.get("disabled") is True:
            return None
        # Useless data
        user_info.pop("id", None)
        user_info.pop("url", None)
        user_info.pop("count", None)
        user_info.pop("playTime", None)

        # remove playing if present
        user_info.pop("playing", None)

        try:
            # Location fetching
            user_info["country"] = user_info.get("profile").get("flag")
        except:
            user_info["country"] = random.choice(countries)

        # Date modification for MongoDB storage 
                    
        date = user_info.get("seenAt") / 1000
        user_info["last_online"] =  datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')
        date = user_info.get("createdAt") / 1000
        user_info["joined"] = datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')

        # Adding empty array/object to comply with MongoDB data structure constraints
        user_info["stats"] = {}

        # Modifiying "perf" value for Chess.com compability
        game_mods_in_common = ["bullet","blitz","rapid"]
        stats = {}
        for game_mod in game_mods_in_common:
            stat = user_info.get("perfs").get(game_mod)["rating"]
            stats[game_mod] = math.floor(stat*0.75)

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

        # remove other unnecessary fields
        user_info.pop("profile", None)
        user_info.pop("perfs", None)
        user_info.pop("flair", None)
        user_info.pop("seenAt", None)
        user_info.pop("createdAt", None)

        user_info["admin"] = "false"

        return user_info
    
    @staticmethod
    def format_lichess_player_essentials(id: str, user:dict) -> dict:
        """Formats essential information of a Lichess player for user document storage."""
        return {
            "country": user.get("country", ""),
            # here I have to format like chess.com so the stats field will be stats: {bullet: rating, blitz: rating, rapid: rating}
            "stats": {
                "bullet": user.get("stats", {}).get("bullet", {}),
                "blitz": user.get("stats", {}).get("blitz", {}),
                "rapid": user.get("stats", {}).get("rapid", {})
            }
        }
        

    @staticmethod
    # Scraping user tournaments
    def get_n_lichess_player_tournaments(username: str, n: int) -> list:
        """ Fetches n player tournaments from a given Lichess username.
        url example: https://lichess.org/api/user/{username}/tournament/played
        url example: https://lichess.org/api/user/senukRandidu/tournament/played?nb=1
        """
        url = f"https://lichess.org/api/user/{username}/tournament/played?nb={n}"
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            return []
        tournaments = []
        for line in response.text.splitlines():
            try:
                tournaments.append(json.loads(line))
            except json.JSONDecodeError:
                continue
        return tournaments

    @staticmethod
    def get_lichess_tournament_infos(tournament_id: str) -> dict:
        """Fetches Lichess tournament information for a given tournament ID.
        url example: https://lichess.org/api/tournament/{tournament_id}
        """
        url = f"https://lichess.org/api/tournament/{tournament_id}"
        headers = {"Accept": "application/json",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        response = requests.get(url, headers=headers)
        if response.status_code != 200:
            return {}
        return response.json()

    @staticmethod
    def get_lichess_tournament_infos_with_players(tournament_id: str, max_players: int) -> dict:
        """Fetches Lichess tournament information for a given tournament ID.
        url example: https://lichess.org/api/tournament/{tournament_id}
        """
        url = f"https://lichess.org/api/tournament/{tournament_id}"
        headers = {"Accept": "application/json",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        page = 1
        all_participants = []
        seen_ids = set()
        last_page_ids = None
        tournament = None
        while True:
            response = requests.get(
                url,
                headers=headers,
                params={"page": page}
            )
            if response.status_code != 200:
                break
            data = response.json()
            # salva i metadata solo una volta
            if tournament is None:
                tournament = {k: v for k, v in data.items() if k != "standing"}
            players_page = data.get("standing", {}).get("players", [])
            if not players_page:
                break
            current_ids = [p.get("name") for p in players_page]
            if current_ids == last_page_ids:
                break

            for p in players_page:
                pid = p.get("name")
                if pid and pid not in seen_ids:
                    seen_ids.add(pid)
                    all_participants.append(p)
                    if max_players is not None and len(all_participants) >= max_players:
                        break
            if max_players is not None and len(all_participants) >= max_players:
                break

            last_page_ids = current_ids
            page += 1
            time.sleep(0.3)  # rate limit
        if tournament is None:
            return {}

        tournament["players"] = all_participants

        return tournament
    

    @staticmethod
    def get_n_participants_in_tournament(tournament_info: dict) -> list:
        """Returns the list of participants in a tournament given its info dictionary."""
        players = [player['name'] for player in tournament_info['standing']['players']]

        return players

    @staticmethod
    def get_lichess_tournament_games(tournament_id: str, username: str) -> list:
        """Fetches Lichess tournament games for a given tournament ID in NDJSON format.
        url example: https://lichess.org/api/tournament/{tournament_id}/games
        """
        url = f"https://lichess.org/api/tournament/{tournament_id}/games?player={username}&opening=true"
        
        headers = {"Accept": "application/x-ndjson",
                   "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"}
        games = []
        response = requests.get(url, headers=headers, stream =True)
        if response.status_code == 200:
            # Parse NDJSON response
            for line in response.iter_lines():
                # For each line in the response, parse it as JSON
                if line:
                    game = json.loads(line)
                    # Append the parsed game to the list
                    games.append(game)
            return games
        else:
            return []

    

    @staticmethod
    def get_lichess_tournament_games_all(
        tournament_id: str,
        total_games: int | None = None
    ) -> List[Dict]:
        """
        Fetch all games from a Lichess Arena tournament using NDJSON streaming.

        :param tournament_id: Lichess tournament ID
        :param total_games: Optional hard limit on number of games to fetch
        :param oauth_token: Optional OAuth token (increases rate limit)
        :return: List of game dicts
        """

        url = f"https://lichess.org/api/tournament/{tournament_id}/games"

        oauth_token = "Nooo" # Ask Andrea for token, soon it will be added an env file with it
        
        headers = {
            "Accept": "application/x-ndjson",
            "User-Agent": "Lichess Data Collector - academic purposes"
        }

        if oauth_token:
            headers["Authorization"] = f"Bearer {oauth_token}"

        params = {
            "player": "",
            "moves": "true",
            "pgnInJson": "false",
            "tags": "true",
            "clocks": "false",
            "evals": "false",
            "accuracy": "false",
            "opening": "true",
            "division": "false"
        }

        games: List[Dict] = []

        with requests.get(url, headers=headers, params=params, stream=True, timeout=60) as response:
            response.raise_for_status()

            for line in response.iter_lines():
                if not line:
                    continue

                try:
                    game = json.loads(line.decode("utf-8"))
                    games.append(game)
                except json.JSONDecodeError:
                    continue

                if total_games is not None and len(games) >= total_games:
                    return games

        return games

    @staticmethod
    def format_lichess_tournament_info(tournament_info: dict) -> dict:
        """Formats a Lichess tournament information dictionary into a structured format for MongoDB storage."""
        # rename fields
        tournament_info["number_partecipants"] = tournament_info.pop("nbPlayers")
        tournament_info["creator"] = tournament_info.pop("createdBy")
        tournament_info["name"] = tournament_info.pop("fullName")

        # finished at, calculated from startsAt + field minutes
        try:
            starts_at = isoparse(tournament_info["startsAt"])
        except Exception as e:
            starts_at = None
        # tournament_info["started_at"] = starts_at
        if tournament_info.get("minutes"):
            finish_obj = starts_at + timedelta(minutes=tournament_info["minutes"])
            tournament_info["finish_time"] = finish_obj.strftime("%Y-%m-%d %H:%M:%S")
        else:
            tournament_info["finish_time"] = None
        tournament_info.pop("startsAt")
        tournament_info.pop("minutes", None)

        # time control and clock limit
        tournament_info["time_control"] = tournament_info["perf"]["name"]
        tournament_info.pop("perf")

        tournament_info["clock_limit"] = tournament_info["clock"]["limit"]
        tournament_info["clock_increment"] = tournament_info["clock"]["increment"]
        tournament_info.pop("clock")

        # chess variant, possible values: standard, chess960 (Fischer Random)
        tournament_info["chess_variant"] = tournament_info.pop("variant")

        # max rating
        max_rating = tournament_info.pop("maxRating", None)
        tournament_info["max_rating"] = math.floor(max_rating.get("rating")*0.75) if isinstance(max_rating, dict) else None

        min_rating = tournament_info.pop("minRating", None)
        tournament_info["min_rating"] = math.floor(min_rating.get("rating")*0.75) if isinstance(min_rating, dict) else None

        # remove unnecessary fields
        tournament_info.pop("minRatedGames", None)

        tournament_info.pop("duels", None)


        ## da valutare
        tournament_info.pop("berserkable", None)

        tournament_info.pop("rated", None)

        tournament_info["total_games"] = tournament_info.get("stats", {}).get("games", 0)
        tournament_info["whiteWins"] = tournament_info.get("stats", {}).get("whiteWins", 0)
        tournament_info["blackWins"] = tournament_info.get("stats", {}).get("blackWins", 0)
        tournament_info.pop("stats", None)

        tournament_info.pop("verdicts", None)

        tournament_info.pop("pairingsClosed", None)

        tournament_info.pop("isRecentlyFinished", None)

        tournament_info.pop("id", None)

        tournament_info.pop("schedule", None)

        tournament_info.pop("teamMember", None)
        
        tournament_info.pop("teamStanding", None)

        tournament_info.pop("teamBattle", None)

        tournament_info.pop("position", None)

        tournament_info.pop("noStreak", None)

        tournament_info.pop("podium", None)

        # for the app purposes, if the creator is lichess, we set it to admin
        if tournament_info["creator"] == "lichess":
            tournament_info["creator"] = "admin"

        if tournament_info["isFinished"] == "true":
            tournament_info["status"] = "finished"
        else:
            tournament_info["status"] = "not finished"
        tournament_info.pop("isFinished", None)

        tournament_info.pop("system")

        tournament_info["description"] = tournament_info.get("description") or ""


        return tournament_info
    
    @staticmethod
    def estimate_player_stats(total_games: int, total_players: int, white_wins: int, black_wins: int, placement: int) -> tuple:
        """
        Stima le statistiche di un singolo giocatore basandosi sui dati globali del torneo.
        """
        if total_players <= 0:
            return {"played": 0, "wins": 0, "draws": 0, "losses": 0}

        # 1. Calcolo della media partite per giocatore (ogni partita ha 2 giocatori)
        avg_games_per_player = (total_games * 2) / total_players

        # 2. Stima partite giocate (chi sta in alto in classifica di solito ha giocato di più)
        # Se placement è piccolo (es. 1, 2, 3), il moltiplicatore è più alto
        if placement <= 10:
            play_factor = random.uniform(1.2, 1.6)
        elif placement <= 50:
            play_factor = random.uniform(0.9, 1.3)
        else:
            play_factor = random.uniform(0.4, 1.0)
        
        played = max(1, int(avg_games_per_player * play_factor))

        # 3. Calcolo Win Rate basato sul placement
        # Più il placement è basso (vicino a 1), più la win_rate è alta
        # Una formula semplice: i top player hanno 70-90% win rate, gli ultimi 10-20%
        base_win_rate = (white_wins + black_wins) / (total_games * 2) if total_games > 0 else 0.45
        
        # Modificatore basato sulla posizione (più sei in alto, più vinci rispetto alla media)
        if placement <= 10:
            win_rate = random.uniform(0.70, 0.85)
        elif placement <= 100:
            win_rate = random.uniform(0.45, 0.65)
        else:
            win_rate = random.uniform(0.15, 0.40)

        # 4. Generazione numeri finali
        wins = int(played * win_rate)
        
        # Le patte (draws) sono meno comuni nei tornei online, diciamo tra 2% e 8%
        draw_rate = random.uniform(0.02, 0.08)
        draws = int(played * draw_rate)
        
        # Il resto sono sconfitte
        losses = max(0, played - wins - draws)

        return wins, losses, draws


    @staticmethod
    def format_lichess_tournament_essentials(tournament_id: str, tournament_info: dict) -> dict:
        """Formats essential information of a Lichess tournament for user document storage."""
        return {
            "_id": tournament_id,
            "placement": tournament_info["player"]["rank"]
            }

    @staticmethod
    def get_lichess_player_infos_by_list_ids(user_ids: list) -> list:
        """Uses POST endpoint to fetch multiple user infos by list of IDs.
        url example: curl -k 'https://lichess.org/api/users?profile=true' \
                                -H 'Authorization: Bearer lip_xxxxxxxxxxxxx' \
                                -H 'Content-Type: text/plain' \
                                --data 'thibault,maia1'
        """
        url = "https://lichess.org/api/users?profile=true&rank=true"
        headers = {
            "Accept": "application/json",
            "Content-Type": "text/plain",
            "User-Agent": "Lichess Data Collector - for academic purposes - contact: andreagiacomazzi202@gmail.com"
        }
        data = ",".join(user_ids)
        response = requests.post(url, headers=headers, data=data)
        if response.status_code != 200:
            return []
        return response.json()

        





