from pymongo import MongoClient
from tqdm import tqdm
import json
import sys

from lichess_interface import lichess_interface

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

    teams = ['lichess-swis']
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
            games = lichess_interface.get_lichess_games(
                f"https://lichess.org/api/games/user/{user}?max=20&format=ndjson&opening=true&pgnInJson=true"
            )

            for i_game, game in tqdm(enumerate(games), total=len(games), desc="Scraping games"):
                formatted_game = lichess_interface.format_lichess_game(game)
                # add id of game to user games
                user_info["games"].append(formatted_game["_id"])
                # saving games to mongodb
                mongo_db_interface.store_dict_to_MongoDB(formatted_game, collection_games)

            # Salvo utente su MongoDB
            mongo_db_interface.store_dict_to_MongoDB(user_info, collection_users)            
            






