import json
import sys
import os
from pymongo import MongoClient
from tqdm import tqdm

from api.lichess import lichess_interface
from storage.mongo_interface import mongo_db_interface

from storage.neo4j_interface import neo4j_interface
import time
if __name__ == "__main__":

    neo4j_dr = neo4j_interface()
    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()
        db = client["chess_db_test"]


        ## Uncomment to clear collections
        db["games_isaia"].drop()
        db["users_isaia"].drop()
        db["tournaments_isaia"].drop()
        db["teams_isaia"].drop()

        collection_users = db["users_isaia"]
        collection_games = db["games_isaia"]
        collection_tournament = db["tournaments_isaia"]
        collection_team = db["teams_isaia"]

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

    for page in range(3, number_of_pages + 3): # fetch teams from pages
        teams_on_page = lichess_interface.get_teams_list(page)
        for team in teams_on_page:
            teams.append(team["id"])

    # after obtaining all teams (could be up to 15 * number_of_pages), we get n users from each team
    print(f"Total teams fetched: {len(teams)}")

    for i_team, team in enumerate(teams):
        print(f"Scraping team: {i_team} - {team}")
        time.sleep(1)
        # team infos
        team_info = lichess_interface.get_team_infos(team)
        team_info = lichess_interface.format_team_infos(team_info)
        # save team info to MongoDB
        team_mongo_id = mongo_db_interface.store_dict_to_MongoDB(team_info, collection_team)
        # insert in neo4j
        neo4j_dr.insert_club_entity(str(team_mongo_id), team_info.get("name"))
        player_data_list = []
        # get n users from team
        usernames = lichess_interface.get_n_users_from_team(team, number_of_users_per_team)
        # get player infos for each user
        for username in tqdm(usernames, desc=f"Scraping users from team {team}"):
            time.sleep(1)
            player_info = lichess_interface.get_player_infos(username) # player info is a dict
            
            #time.sleep(1)
            if not player_info:
                print(f"  No data for user: {username}, skipping...")
            else:
                player_info["games"] = []
                player_info["team"] = team
                # get last n games of user
                games = lichess_interface.get_lichess_games(username, number_of_games_per_user)
                for i_game, game in tqdm(
                    enumerate(games), total=len(games), desc=f"  Scraping games for user {username}"
                ):
                    formatted_game = lichess_interface.format_lichess_game(game)
                    # saving games to mongodb
                    game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                        formatted_game, collection_games
                    )
                    # add essential game info to player data list
                    player_info["games"].append(lichess_interface.format_lichess_game_essentials(game_mongo_id, formatted_game, False))

                # format player info
                player_info = lichess_interface.format_lichess_player_infos(player_info)
                # store player info to MongoDB
                user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(player_info, collection_users)
                # insert user in neo4j
                neo4j_dr.insert_user_entity(str(user_mongo_id), player_info.get("username"))
                # connect user to team in neo4j
                neo4j_dr.connect_user_club(str(user_mongo_id), str(team_mongo_id), lichess_interface.format_lichess_player_essentials(user_mongo_id, player_info))
            
            # get n tournaments played by user
            tournaments = lichess_interface.get_n_lichess_player_tournaments(username, number_of_tournaments_per_user)
            # store tournaments to MongoDB
            print(f"  Tournaments found for user {username}: {len(tournaments)}")
            for tournament in tournaments:
                # get games for the tournament
                tournament_id = tournament["tournament"]["id"]
                if tournament_id:
                    infos = lichess_interface.get_lichess_tournament_infos(tournament_id)
                    # get a list of participants
                    players = lichess_interface.get_n_participants_in_tournament(infos)
                    print(players)
                    for p in players:
                        p_info = lichess_interface.get_player_infos(p)
                        p_info_formatted = lichess_interface.format_lichess_player_infos(p_info)
                        p_user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(p_info_formatted, collection_users)
                        neo4j_dr.insert_user_entity(str(p_user_mongo_id), p_info_formatted.get("username"))
                    infos_formatted = lichess_interface.format_lichess_tournament_info(infos)
                    tournament_mongo_id = mongo_db_interface.store_dict_to_MongoDB(infos_formatted, collection_tournament)
                    # insert tournament in neo4j
                    neo4j_dr.insert_tournament_entity(str(tournament_mongo_id), infos_formatted.get("name"))
                    # connect user to tournament in neo4j
                    neo4j_dr.connect_user_tournament(str(user_mongo_id), str(tournament_mongo_id), {})  
                    # get games for the tournament by the user
                    games = lichess_interface.get_lichess_tournament_games(tournament_id, username)
                    for game in games:
                        game_formatted = lichess_interface.format_lichess_game(game)
                        mongo_db_interface.store_dict_to_MongoDB(game_formatted, collection_games)  
    print("Finished processing all teams.")