import json
import sys
import os
from pymongo import MongoClient
from tqdm import tqdm

from api.lichess import lichess_interface
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

    script_dir = os.path.dirname(os.path.abspath(__file__))
    config_path = os.path.join(script_dir, "..", "config_lichess.json")
    with open(config_path, "r") as config_file:
        config_data = json.load(config_file)
    n = config_data.get("number_of_games_per_user", 20)
    teams = ["lichess-swis"]
    
    for i_team, team in enumerate(teams):
        print(f"Scraping team: {i_team}")
        # Team scraping
        usernames = lichess_interface.get_players_usernames(team)

        for i_user, user in enumerate(usernames):
            print(f"Scraping user {i_user} of team {i_team}")
            user_info = lichess_interface.get_player_infos(user)
            # Rename id property complying to mongo DB specification
            user_info["_id"] = user_info.pop("id")
            # Add key property to store user players ids
            user_info["games"] = []
            
            # get last 20 games of user
            games = lichess_interface.get_lichess_games(user, n)

            for i_game, game in tqdm(
                enumerate(games), total=len(games), desc="Scraping games"
            ):
                formatted_game = lichess_interface.format_lichess_game(game)
                # add id of game to user games
                user_info["games"].append(formatted_game["_id"])
                # saving games to mongodb
                mongo_db_interface.store_dict_to_MongoDB(
                    formatted_game, collection_games
                )

            # Salvo utente su MongoDB
            mongo_db_interface.store_dict_to_MongoDB(user_info, collection_users)
