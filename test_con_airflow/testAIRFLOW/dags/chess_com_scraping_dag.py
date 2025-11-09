from datetime import datetime
from pymongo import MongoClient
from airflow.sdk import dag, task
from airflow.models import Variable
import requests

API_BASE = "https://api.chess.com/pub/player"

@dag(
    dag_id="chess_com_scraping_dag",
    schedule='@weekly',
    start_date=datetime(2024, 1, 1),
    catchup=False,
    tags=['chess', 'scraping', 'chess.com']
)
def chess_com_scraping_dag():

    @task
    def fetch_games(username: str, year: int, month: int) -> list:
        url = f"{API_BASE}/{username}/games/{year}/{month:02d}"
        headers = {
            "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                          "AppleWebKit/537.36 (KHTML, like Gecko) "
                          "Chrome/130.0.0.0 Safari/537.36",
            "Accept": "application/json",
        }
        response = requests.get(url, headers=headers)
        if response.status_code == 200:
            data = response.json()
            return data.get("games", [])
        else:
            raise Exception(f"Failed to fetch games: {response.status_code} - {response.text}")

    @task
    def format_games(games: list) -> list:
        formatted_games = []
        for game in games:
            formatted_game = {
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
            formatted_games.append(formatted_game)
        return formatted_games
    
    @task
    def save_to_mongodb(games: list):
        print(f"Saving {len(games)} games to MongoDB")
        try:
            mongo_url = Variable.get("MONGODB_URL", default_var="mongodb://host.docker.internal:27017/")
            client = MongoClient(mongo_url)
            db = client["chess_db"]
            collection = db["games"]
            if not games:
                print("No games to save.")
                collection.update_one(
                    {"test": "example"},
                    {"$set": {"test": "example"}},
                    upsert=True
                )
                return
            for game in games:
                collection.update_one(
                    {"url": game["url"]},
                    {"$set": game},
                    upsert=True
                )
        except Exception as e:
            raise Exception(f"Failed to connect to MongoDB: {e}")

    usernames = ["jack_o_mazi", "jeccabahug"]
    years = [2024]
    months = list(range(1, 13))
    fetch_results = fetch_games.expand(
        username=usernames,
        year=years,
        month=months
    )
    formatted_results = format_games.expand(games=fetch_results)
    save_to_mongodb.expand(games=formatted_results)

chess_com_scraping_dag()