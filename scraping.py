import requests
from pymongo import MongoClient
import logging
import logging.config
import sys
import json
from time import sleep
from datetime import datetime
from dateutil.relativedelta import relativedelta
import calendar
import time
import re

logging.config.fileConfig('log/logger.config')
logger = logging.getLogger('scraping')
logger.info("="*50)
logger.info("Starting the chess.com scraping script.")

try:
    client = MongoClient("mongodb://localhost:27017/")
    client.server_info()
    logger.info("Connected to MongoDB successfully.")
    db = client["chess_db_test"]
    collection = db["games"]
except Exception as e:
    logger.error(f"Failed to connect to MongoDB: {e}")
    logger.info("="*50)
    sys.exit(1)

def extract_moves_from_pgn(pgn: str) -> str:
    """Extracts and formats the moves from a PGN string."""
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


def format_chess_com_game(game: dict) -> dict:
    """Formats a Chess.com game dictionary into a structured format for MongoDB storage."""
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
        "moves": extract_moves_from_pgn(game.get("pgn", "")),
        "time_class": game.get("time_class"),
        "rated": game.get("rated"),
        "end_time": game.get("end_time"),
    }

def fetch_chess_com_games(username: str, year: int, month: int) -> list:
    """Fetches games for a given Chess.com username within a specified year and month."""
    url = f"https://api.chess.com/pub/player/{username}/games/{year}/{month:02d}"
    logger.info(f"Fetching games for {username} for {year}-{month:02d} from {url}")
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/130.0.0.0 Safari/537.36",
        "Accept": "application/json",
    }
    response = requests.get(url, headers=headers)
    if response.status_code == 200:
        games = response.json().get("games", [])
        logger.info(f"Fetched {len(games)} games for {username} for {year}-{month:02d}")
        return games 
    else:
        logger.error(f"Failed to fetch games for {username} for {year}-{month:02d}")
        logger.error(f"Status Code: {response.status_code}, Response: {response.text}")
        logger.info("="*50)
        sys.exit(1)

def fetch_chess_com_games_v2(url : str) -> list:
    """Fetches games from a given Chess.com API URL."""
    logger.info(f"Fetching games from {url}")
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
        logger.error(f"Status Code: {response.status_code}, Response: {response.text}")
        logger.info("="*50)
        return []


def format_lichess_game(game: dict) -> dict:
    """Formats a Lichess game dictionary into a structured format for MongoDB storage."""
    white = game.get("players", {}).get("white", {})
    black = game.get("players", {}).get("black", {})
    winner = game.get("winner")
    
    return {
        "url": f"https://lichess.org/{game.get('id')}",
        "white_player": white.get("user", {}).get("name"),
        "black_player": black.get("user", {}).get("name"),
        "white_rating": white.get("rating"),
        "black_rating": black.get("rating"),
        "result_white": "win" if winner == "white" else "loss" if winner == "black" else "draw",
        "result_black": "win" if winner == "black" else "loss" if winner == "white" else "draw",
        "eco_url": game.get("opening", {}).get("eco"),
        "opening": game.get("opening", {}).get("name"),
        "moves": game.get("moves"),
        "time_class": game.get("speed"),
        "rated": game.get("rated"),
        "end_time": datetime.fromtimestamp(game.get("lastMoveAt")/1000) if game.get("lastMoveAt") else None
    }

def fetch_lichess_games(username: str, year: str, month: str) -> list:
    """Fetches games for a given Lichess username within a specified year and month."""
    start_day = datetime(year, month, 1) # primo giorno del mese
    last_day = calendar.monthrange(year, month)[1] # ottengo l'ultimo giorno del mese
    end_date = datetime(year, month, last_day, 23, 59, 59)

    since = int(calendar.timegm(start_day.timetuple()) * 1000) # timestamp in milliseconds, mi serve per l'url di lichess
    until = int(calendar.timegm(end_date.timetuple()) * 1000)

    # costruisco l'url per ottenere le partite
    # aggiunto parametro opening=true per ottenere info apertura
    # aggiunto pgnInJson=true per ottenere i movimenti in formato stringa
    # ci sono altri parametri che si possono aggiungere, vedi documentazione lichess API
    url = f"https://lichess.org/api/games/user/{username}?since={since}&until={until}&max=300&format=ndjson&opening=true&pgnInJson=true"

    logger.info(f"Fetching games for {username} for {year}-{month:02d} from {url}")
    headers = {"Accept": "application/x-ndjson"} # per ottenere risposta in formato ndjson
    try:
        response = requests.get(url, headers=headers, stream =True)
    except Exception as e:
        logger.error(f"Error fetching games for {username} for {year}-{month:02d}: {e}")
        logger.info("="*50)
        sys.exit(1)
    
    games = []
    if response.status_code == 200:
        sleep(1) # per evitare di sovraccaricare il server di lichess
        for line in response.iter_lines():
            if line:
                game = json.loads(line) # transformo la riga in un dizionario
                games.append(game)
        logger.info(f"Fetched {len(games)} games for {username} for {year}-{month:02d}")
        return games
    else:
        logger.error(f"Failed to fetch games for {username} for {year}-{month:02d}")
        logger.error(f"Status Code: {response.status_code}, Response: {response.text}")
        logger.info("="*50)
        sys.exit(1)

def save_games_to_db(games: str, formatter: callable) -> None:
    """Saves a list of games to the MongoDB database using the provided formatter function."""
    if not games:
        logger.info("No games to save to the database.")
        return
    
    for g in games: # scorre tutte le partite ottenute
        game_doc = formatter(g) # formatta la partita usando la funzione di formattazione specifica
        collection.update_one( # aggiorna o inserisce il documento nel db
            {"url": game_doc["url"]},
            {"$set": game_doc},
            upsert=True
        )
    logger.info(f"Saved {len(games)} games to the database.")


if __name__ == "__main__":
    # lista di users di chess_com da processare
    chess_com_users = ["jack_o_mazi"]#,
                        #"jeccabahug", 
                        #"MagnusCarlsen",
                        #  "Hikaru",
                        #    "GothamChess", 
                        #    "FabianoCaruana", 
                        #    "GukeshDommaraju", 
                        #    "AlirezaFirouzja"]
    
    # lista di users di lichess da processare
    #lichess_users = ["OjaiJoao"]
    
    # calcolo l'anno e il mese scorso
    now = datetime.now()

    last_month = now - relativedelta(months=1)
    year = last_month.year
    month = last_month.month

    # processa gli utenti di chess.com
    for username in chess_com_users:
        logger.info("-"*50)
        logger.info(f"Starting to process player: {username}")
        games = fetch_chess_com_games(username, year, month) # ottengo le partite di chess.com di username, in uno specifico mese e anno
        save_games_to_db(games, format_chess_com_game) # salvo le partite nel db

    # processa gli utenti di lichess
    for username in lichess_users:
        logger.info("-"*50)
        logger.info(f"Starting to process player: {username}")
        games = fetch_lichess_games(username, year, month)
        save_games_to_db(games, format_lichess_game)

    logger.info("Scraping and saving process completed.")
    logger.info("="*50)
