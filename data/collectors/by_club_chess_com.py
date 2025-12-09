import json
import sys
import os

# Using chess.com interface methods
from api.chess_com import chess_com_interface
from pymongo import MongoClient
from bson.objectid import ObjectId

# Using mongo db mothod to interact with a certain collection
from storage.mongo_interface import mongo_db_interface
from tqdm import tqdm

if __name__ == "__main__":
    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()
        db = client["chess_db_test"]
        collection_users = db["users_isaia"]
        collection_games = db["games_isaia"]
    except:
        sys.exit(1)

    # Config fetching
    scraping_values = {}
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        config_path = os.path.join(script_dir, "..", "config.json")
        with open(config_path, "r") as config:
            data = json.load(config)
        scraping_values["max_scrap_users_per_club"] = data["max_scrap_users_per_club"]
        scraping_values["max_scrap_archives"] = data["max_scrap_archives"]
        scraping_values["max_scrap_games_per_archive"] = data[
            "max_scrap_games_per_archive"
        ]
        scraping_values["clubs"] = data["clubs"]
    except:
        print("Failed loading config proceeding with default values")
        # Default scraping values
        scraping_values = {
            "max_scrap_users_per_club": 10,
            "max_scrap_archives": 3,
            "max_scrap_games_per_archive": 20,
            "clubs": ["india-11"],
        }

    for i_club, club in enumerate(scraping_values["clubs"]):
        print(f"Scraping club: {i_club}")
        # Club scraping
        usernames = chess_com_interface.get_players_usernames(club)
        # Users scraping
        for i_user, user in enumerate(usernames):
            user_info = chess_com_interface.get_player_infos(user)
            user_archives = chess_com_interface.get_player_games_archives(user)
            # Slight variation to user object
            # Rename id property complying to mongo DB specification
            user_info.pop("@id")
            # Add key property to store user players ids
            user_info["games"] = []
            # Add key property to store user game stats
            user_info["stats"] = chess_com_interface.get_player_games_stats(user)

            if i_user >= scraping_values.get("max_scrap_users_per_club"):
                break

            # Archives scraping
            for i_archive, archive_url in enumerate(user_archives):
                print(
                    f"Scraping archive {i_archive} of user {i_user} of club: {i_club}"
                )
                games = chess_com_interface.get_chess_com_games(archive_url)
                # If the archive is empty jump ahead
                if not games:
                    continue

                # Formating
                for i_game, game in tqdm(enumerate(games), total=len(games), desc="Scraping games"):
                    formatted_game = chess_com_interface.format_chess_com_game(game)

                    # Saving games to mongoDB
                    game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                        formatted_game, collection_games
                    )

                    # Add id of game to user games
                    user_info["games"].append(game_mongo_id)

                    if i_game >= scraping_values.get("max_scrap_games_per_archive"):
                        break

                if i_archive >= scraping_values.get("max_scrap_archives"):
                    break

            # Saving user to mongoDB
            mongo_db_interface.store_dict_to_MongoDB(user_info, collection_users)
