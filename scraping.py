import requests
from pymongo import MongoClient
import logging
import logging.config
import sys
import json
from time import sleep

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
        "pgn": game.get("pgn"),
        "time_class": game.get("time_class"),
        "rated": game.get("rated"),
        "end_time": game.get("end_time"),
    }

def fetch_chess_com_games(username: str, year: int, month: int) -> list:
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

def format_lichess_game(game: dict) -> dict:
    return {
        "url": game.get("url"),
        "white_player": game.get("players", {}).get("white", {}).get("user", {}).get("name"),
        "black_player": game.get("players", {}).get("black", {}).get("user", {}).get("name"),
        "white_rating": game.get("players", {}).get("white", {}).get("rating"),
        "black_rating": game.get("players", {}).get("black", {}).get("rating"),
        "result_white": game.get("players", {}).get("white", {}).get("result"),
        "result_black": game.get("players", {}).get("black", {}).get("result"),
        "eco_url": game.get("opening", {}).get("eco"),
        "opening": game.get("opening", {}).get("name"),
        "pgn": game.get("pgn"),
        "time_class": game.get("speed"),
        "rated": game.get("rated"),
        "end_time": game.get("end_time"),
    }


def fetch_lichess_games(username: str, year: str, month: str) -> list:
    url = f"https://lichess.org/api/games/user/{username}?since={year}-{month:02d}-01T00:00:00Z&until={year}-{month:02d}-31T23:59:59Z"
    logger.info(f"Fetching games for {username} for {year}-{month:02d} from {url}")
    headers = {"Accept": "application/x-ndjson"}
    try:
        response = requests.get(url, headers=headers, stream =True)
    except Exception as e:
        logger.error(f"Error fetching games for {username} for {year}-{month:02d}: {e}")
        logger.info("="*50)
        sys.exit(1)
    
    games = []
    if response.status_code == 200:
        sleep(1)
        for line in response.iter_lines():
            if line:
                game = json.loads(line)
                games.append(game)
        logger.info(f"Fetched {len(games)} games for {username} for {year}-{month:02d}")
        return games
    else:
        logger.error(f"Failed to fetch games for {username} for {year}-{month:02d}")
        logger.error(f"Status Code: {response.status_code}, Response: {response.text}")
        logger.info("="*50)
        sys.exit(1)

def save_games_to_db(games: str, formatter: callable) -> None:
    for g in games:
        game_doc = formatter(g)
        collection.update_one(
            {"url": game_doc["url"]},
            {"$set": game_doc},
            upsert=True
        )


def save_games_to_db_chess_com(games: list) -> None:
    if not games:
        logger.info("No games to save to the database.")
        return
    for g in games:
        game_doc = {
            "url": g.get("url"),
            "white_player": g.get("white", {}).get("username"),
            "black_player": g.get("black", {}).get("username"),
            "white_rating": g.get("white", {}).get("rating"),
            "black_rating": g.get("black", {}).get("rating"),
            "result_white": g.get("white", {}).get("result"),
            "result_black": g.get("black", {}).get("result"),
            "eco_url": g.get("eco"),
            "opening": g.get("eco", "").split("/")[-1] if g.get("eco") else None,
            "pgn": g.get("pgn"),
            "time_class": g.get("time_class"),
            "rated": g.get("rated"),
            "end_time": g.get("end_time"),
        }
        
        collection.update_one(
            {"url": game_doc["url"]},
            {"$set": game_doc},
            upsert=True
        )
    logger.info(f"Saved {len(games)} games to the database.")


if __name__ == "__main__":
    chess_com_users = ["jack_o_mazi", "jeccabahug"]
    lichess_users = ["MagnusCarlsen"]
    
    for username in chess_com_users:
        logger.info("-"*50)
        logger.info(f"Starting to process player: {username}")
        for year in [2024]:
            logger.info(f"Processing year: {year}")
            for month in range(1, 5):
                logger.info(f"Processing month: {month:02d}")
                games = fetch_chess_com_games(username, year, month)
                save_games_to_db(games, format_chess_com_game)
    
    for username in lichess_users:
        logger.info("-"*50)
        logger.info(f"Starting to process player: {username}")
        for year in [2024]:
            logger.info(f"Processing year: {year}")
            for month in range(1, 5):
                logger.info(f"Processing month: {month:02d}")
                games = fetch_lichess_games(username, year, month)
                save_games_to_db(games, format_lichess_game)
    
    logger.info("Scraping and saving process completed.")
    logger.info("="*50)
