import re
from datetime import datetime
from faker import Faker

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
        user_info.pop("url")
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
    # Formatting farched games dict into the desired form
    # Note: Does modify certains values and name fields
    def format_chess_com_game(game: dict) -> dict:
        return {
            "url": game.get("url"),
            "white_player": game.get("white", {}).get("username"),
            "black_player": game.get("black", {}).get("username"),
            "white_rating": game.get("white", {}).get("rating"),
            "black_rating": game.get("black", {}).get("rating"),
            "result_white": game.get("white", {}).get("result"),
            "result_black": game.get("black", {}).get("result"),
            "eco_url": game.get("eco"),
            "opening": game.get("eco", "").split("/")[-1] if game.get("eco") else None,
            "moves": chess_com_interface.extract_moves_from_pgn(game.get("pgn", "")),
            "time_class": game.get("time_class"),
            "rated": game.get("rated"),
            "end_time": game.get("end_time"),
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
