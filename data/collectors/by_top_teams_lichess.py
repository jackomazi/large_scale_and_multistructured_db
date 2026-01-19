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

        db["games_isaia"].drop()
        db["users_isaia"].drop()
        db["tournaments_isaia"].drop()

        collection_users = db["users_isaia"]
        collection_games = db["games_isaia"]
        collection_tournament = db["tournaments_isaia"]


        print("MongoDB connection successful")
    except:
        sys.exit(1)

    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    # fetch config data
    config_path = os.path.join(script_dir, "..", "config_lichess.json")
    with open(config_path, "r") as config_file:
        config_data = json.load(config_file)

    # number of pages to fetch teams from, each page has 15 teams, default 5
    number_of_pages = config_data.get("number_of_pages", 5)
    # number of users to fetch from each team, default 10
    number_of_users_per_team = config_data.get("number_of_users_per_team", 10)
    # number of games to fetch from each user, default 20
    number_of_games_per_user = config_data.get("number_of_games_per_user", 20)
    # number of tournaments to fetch from each user, default 5
    number_of_tournaments_per_user = config_data.get("number_of_tournaments_per_user", 5)
    
    teams = []

    for page in range(1, number_of_pages + 1): # fetch teams from pages
        teams_on_page = lichess_interface.get_teams_list(page)
        for team in teams_on_page:
            teams.append(team["id"])

    # after obtaining all teams (could be up to 15 * number_of_pages), we get n users from each team
    print(f"Total teams fetched: {len(teams)}")

    for i_team, team in enumerate(teams):
        print(f"Scraping team: {i_team} - {team}")
        player_data_list = []
        # get n users from team
        usernames = lichess_interface.get_n_users_from_team(team, number_of_users_per_team)
        # get player infos for each user
        for username in tqdm(usernames, desc=f"Scraping users from team {team}"):
            player_info = lichess_interface.get_player_infos(username)
            player_info = lichess_interface.format_lichess_player_infos(player_info)
            # store player info to MongoDB
            mongo_db_interface.store_dict_to_MongoDB(player_info, collection_users)
            # get n tournaments played by user
            tournaments = lichess_interface.get_n_lichess_player_tournaments(username, number_of_tournaments_per_user)
            # store tournaments to MongoDB
            for tournament in tournaments:
                ## First try, this works but just store for each tournament some basic infos
                #tournament = lichess_interface.format_lichess_player_tournament(tournament)
                #mongo_db_interface.store_dict_to_MongoDB(tournament, collection_tournament)

                ## Second try, use tournament id to fetch full tournament info
                tournament_id = tournament["tournament"]["id"]
                full_tournament_info = lichess_interface.get_lichess_tournament_infos_with_players(tournament_id)
                
                # format full tournament info
                full_tournament_info = lichess_interface.format_lichess_tournament_info(full_tournament_info)
                # store full tournament info to MongoDB
                mongo_db_interface.store_dict_to_MongoDB(full_tournament_info, collection_tournament)

            
    print("Finished processing all teams.")






