import re

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
        return response.json()

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

        return response.json()

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
        """Fetches a list of player usernames for a given country code."""
        url = f"https://api.chess.com/pub/country/{country_code}/players"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
            "AppleWebKit/537.36 (KHTML, like Gecko) "
            "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)
        return response.json().get("players", [])
