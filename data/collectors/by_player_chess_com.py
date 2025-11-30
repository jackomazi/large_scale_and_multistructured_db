from pymongo import MongoClient
import json
import sys
import os

#Using chess.com interface methods
from api.chess_com import chess_com_interface
    
#Using mongo db mothod to interact with a certain collection
from storage.mongo_interface import mongo_db_interface

if __name__ == "__main__":

    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()
        db = client["chess_db_test"]
        collection_users = db["users_isaia"]
        collection_games = db["games_isaia"]
    except:
        sys.exit(1)
    
    #Config fetching
    scraping_values = {}
    try:
        script_dir = os.path.dirname(os.path.abspath(__file__))
        config_path = os.path.join(script_dir, "..", "config.json")
        with open(config_path, 'r') as config:
            data = json.load(config)
        scraping_values["max_scrap_users_per_club"] = data["max_scrap_users_per_club"]
        scraping_values["max_scrap_archives"] = data["max_scrap_archives"]
        scraping_values["max_scrap_games_per_archive"] = data["max_scrap_archives"]
        scraping_values["usernames"] = data["players_usernames"]
    except:
        print("Failed loading config proceeding with default values")
        # Default scraping values
        scraping_values = {
            "max_scrap_users_per_club": 10,
            "max_scrap_archives": 3,
            "max_scrap_games_per_archive": 20,
            "usernames": ["jack_o_mazi",
                        "jeccabahug", 
                        "MagnusCarlsen",
                        "Hikaru",
                        "GothamChess", 
                        "FabianoCaruana", 
                        "GukeshDommaraju", 
                        "AlirezaFirouzja"]
            }
    
    for username in scraping_values["usernames"]:
        # Fetching user games archives
        archives = chess_com_interface.get_player_games_archives(username)
        # Fetching user infos
        user_info = chess_com_interface.get_player_infos(username)
        # Rename id property complying to mongo DB specification
        user_info["_id"] = user_info.pop("@id")
        # Add key property to store user players ids
        user_info["games"] = []

        for i_archive, archive in enumerate(archives):
            # Fetching games from archive
            games = chess_com_interface.get_chess_com_games(archive)
            #If the archive is empty jump ahead
            if games is []:
                continue

            for i_game, game in enumerate(games):
                formatted_game = chess_com_interface.format_chess_com_game(game)
                # Rname id property complying to mongo DB specification
                formatted_game["_id"] = formatted_game["url"]

                # Add id of game to user games
                user_info["games"].append(formatted_game["_id"])

                # Saving games to mongoDB
                mongo_db_interface.store_dict_to_MongoDB(formatted_game,collection_games)

                if i_game >= scraping_values.get("max_scrap_games_per_archive"):
                    break

            if i_archive >= scraping_values.get("max_scrap_archives"):
                    break
            
        #Saving user to mongoDB
        mongo_db_interface.store_dict_to_MongoDB(user_info,collection_users)
            
        