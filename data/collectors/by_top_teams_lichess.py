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
    # fetch config data
    config_path = os.path.join(script_dir, "..", "config_lichess.json")
    with open(config_path, "r") as config_file:
        config_data = json.load(config_file)

    # number of pages to fetch teams from, each page has 15 teams, default 5
    number_of_pages = config_data.get("number_of_pages", 5)
    # number of users to fetch from each team, default 10
    number_of_users_per_team = config_data.get("number_of_users_per_team", 10)
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
        usernames = lichess_interface.get_n_users_from_team(team, number_of_users_per_team)
        for username in tqdm(usernames, desc=f"Scraping users from team {team}"):
            player_info = lichess_interface.get_player_infos(username)
            if player_info:
                player_data_list.append(player_info)
            
            if not player_data_list:
                print(f"No player data collected for team {team}.")
                continue

            dumps_dir = os.path.join(os.path.dirname(script_dir), "dumps/lichess_teams")
            os.makedirs(dumps_dir, exist_ok=True)
            output_path = os.path.join(dumps_dir, f"{team}_players.json")

            try:
                with open(output_path, "w") as f:
                    json.dump(player_data_list, f, indent=4)
                print(
                    f"Successfully saved {len(player_data_list)} players' data to {output_path}"
                )
            except IOError as e:
                print(f"Error saving data for team {team}: {e}")

    print("Finished processing all teams.")






