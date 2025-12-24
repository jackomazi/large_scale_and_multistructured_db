import json
import sys
import os

# Using chess.com interface methods
from api.chess_com import chess_com_interface
from pymongo import MongoClient
from bson.objectid import ObjectId

# Using opening detector
from api.absolute_opening_detector import ChessOpeningDetector

# Using mongo db mothod to interact with a certain collection
from storage.mongo_interface import mongo_db_interface
from tqdm import tqdm

if __name__ == "__main__":

    # Opening detector
    opening_detector = ChessOpeningDetector()

    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()
        db = client["chess_db_test"]
        collection_users = db["users_isaia"]
        collection_games = db["games_isaia"]
        collection_clubs = db["clubs_isaia"]
        collection_tournament = db["tournaments_isaia"]
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
        scraping_values["max_scrap_games_per_archive"] = data["max_scrap_games_per_archive"]
        scraping_values["maximum_games_stored_per_user_document"] = data["maximum_games_stored_per_user_document"]
        scraping_values["maximum_user_stored_per_club"] = data["maximum_user_stored_per_club"]
        scraping_values["clubs"] = data["clubs"]
        scraping_values["max_tournament_per_user"] = data["max_tournament_per_user"]
        scraping_values["maximum_games_per_tournament"] = data["maximum_games_per_tournament"]
    except:
        print("Failed loading config proceeding with default values")
        # Default scraping values
        scraping_values = {
            "max_scrap_users_per_club": 10,
            "max_scrap_archives": 3,
            "max_scrap_games_per_archive": 20,
            "maximum_games_stored_per_user_document": 200,
            "maximum_user_stored_per_club": 200,
            "clubs": ["india-11"],
        }

    for i_club, club in enumerate(scraping_values["clubs"]):
        print(f"Scraping club: {i_club}")
        # Club scraping
        usernames = chess_com_interface.get_players_usernames(club)
        club = chess_com_interface.get_club_info(club)
        # Users scraping
        for i_user, user in enumerate(usernames):

            if i_user >= scraping_values.get("max_scrap_users_per_club"):
                break

            user_info = chess_com_interface.get_player_infos(user)
            user_archives = chess_com_interface.get_player_games_archives(user)
            # Storing user game stats
            user_info["stats"] = chess_com_interface.get_player_games_stats(user)
            # Fetching user tournament data
            tournaments = chess_com_interface.get_chess_com_player_tournaments(user)
            for i_tournament, tournament in enumerate(tournaments):

                if i_tournament >= scraping_values.get("max_tournament_per_user"):
                    break

                tournament_url = tournament.get("@id")

                #Create tournament document for mongoDB
                tournament_doc = chess_com_interface.get_chess_com_tournament(tournament_url)
                if "Error" in tournament_doc:
                    print("Jumping")
                    continue

                #Fetching games from tournaments and mongoDB memorization and cached inside tournament document
                games = chess_com_interface.get_games_from_tournament(tournament_url)
                if games is None:
                    print("Jumping")
                    continue

                for i_game, game in tqdm(enumerate(games), total=len(games), desc="Scraping games tournaments"):
                    if i_game >= scraping_values.get("maximum_games_per_tournament"):
                        break
                    formatted_game = chess_com_interface.format_chess_com_game(game)
                    if formatted_game["opening"] is None:
                        formatted_game["opening"] = chess_com_interface.fetch_tournament_game_opening(game.get("pgn"),opening_detector)
                    game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                        formatted_game, collection_games
                    )
                    tournament_doc["games"].append(chess_com_interface.format_chess_com_game_essentials(game_mongo_id,formatted_game,False))

                #MongoDB tournament memorization
                tournament_mongo_id = mongo_db_interface.store_dict_to_MongoDB(tournament_doc, collection_tournament)

                #Caching inside user profile
                user_info["tournaments"].append(chess_com_interface.format_chess_com_tournament_essentials(tournament_mongo_id, tournament, False))

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

                    if i_game >= scraping_values.get("max_scrap_games_per_archive"):
                        break

                    formatted_game = chess_com_interface.format_chess_com_game(game)

                    # Saving games to mongoDB
                    game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                        formatted_game, collection_games
                    )

                    # Add id of game to user games
                    user_info["games"].append(chess_com_interface.format_chess_com_game_essentials(game_mongo_id,formatted_game,False))

                if i_archive >= scraping_values.get("max_scrap_archives"):
                    break

            # Set maximum document size to minimize document relocation
            # Insert "blank" data into games array
            for i in range(0,scraping_values["maximum_games_stored_per_user_document"] - len(user_info.get("games"))):
                user_info["games"].append(chess_com_interface.format_chess_com_game_essentials(None, None, True))

            # Saving user to mongoDB
            user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(user_info, collection_users)

            # Storing user short profile into club document
            club["members"].append(chess_com_interface.format_chess_com_player_essentials(user_mongo_id, user_info, False)) 

        # Set maximum document size to minimize document relocation
        # Insert "blank" data into games array
        club["members_number"] = len(club.get("members"))
        for i in range(0,scraping_values["maximum_user_stored_per_club"] - len(club.get("members"))):
            club["members"].append(chess_com_interface.format_chess_com_player_essentials(None, None, True))    

        #Saving club to mongo DB
        mongo_db_interface.store_dict_to_MongoDB(club, collection_clubs)
