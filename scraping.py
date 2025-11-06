import requests
from pymongo import MongoClient
import logging
import logging.config
import sys

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

def fetch_chess_com_games(username, year, month):
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

def save_games_to_db(games):
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
    username = "jack_o_mazi"
    year = 2025
    month = 10
    games = fetch_chess_com_games(username, year, month)
    save_games_to_db(games)
    logger.info("Scraping and saving process completed.")
    logger.info("="*50)
