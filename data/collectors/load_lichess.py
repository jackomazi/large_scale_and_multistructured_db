# standard library
import json
import sys
import os
import time
import random

# third-party
from pymongo import MongoClient
from tqdm import tqdm

# project
from api.lichess import LichessInterface
from storage.mongo_interface import mongo_db_interface
from storage.neo4j_interface import neo4j_interface

if __name__ == "__main__":

    neo4j_dr = neo4j_interface()
    # mongodb connection
    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()

        # change the database name if needed
        db = client["chess_db_test_3"]

        ## Uncomment to clear collections
        #db["games"].drop()
        db["users"].drop()
        db["tournaments"].drop()
        db["clubs"].drop()

        collection_users = db["users"]
        collection_games = db["games"]
        collection_tournament = db["tournaments"]
        collection_team = db["clubs"]

        print("MongoDB connection successful")
    except:
        print("Could not connect to MongoDB, exiting...")
        sys.exit(1)

    
    script_dir = os.path.dirname(os.path.abspath(__file__))
    # fetch config data
    config_path = os.path.join(script_dir, "..", "config.json")
    with open(config_path, "r") as config_file:
        config_data = json.load(config_file)

    # CONFIG
    NUM_PAGES = config_data.get("number_of_pages", 5)
    USERS_PER_TEAM = config_data.get("number_of_users_per_team", 10)
    GAMES_PER_USER = config_data.get("number_of_games_per_user", 20)
    TOURNAMENTS_PER_USER = config_data.get("max_tournament_per_user", 5)
    MAX_GAMES_PER_USER = config_data.get("max_games_per_user", 100)
    GAMES_PER_TOURNAMENT = config_data.get("maximum_games_per_tournament", 10)
    NUM_PARTICIPANTS = config_data.get("number_of_participant")
    COUNTRIES = config_data.get("countries")
    OPENINGS = config_data.get("openings")

    # this part purpose is to have 15 real teams, taken from page 25, stored in mongodb and neo4j
    # this allow to connect randomly users to teams
    # the reason is because when extracting participants's infos from tournament, the club is not available
    teams = []
    print("Fetching team infos...")
    random_teams = LichessInterface.get_teams_list(25)
    for team in random_teams:
        teams.append(team["id"])
        
    random_teams_dict = {} # this will store "mongo_team_id" : "team_name"
    for team in teams:
        team_info = LichessInterface.get_team_infos(team)
        team_info = LichessInterface.format_team_infos(team_info, COUNTRIES)
        team_mongo_id = mongo_db_interface.store_dict_to_MongoDB(team_info, collection_team)
        neo4j_dr.insert_club_entity(str(team_mongo_id), team_info.get("name"))
        random_teams_dict[str(team_mongo_id)] = team_info.get("name")
    
    # add none club
    team_keys = list(random_teams_dict.keys())

    team_keys.extend([None] * 5)  # adding None values to have some users without a team

    teams = []

    for page in range(1, NUM_PAGES + 1): # fetch teams from page 1 to number of pages
        teams_on_page = LichessInterface.get_teams_list(page)
        for team in teams_on_page:
            teams.append(team["id"])

    # after obtaining all teams (could be up to 15 * number_of_pages), we get n users from each team
    total_team_fetched = len(teams)
    print(f"Total teams fetched: {total_team_fetched}")
    
    for i_team, team in tqdm(enumerate(teams), total= len(teams), desc=f"Scraping teams"):
        # max tournaments per team
        max_tournaments_per_team = 40

        count_tournaments_team = 0

        print(f"Scraping team: {i_team}/{total_team_fetched - 1} - {team}")
        # team infos + format
        team_info = LichessInterface.get_team_infos(team)
        team_info = LichessInterface.format_team_infos(team_info, COUNTRIES)
        # save team info to MongoDB
        team_mongo_id = mongo_db_interface.store_dict_to_MongoDB(team_info, collection_team)
        # insert in neo4j
        neo4j_dr.insert_club_entity(str(team_mongo_id), team_info.get("name"))
        # add each team to the random_teams_dict
        random_teams_dict[str(team_mongo_id)] = team_info.get("name")

        # get n users's id from team
        ids = LichessInterface.get_n_ids_from_team(team, USERS_PER_TEAM)
        # get their infos
        infos = LichessInterface.get_lichess_player_infos_by_list_ids(ids)
        for user_info in infos:
            if not user_info:
                print(f"  No data for user id: {user_info}, skipping...")
                continue
            
            user_info["games"] = [] # games array
            
            username = user_info.get("username")

            # get n games from user
            games = LichessInterface.get_lichess_games(username, GAMES_PER_USER)
            
            # format and store games
            for i_game, game in tqdm(
                enumerate(games), total=len(games), desc=f"  Scraping games for user {username}"
            ):
                if len(user_info["games"]) >= MAX_GAMES_PER_USER:
                    break
                formatted_game = LichessInterface.format_lichess_game(game, OPENINGS)
                # saving games to mongodb
                game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                    formatted_game, collection_games
                )
                # add essential game info to player data list
                user_info["games"].append(LichessInterface.format_lichess_game_essentials(game_mongo_id, formatted_game, False))

            # placeholders to reach max document size
            if len(user_info["games"]) < MAX_GAMES_PER_USER:
                user_info["buffered_games"] = len(user_info["games"])
                for i in range(0, MAX_GAMES_PER_USER - len(user_info.get("games"))):
                    user_info["games"].append(LichessInterface.format_lichess_game_essentials(None, None, True))
            else:
                user_info["buffered_games"] = MAX_GAMES_PER_USER

            # format player info
            # store player info to MongoDB
            user_info_formatted = LichessInterface.format_lichess_player_infos(user_info, COUNTRIES)
            if user_info_formatted is None:
                continue

            user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(user_info_formatted, collection_users)
            # insert user in neo4j
            neo4j_dr.insert_user_entity(str(user_mongo_id), user_info_formatted.get("username"))
            # connect user to team in neo4j
            user_info_formatted["club"] = team_info.get("name")
            player_essentials = LichessInterface.format_lichess_player_essentials(user_info_formatted)
            neo4j_dr.connect_user_club(str(user_mongo_id), str(team_mongo_id), player_essentials)
            

            if count_tournaments_team >= max_tournaments_per_team:
                print(f"  Reached max tournaments per team ({max_tournaments_per_team}), skipping remaining tournaments for team {team}...")
                break

            # get n tournaments played by user
            tournaments = LichessInterface.get_n_lichess_player_tournaments(username, TOURNAMENTS_PER_USER)
            # store tournaments to MongoDB
            print(f"  Tournaments found for user {username}: {len(tournaments)}")
            for tournament in tournaments:
                tour_games = []
                # get games for the tournament
                player_rank = tournament["player"]["rank"]
                tournament_id = tournament["tournament"]["id"]
                if tournament_id:
                    # get infos
                    tour_infos = LichessInterface.get_lichess_tournament_infos_with_players(tournament_id, NUM_PARTICIPANTS)
                    if not tour_infos:
                        #print(f"    No data for tournament id: {tournament_id}, skipping...")
                        continue
                    
                    count_tournaments_team += 1
                    tour_infos["buffered_games"] = 0
                    # get all games played in the tournament
                    max_games_per_tournament = 150
                    tour_games = LichessInterface.get_lichess_tournament_games_all(tournament_id, max_games_per_tournament)
                    formatted_games = []
                    for i, game in enumerate(tqdm(tour_games, desc=f"    Scraping all games for tournament {tournament_id}")):
                        game_formatted = LichessInterface.format_lichess_game(game, OPENINGS)
                        game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(game_formatted, collection_games)
                        formatted_games.append(LichessInterface.format_lichess_game_essentials(game_mongo_id, game_formatted, False))
                        tour_infos["buffered_games"] += 1

                    # get formatted infos
                    infos_formatted = LichessInterface.format_lichess_tournament_info(tour_infos)
                    infos_formatted["games"] = formatted_games
                    # max_games_per_tournament = GAMES_PER_TOURNAMENT * NUM_PARTICIPANTS
                    if len(infos_formatted["games"]) < max_games_per_tournament:
                        for i in range(0, max_games_per_tournament - len(infos_formatted["games"])):
                            infos_formatted["games"].append(LichessInterface.format_lichess_game_essentials(None, None, True))

                    player_names = tour_infos["players"]
                    infos_formatted.pop("players")
                    total_games = infos_formatted["total_games"]
                    number_participants = infos_formatted["number_participants"]
                    whiteWins = infos_formatted["whiteWins"]
                    blackWins = infos_formatted["blackWins"]
                    infos_formatted.pop("total_games")
                    infos_formatted.pop("whiteWins")
                    infos_formatted.pop("blackWins")
                    infos_formatted.pop("number_participants", None)

                    infos_formatted["max_participant"] = NUM_PARTICIPANTS
                    # store tournament to MongoDB
                    tournament_mongo_id = mongo_db_interface.store_dict_to_MongoDB(infos_formatted, collection_tournament)
                    # insert tournament in neo4j
                    neo4j_dr.insert_tournament_entity(str(tournament_mongo_id), infos_formatted.get("name"))
                    # estimate wins, losses, draws
                    
                    # if len(players) < NUM_PARTICIPANTS: user is set to last position
                    if len(player_names) < NUM_PARTICIPANTS:
                        player_rank = len(player_names)

                    wins, losses, draws = LichessInterface.estimate_player_stats(total_games, number_participants, whiteWins,blackWins, player_rank)
                    # connect user to tournament in neo4j
                    neo4j_dr.connect_user_tournament(str(user_mongo_id), str(tournament_mongo_id), tournament_user_stats={"placement": player_rank, "wins": wins, "losses": losses, "draws": draws})

                    ## here it's possible to fetch and store also other participants of the tournament, but you would need to 
                    ## use the url https://lichess.org/api/tournament/{tournament_id} and set a new page number, look at
                    ## the get_lichess_tournament_infos_with_players method in LichessInterface.py for reference
                    # get a list of participants
                    #player_names = tour_infos["players"]
                    simplified_players = [{"name": p["name"], "rank": p["rank"]} for p in player_names]
                    
                    # get only 
                    participant_names = [p["name"] for p in simplified_players if p["name"] != username]

                    # get infos
                    participants_infos = LichessInterface.get_lichess_player_infos_by_list_ids(participant_names)
                    
                    # get infos about each participant using get_lichess_player_infos_by_list_ids
                    for p_info in participants_infos:
                        p_tour_games = []
                        if not p_info:
                            print(f"    No data for user id: {p_info}, skipping...")
                        else:
                            p_info_formatted = LichessInterface.format_lichess_player_infos(p_info, COUNTRIES)
                            if p_info_formatted is None:
                                continue
                            games = LichessInterface.get_lichess_games(p_info_formatted.get("username"), GAMES_PER_USER)
                            p_info_formatted["games"] = []
                            for i_game, game in tqdm(enumerate(games), total=len(games), desc=f"    Scraping games for tournament participant {p_info_formatted.get('username')}"):
                                if len(p_info_formatted["games"]) >= MAX_GAMES_PER_USER:
                                    break
                                formatted_game = LichessInterface.format_lichess_game(game, OPENINGS)
                                # saving games to mongodb
                                game_mongo_id = mongo_db_interface.store_dict_to_MongoDB(
                                    formatted_game, collection_games
                                )
                                # add essential game info to player data list
                                p_info_formatted["games"].append(LichessInterface.format_lichess_game_essentials(game_mongo_id, formatted_game, False))
                            
                            # user can also not be in a team
                            p_team_mongo_id = random.choice(team_keys)
                            p_team_name = random_teams_dict.get(p_team_mongo_id)

                            

                            # placeholders to reach max document size
                            if len(p_info_formatted["games"]) < MAX_GAMES_PER_USER:
                                p_info_formatted["buffered_games"] = len(p_info_formatted["games"])
                                for i in range(0, MAX_GAMES_PER_USER - len(p_info_formatted.get("games"))):
                                    p_info_formatted["games"].append(LichessInterface.format_lichess_game_essentials(None, None, True))
                            else:
                                p_info_formatted["buffered_games"] = MAX_GAMES_PER_USER

                            current_username= p_info_formatted.get("username")
                            placement = None
                            for p in simplified_players:
                                if p["name"] == current_username:
                                    placement = p["rank"]
                                    break
                                
                            # store participant info to MongoDB
                            p_user_mongo_id = mongo_db_interface.store_dict_to_MongoDB(p_info_formatted, collection_users)
                            # insert user in neo4j
                            neo4j_dr.insert_user_entity(str(p_user_mongo_id), p_info_formatted.get("username"))
                            # estimate wins, losses, draws
                            wins, losses, draws = LichessInterface.estimate_player_stats(total_games,number_participants , whiteWins, blackWins, placement)
                            # connect user to tournament in neo4j
                            neo4j_dr.connect_user_tournament(str(p_user_mongo_id), str(tournament_mongo_id), tournament_user_stats={"placement": placement, "wins": wins, "losses": losses, "draws": draws})
                            
                            p_info_formatted["club"] = p_team_name
                            # connect user to team in neo4j
                            player_essentials = LichessInterface.format_lichess_player_essentials(p_info_formatted)
                            if p_team_mongo_id is not None:
                                neo4j_dr.connect_user_club(str(p_user_mongo_id), str(p_team_mongo_id), player_essentials)

        

    print("Finished processing all teams.")