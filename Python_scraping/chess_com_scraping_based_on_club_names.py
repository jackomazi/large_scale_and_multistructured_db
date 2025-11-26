from pymongo import MongoClient
from tqdm import tqdm
import json
import sys

#Using chess.com interface methods
from chess_com_interface import chess_com_interface
    
#Using mongo db mothod to interact with a certain collection
from mongo_db_interface import mongo_db_interface

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
        with open('Scraping_config.json', 'r') as config:
            data = json.load(config)
        scraping_values["max_scrap_users_per_club"] = data["max_scrap_users_per_club"]
        scraping_values["max_scrap_archives"] = data["max_scrap_archives"]
        scraping_values["max_scrap_games_per_archive"] = data["max_scrap_games_per_archive"]
        scraping_values["clubs"] = data["clubs"]
    except:
        print("Failed loading config proceeding with default values")
        # Default scraping values
        scraping_values = {
            "max_scrap_users_per_club": 10,
            "max_scrap_archives": 3,
            "max_scrap_games_per_archive": 20,
            "clubs": ["india-11"]
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
            user_info["_id"] = user_info.pop("@id")
            # Add key property to store user players ids
            user_info["games"] = []
        
            if i_user >= scraping_values.get("max_scrap_users_per_club"):
                break

            # Archives scraping
            for i_archive, archive_url in enumerate(user_archives):
                print(f"Scraping archive {i_archive} of user {i_user} of club: {i_club}")
                games = chess_com_interface.get_chess_com_games(archive_url)
                #If the archive is empty jump ahead
                if games is []:
                    continue

                # Formating
                for i_game, game in tqdm(enumerate(games), desc="Scraping archive games..."):
                    formatted_game = chess_com_interface.format_chess_com_game(game)
                    # Rename id property complying to mongo DB specification
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


            
            
            
        

