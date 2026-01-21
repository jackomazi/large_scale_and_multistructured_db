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

        # change the database name if needed
        db = client["chess_db_test_3"]

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
        print("Could not connect to MongoDB, exiting...")
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
    # max games per user to store in document, default 100
    max_games_per_user = config_data.get("max_games_per_user", 100)
    # number of games per tournament
    number_of_games_per_tournament = config_data.get("number_of_games_per_tournament", 10)

    teams = []

    for page in range(1, number_of_pages + 1): # fetch teams from pages
        teams_on_page = lichess_interface.get_teams_list(page)
        for team in teams_on_page:
            teams.append(team["id"])

    # after obtaining all teams (could be up to 15 * number_of_pages), we get n users from each team
    print(f"Total teams fetched: {len(teams)}")

    for i_team, team in tqdm(enumerate(teams), total= len(teams), desc=f"Scraping team {team}"):
        
        members = []
        print(f"Scraping team: {i_team} - {team}")
        # team infos
        team_info = lichess_interface.get_team_infos(team)
        team_info = lichess_interface.format_team_infos(team_info)
        team_info["members"] = []  # will be filled later
        # save team info to MongoDB
        team_mongo_id = mongo_db_interface.store_dict_to_MongoDB(team_info, collection_team)
        # insert in neo4j
        neo4j_dr.insert_club_entity(str(team_mongo_id), team_info.get("name"))
        
        # get n users from team
        ids = lichess_interface.get_n_ids_from_team(team, number_of_users_per_team)
        # get their infos
        infos = lichess_interface.get_lichess_player_infos_by_list_ids(ids)
        for user_info in infos:
            time.sleep(1)
            if not user_info:
                print(f"  No data for user id: {user_info}, skipping...")
            else:
                user_info["games"] = []
                user_info["team"] = team
                username = user_info.get("username")

                # get n games from user
                games = lichess_interface.get_lichess_games(username, number_of_games_per_user)
                

                # format and store games
                for i_game, game in tqdm(
                    enumerate(games), total=len(games), desc=f"  Scraping games for user {username}"
                ):
                    if len(user_info["games"]) >= max_games_per_user:
                        break
                    formatted_game = lichess_interface.format_lichess_game(game)
                    # saving games to mongodb
                    game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                        formatted_game, collection_games
                    )
                    # add essential game info to player data list
                    user_info["games"].append(lichess_interface.format_lichess_game_essentials(game_mongo_id, formatted_game, False))

                # placeholders to reach max document size
                if len(user_info["games"]) < max_games_per_user:
                    for i in range(0, max_games_per_user - len(user_info.get("games"))):
                        user_info["games"].append(lichess_interface.format_lichess_game_essentials(None, None, True))

                # format player info
                # store player info to MongoDB
                user_info_formatted = lichess_interface.format_lichess_player_infos(user_info)
                if user_info_formatted is None:
                    continue

                user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(user_info_formatted, collection_users)
                # insert user in neo4j
                neo4j_dr.insert_user_entity(str(user_mongo_id), user_info_formatted.get("username"))
                # connect user to team in neo4j
                player_essentials = lichess_interface.format_lichess_player_essentials(user_mongo_id, user_info_formatted)
                neo4j_dr.connect_user_club(str(user_mongo_id), str(team_mongo_id), lichess_interface.format_lichess_player_essentials(user_mongo_id, user_info_formatted))
                # add user to team members list
                player_essentials["username"] = username
                members.append(player_essentials)
                
            # get n tournaments played by user
            tournaments = lichess_interface.get_n_lichess_player_tournaments(username, number_of_tournaments_per_user)
            # store tournaments to MongoDB
            print(f"  Tournaments found for user {username}: {len(tournaments)}")
            for tournament in tournaments:
                tour_games = []
                # get games for the tournament
                tournament_id = tournament["tournament"]["id"]
                if tournament_id:
                    # get infos
                    tour_infos = lichess_interface.get_lichess_tournament_infos(tournament_id)

                    games = lichess_interface.get_lichess_tournament_games(tournament_id, username)
                    for game in games:
                        game_formatted = lichess_interface.format_lichess_game(game)
                        game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(game_formatted, collection_games)
                        tour_games.append(lichess_interface.format_lichess_game_essentials(game_mongo_id, game_formatted, False))

                    # number of games in total in the tournament
                    number_of_games = tour_infos["stats"]["games"]
                    
                    # get all games played in the tournament
                    tour_games = lichess_interface.get_lichess_tournament_games_all(tournament_id, number_of_games_per_tournament)
                    formatted_games = []
                    for i, game in enumerate(tqdm(tour_games, desc=f"    Scraping all games for tournament {tournament_id}")):
                        print(f"    Processing game {i+1}/{len(tour_games)}")
                        game_formatted = lichess_interface.format_lichess_game(game)
                        game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(game_formatted, collection_games)
                        formatted_games.append(lichess_interface.format_lichess_game_essentials(game_mongo_id, game_formatted, False))

                    print(f"    Total games fetched for tournament {tournament_id}: {len(tour_games)}")
                    # get formatted infos
                    infos_formatted = lichess_interface.format_lichess_tournament_info(tour_infos)
                    infos_formatted["games"] = formatted_games
                    if len(infos_formatted["games"]) < number_of_games_per_tournament:
                            for i in range(0, number_of_games_per_tournament - len(infos_formatted["games"])):
                                infos_formatted["games"].append(lichess_interface.format_lichess_game_essentials(None, None, True))

                    # placeholders to reach max document size
                            if len(p_info_formatted["games"]) < max_games_per_user:
                                for i in range(0, max_games_per_user - len(p_info_formatted.get("games"))):
                                    p_info_formatted["games"].append(lichess_interface.format_lichess_game_essentials(None, None, True))
                            

                    # store tournament to MongoDB
                    tournament_mongo_id = mongo_db_interface.store_dict_to_MongoDB(infos_formatted, collection_tournament)
                    # insert tournament in neo4j
                    neo4j_dr.insert_tournament_entity(str(tournament_mongo_id), infos_formatted.get("name"))
                    # connect user to tournament in neo4j
                    neo4j_dr.connect_user_tournament(str(user_mongo_id), str(tournament_mongo_id), {})

                    ## here it's possible to fetch and store also other participants of the tournament, but you would need to 
                    ## use the url https://lichess.org/api/tournament/{tournament_id} and set a new page number, look at
                    ## the get_lichess_tournament_infos_with_players method in lichess_interface.py for reference
                    # get a list of participants
                    player_names = lichess_interface.get_n_participants_in_tournament(tour_infos)
                    try:
                        player_names.remove(username)  # remove the main user to avoid duplicate entry
                    except ValueError:
                        pass
                    # get infos about each participant using get_lichess_player_infos_by_list_ids
                    participants_infos = lichess_interface.get_lichess_player_infos_by_list_ids(player_names)
                    for p_info in participants_infos:
                        p_tour_games = []
                        time.sleep(1)
                        if not p_info:
                            print(f"    No data for user id: {p_info}, skipping...")
                        else:
                            p_info_formatted = lichess_interface.format_lichess_player_infos(p_info)
                            if p_info_formatted is None:
                                continue
                            games = lichess_interface.get_lichess_games(p_info_formatted.get("username"), number_of_games_per_user)
                            p_info_formatted["games"] = []
                            for i_game, game in tqdm(enumerate(games), total=len(games), desc=f"    Scraping games for tournament participant {p_info_formatted.get('username')}"):
                                if len(p_info_formatted["games"]) >= max_games_per_user:
                                    break
                                formatted_game = lichess_interface.format_lichess_game(game)
                                # saving games to mongodb
                                game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                                    formatted_game, collection_games
                                )
                                # add essential game info to player data list
                                p_info_formatted["games"].append(lichess_interface.format_lichess_game_essentials(game_mongo_id, formatted_game, False))
                            
                            #p_games = lichess_interface.get_lichess_tournament_games(tournament_id, p_info_formatted.get("username"))
                            #for game in p_games:
                            #    if len(p_info_formatted["games"]) >= max_games_per_user:
                            #        break
                            #    game_formatted = lichess_interface.format_lichess_game(game)
                            #    game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(game_formatted, collection_games)
                            #    p_tour_games.append(lichess_interface.format_lichess_game_essentials(game_mongo_id, game_formatted, False))
                            #    p_info_formatted["games"].append(lichess_interface.format_lichess_game_essentials(game_mongo_id, game_formatted, False))
                            # add tournament games to tournament
                            #mongo_db_interface.add_games_to_tournament_in_MongoDB(tournament_mongo_id, p_tour_games, collection_tournament)

                            # placeholders to reach max document size
                            if len(p_info_formatted["games"]) < max_games_per_user:
                                for i in range(0, max_games_per_user - len(p_info_formatted.get("games"))):
                                    p_info_formatted["games"].append(lichess_interface.format_lichess_game_essentials(None, None, True))
                            

                            # store participant info to MongoDB
                            p_user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(p_info_formatted, collection_users)
                            # insert user in neo4j
                            neo4j_dr.insert_user_entity(str(p_user_mongo_id), p_info_formatted.get("username"))
                            # connect user to tournament in neo4j
                            neo4j_dr.connect_user_tournament(str(p_user_mongo_id), str(tournament_mongo_id), {})
        
        # after processing all users of a given team, add them to team in MongoDB
        mongo_db_interface.add_members_to_team_in_MongoDB(team_mongo_id, members, collection_team)



    print("Finished processing all teams.")