import requests
import re
from datetime import datetime, timedelta
import json
from faker import Faker
from dateutil.parser import isoparse
import time
import random
from requests.exceptions import ReadTimeout

COUNTRIES = [
    "AF","AX","AL","DZ","AS","AD","AO","AI","AQ","AG","AR","AM","AW","AU","AT","AZ",
    "BS","BH","BD","BB","BY","BE","BZ","BJ","BM","BT","BO","BQ","BA","BW","BV","BR",
    "IO","BN","BG","BF","BI","KH","CM","CA","CV","KY","CF","TD","CL","CN","CX","CC",
    "CO","KM","CG","CD","CK","CR","CI","HR","CU","CW","CY","CZ","DK","DJ","DM","DO",
    "EC","EG","SV","GQ","ER","EE","SZ","ET","FK","FO","FJ","FI","FR","GF","PF","TF",
    "GA","GM","GE","DE","GH","GI","GR","GL","GD","GP","GU","GT","GG","GN","GW","GY",
    "HT","HM","VA","HN","HK","HU","IS","IN","ID","IR","IQ","IE","IM","IL","IT","JM",
    "JP","JE","JO","KZ","KE","KI","KP","KR","KW","KG","LA","LV","LB","LS","LR","LY",
    "LI","LT","LU","MO","MG","MW","MY","MV","ML","MT","MH","MQ","MR","MU","YT","MX",
    "FM","MD","MC","MN","ME","MS","MA","MZ","MM","NA","NR","NP","NL","NC","NZ","NI",
    "NE","NG","NU","NF","MK","MP","NO","OM","PK","PW","PS","PA","PG","PY","PE","PH",
    "PN","PL","PT","PR","QA","RE","RO","RU","RW","BL","SH","KN","LC","MF","PM","VC",
    "WS","SM","ST","SA","SN","RS","SC","SL","SG","SX","SK","SI","SB","SO","ZA","GS",
    "SS","ES","LK","SD","SR","SJ","SE","CH","SY","TW","TJ","TZ","TH","TL","TG","TK",
    "TO","TT","TN","TR","TM","TC","TV","UG","UA","AE","GB","US","UM","UY","UZ","VU",
    "VE","VN","VG","VI","WF","EH","YE","ZM","ZW"
]

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
    def format_team_infos(team_info: dict) -> dict:
        """Formats a Lichess team information dictionary into a structured format for MongoDB storage."""
        team_info.pop("id", None)
        team_info.pop("open", None)
        team_info["number_members"] = team_info.pop("nbMembers", None)
        team_info.pop("leaders", None)
        team_info.pop("joined", None)
        team_info.pop("requested", None)
        team_info.pop("flair", None)
        team_info["leader"] = team_info["leader"]["name"] if team_info.get("leader") else None
        

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
                    "date": "date"
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
            "date": formatted_game.get("end_time").strftime('%Y-%m-%d %H:%M:%S') if formatted_game.get("end_time") else None
        }
    
    @staticmethod
    def format_lichess_player_infos(user_info: dict) -> dict:
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
            user_info["country"] = random.choice(COUNTRIES)

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

        # remove other unnecessary fields
        user_info.pop("profile", None)
        user_info.pop("perfs", None)
        user_info.pop("flair", None)
        user_info.pop("seenAt", None)
        user_info.pop("createdAt", None)

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

    #@staticmethod
    #def format_lichess_player_tournament(tournament: dict) -> dict:
    #    """Formats a Lichess tournament dictionary into a structured format for MongoDB storage."""
    #    tournament["id"] = tournament["tournament"]["id"]
    #    tournament["name"] = tournament["tournament"]["fullName"]
    #    tournament["max_players"] = tournament["tournament"]["nbPlayers"]
    #    tournament["started_at"] = datetime.fromtimestamp(tournament["tournament"]["startsAt"]/1000)
    #    tournament["finished_at"] = datetime.fromtimestamp(tournament["tournament"]["finishesAt"]/1000) if tournament["tournament"].get("finishesAt") else None
    #    tournament["creator"] = tournament["tournament"]["createdBy"]
    #    return tournament
    
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
    def get_lichess_tournament_infos_with_players(tournament_id: str) -> dict:
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
    def get_lichess_tournament_games_all(tournament_id: str, total_games: int) -> list:
        """
        Fetch all games from a Lichess tournament safely.
        Stops automatically after total_games or max_games, never blocks.
        """

        url = f"https://lichess.org/api/tournament/{tournament_id}/games"
        headers = {
            "Accept": "application/x-ndjson",
            "User-Agent": "Lichess Data Collector - academic"
        }
        max_games = None  # You can set a maximum number of games to fetch if desired
        limit = total_games if max_games is None else min(total_games, max_games)
        games = []

        try:
            response = requests.get(url, headers=headers, stream=True, timeout=(5, None))
            response.raise_for_status()

            buffer = ""
            last_data_time = time.time()
            INACTIVITY_TIMEOUT = 3  # stop if no data for 3 sec

            while True:
                # leggi chunk dal socket
                chunk = response.raw.read(1024)
                if not chunk:
                    # niente dati → controllo timeout
                    if time.time() - last_data_time > INACTIVITY_TIMEOUT:
                        break
                    else:
                        time.sleep(0.1)
                        continue

                last_data_time = time.time()
                buffer += chunk.decode("utf-8")

                while "\n" in buffer:
                    line, buffer = buffer.split("\n", 1)
                    if not line.strip():
                        continue
                    game = json.loads(line)
                    games.append(game)

                    if len(games) >= limit:
                        break

                if len(games) >= limit:
                    break

        finally:
            response.close()  # chiude la connessione

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
        tournament_info["started_at"] = starts_at
        if tournament_info.get("minutes"):
            tournament_info["finished_at"] = starts_at + timedelta(minutes=tournament_info["minutes"])
        else:
            tournament_info["finished_at"] = None
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
        tournament_info["max_rating"] = max_rating.get("rating") if isinstance(max_rating, dict) else None

        # remove unnecessary fields
        tournament_info.pop("minRatedGames", None)

        tournament_info.pop("duels", None)


        ## da valutare
        tournament_info.pop("berserkable", None)

        tournament_info.pop("rated", None)

        tournament_info.pop("stats", None)

        tournament_info.pop("verdicts", None)

        tournament_info.pop("pairingsClosed", None)

        # for the app purposes, if the creator is lichess, we set it to admin
        if tournament_info["creator"] == "lichess":
            tournament_info["creator"] = "admin"

        return tournament_info
    
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

        





