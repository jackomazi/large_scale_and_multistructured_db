import re
from datetime import datetime
from faker import Faker
import chess.pgn
import os
import json
import random

import requests

class chess_com_interface:
    @staticmethod
    # Scaping player usernames from clubs
    def get_players_usernames(club: str) -> list:
        # Standard api call
        url = f"https://api.chess.com/pub/club/{club}/members"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)

        # Return all players user names
        usernames = []
        for element in response.json().get("all_time"):
            usernames.append(element.get("username"))
        return usernames

    @staticmethod
    def get_club_info(club: str) -> dict:
        # Standard api call
        url = f"https://api.chess.com/pub/club/{club}"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)

        return chess_com_interface.format_club_info(response.json())
    
    @staticmethod
    def get_list_of_working_club_names_for_nation(nation: str, number: int) -> []:
        # Standard api call
        url = f"https://api.chess.com/pub/country/{nation}/clubs"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)
        clubs_urls = response.json().get("clubs")

        # Getting firts n working clubs
        working_clubs = []
        i = 0

        if clubs_urls is None:
            return []
        
        clubs_urls = clubs_urls[::-1]

        for club in clubs_urls:
            response = requests.get(f"{club}/members", headers=headers)
            if response.status_code == 200:
                i += 1
                working_clubs.append(club.split("/")[-1])
            if i > number:
                break

        return working_clubs


    @staticmethod
    def format_club_info(club: dict) -> dict:
        formatted_club = {}
        formatted_club["name"] = club.get("name")
        formatted_club["description"] = club.get("description")
        formatted_club["country"] = club.get("country").split("/")[-1]
        date = club.get("created")
        formatted_club["creation_date"] = datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')
        formatted_club["admin"] = club.get("admin")[0].split("/")[-1]
        return formatted_club

    @staticmethod
    # Scraping player infos
    def get_player_infos(username: str) -> dict:
        # Standard api call
        url = f"https://api.chess.com/pub/player/{username}"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }

        response = requests.get(url, headers=headers)
        reformatted = chess_com_interface.format_player_info(response.json())
        return reformatted
    
    @staticmethod
    # Shaving off useless or incopatible data with Lichess
    def format_player_info(user_info: dict) -> dict:
        # Useless data
        if "@id" in user_info:
            user_info.pop("@id")
        try:
            user_info.pop("url")
            user_info.pop("avatar")
            user_info.pop("player_id")
            user_info.pop("location")
            user_info.pop("status")
        except:
            pass
        # Location modification
        location_url = user_info.get("country")
        user_info["country"] = location_url.split("/")[-1]
        # Date modification
        date = user_info.get("last_online")
        user_info["last_online"] =  datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')
        date = user_info.get("joined")
        user_info["joined"] = datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')
        # Adding empty array/object to comply with MongoDB data structure constraints
        user_info["games"] = []
        user_info["stats"] = {}
        user_info["buffered_games"] = 0

        # Adding some basic fake informations
        fake = Faker()
        # Fake data for 'login' pourposes
        # Adding fake mail
        user_info["mail"] = fake.email()
        # Adding fake password hashed
        user_info["password"] = fake.sha256()

        return user_info


    @staticmethod
    # Scraping player games archives
    def get_player_games_archives(username: str) -> list:
        # Standard api call
        url = f"https://api.chess.com/pub/player/{username}/games/archives"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)

        return response.json().get("archives")

    @staticmethod
    # Scraping games from archive given an url of said archive
    def get_chess_com_games(url: str) -> list:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            games = response.json().get("games", [])
            return games
        else:
            return []

    @staticmethod
    # Scraping user finished tournaments
    def get_chess_com_player_tournaments(username: str) -> list:
         # Standard api call
        url = f"https://api.chess.com/pub/player/{username}/tournaments"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)
        return response.json().get("finished")
    
    @staticmethod
    # Fetches tournament infos
    def get_chess_com_tournament(tournament_url: str) -> dict:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }

        response = requests.get(tournament_url, headers = headers)
        if response.status_code == 200:
            return chess_com_interface.format_chess_com_tournament(response.json())
        else:
            return {"Error":"Error"}

    @staticmethod
    # Formatting tournament page
    def format_chess_com_tournament(tournament: dict) -> dict:
        try:
            tournament.pop("url")
        except:
            pass
        date = tournament.get("finish_time")
        if date is not None:
            tournament["finish_time"] = datetime.fromtimestamp(date).strftime('%Y-%m-%d %H:%M:%S')
        tournament.pop("rounds")
        tournament.pop("players")
        tournament["games"] = []
        # Formatting tournament settings
        tournament["max_rating"] = tournament.get("settings").get("max_rating")
        tournament["min_rating"] = 600
        tournament["max_partecipants"] = random.uniform(10,20)
        tournament["time_control"] = tournament.get("settings").get("time_control")
        return tournament

    @staticmethod
    # Tournament games data structure appears different from games taken from user archives
    # Need new way to extract opening
    def fetch_tournament_game_opening(pgn: str, opening_recognizer: object) -> str:
        match = re.search(r'\[ECOUrl "https://www\.chess\.com/openings/([^"]+)"\]', pgn)
    
        if match:
            # Prende la parte finale dell'URL (gruppo 1)
            raw_name = match.group(1)
        
            # Sostituisce i trattini con spazi per ottenere il nome pulito
            return raw_name.replace("-", " ")
        
        opening_name = opening_recognizer.get_opening(pgn)
        
        return opening_name

    @staticmethod
    # Formatting short description of tournament infos relating to user
    def format_chess_com_tournament_essentials(id: str, tournament: dict, default: bool) -> dict:
        if default:
            return {"_id": None,
                    "name": "No-name",
                    "wins": 0,
                    "losses": 0,
                    "draws": 0,
                    "points_awarded": 0,
                    "placement": 0,
                    "status": "Non-awarded"
                    }
        tournament["_id"] = id
        tournament.pop("url")
        tournament.pop("@id")
        tournament.pop("total_players")
        return tournament

    @staticmethod
    # Fetching all games from tournaments
    def get_games_from_tournament(tournament_url: str) -> list:
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }

        response = requests.get(tournament_url, headers = headers)
        response_dict = response.json()

        # All games array
        all_games = []

        # Rounds of games
        round_urls = response_dict.get("rounds")

        for url in round_urls:
            response = requests.get(url, headers= headers)
            response = response.json()
            group_urls = response.get("groups")
            for url in group_urls:
                response = requests.get(url, headers= headers)
                response = response.json()
                games = response.get("games")
                if games is None:
                    continue
                for game in games:
                    all_games.append(game)
        
        return all_games

    @staticmethod
    # Formatting farched games dict into the desired form
    # Note: Does modify certains values and name fields
    def format_chess_com_game(game: dict) -> dict:
        return {
            "white_player": game.get("white", {}).get("username"),
            "black_player": game.get("black", {}).get("username"),
            "white_rating": game.get("white", {}).get("rating"),
            "black_rating": game.get("black", {}).get("rating"),
            "result_white": game.get("white", {}).get("result"),
            "result_black": game.get("black", {}).get("result"),
            "opening": game.get("eco", "").split("/")[-1] if game.get("eco") else None,
            "moves": chess_com_interface.extract_moves_from_pgn(game.get("pgn", "")),
            "time_class": game.get("time_class"),
            "rated": game.get("rated"),
            "end_time": game.get("end_time"),
        }
    
    @staticmethod
    # Creates short descriptive copy of game information
    # Used for partial embedding in document db
    def format_chess_com_game_essentials(id: str ,game: dict, default: bool) -> dict:
        if default:
            return {"_id": None,
                    "white": "name",
                    "black": "name",
                    "opening": "name",
                    "winner": "name",
                    "date": "date"
                    }
        return {"_id": id,
                "white": game.get("white_player"),
                "black": game.get("black_player"),
                "opening": game.get("opening"),
                "winner": game.get("white_player") if game.get("white_result") == game.get("white_player") else game.get("black_player"),
                "date":  datetime.fromtimestamp(game.get("end_time")).strftime('%Y-%m-%d %H:%M:%S')
        }
    
    @staticmethod
    # Creates short descriptive copy of user information
    # Used for partial embedding in document db
    def format_chess_com_player_essentials(id: str, user: dict, default: bool) -> dict:
        if default:
            return {"_id": None,
                    "name": "name",
                    "stats": [],
                    "country": "country"
            }
        return {"_id": id,
                "name": user.get("username"),
                "stats": user.get("stats"),
                "country": user.get("country")
            }

    @staticmethod
    # Scraping player infos
    def get_player_games_stats(username: str) -> dict:
        # Standard api call
        url = f"https://api.chess.com/pub/player/{username}/stats"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)

        return chess_com_interface.format_chess_com_player_stats(response.json())
    
    @staticmethod
    # Formats player stats
    def format_chess_com_player_stats(stats: dict) -> dict:
        #Chess games types (with direct equivalent of Lichess)
        types = ["chess_bullet","chess_blitz","chess_rapid"]

        new_dict = {}
        # Name changes and best score memorization for lichess compatibility 
        for type in types:
            type_name = type.split("_")[1]
            # Not all categories have the 'best' attribute...
            if stats.get(type) is None:
                continue
            try:
                new_dict[type_name] = stats.get(type).get("best").get("rating")
            except:
                new_dict[type_name] = stats.get(type).get("last").get("rating")
            
        return new_dict

    @staticmethod
    # Support method for 'format_chess_com_games'
    def extract_moves_from_pgn(pgn: str) -> str:
        lines = pgn.splitlines()
        moves = []
        for line in lines:
            # salta tutte le righe che iniziano con [
            if not line.startswith("["):
                moves.append(line.strip())
        moves_str = " ".join(moves)
        # rimuove i clock {[%clk ...]} e altri commenti
        moves_str = re.sub(r"\{.*?\}", "", moves_str)
        # rimuove numeri di mossa come "1." o "1..."
        moves_str = re.sub(r"\d+\.+", "", moves_str)
        # normalizza spazi
        moves_str = " ".join(moves_str.split())
        return moves_str

    @staticmethod
    def get_country_players(country_code: str) -> list:
        # Fetches a list of player usernames for a given country code
        url = f"https://api.chess.com/pub/country/{country_code}/players"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)
        return response.json().get("players", [])


if __name__ == "__main__":
    filename = "../config.json"
    if os.path.exists(filename):
        with open(filename, 'r', encoding='utf-8') as f:
            try:
                data = json.load(f)
            except json.JSONDecodeError:
                print("Il file è vuoto o corrotto. Ne verrà creato uno nuovo.")
                data = {}

    working_clubs = []
    print("fetching italian clubs")
    working_clubs += chess_com_interface.get_list_of_working_club_names_for_nation("IT",10)
    print("fetching french clubs")
    working_clubs += chess_com_interface.get_list_of_working_club_names_for_nation("FR",10)
    print("fetching us clubs")
    working_clubs += chess_com_interface.get_list_of_working_club_names_for_nation("US",10)
    print("fetching indian clubs")
    working_clubs += chess_com_interface.get_list_of_working_club_names_for_nation("IN",10)
    print("fetching german clubs")
    working_clubs += chess_com_interface.get_list_of_working_club_names_for_nation("DE",10)
    data["clubs"] = working_clubs

    # 3. Scrivi il file aggiornato
    with open(filename, 'w', encoding='utf-8') as f:
        json.dump(data, f, indent=4, ensure_ascii=False)