import requests
from pymongo import MongoClient
import sys

#Using Andrea work
import scraping

# Scaping player usernames from clubs
def get_players_usernames(club : str) -> list:
    # Standard api call
    url = f"https://api.chess.com/pub/club/{club}/members"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/130.0.0.0 Safari/537.36",
        "Accept": "application/json",
    }
    response = requests.get(url,headers=headers)

    # Return all players user names
    usernames = []
    for element in response.json().get("all_time"):
        usernames.append(element.get("username"))
    return usernames

# Scraping player infos
def get_player_infos(username: str) -> dict:
    # Standard api call
    url = f"https://api.chess.com/pub/player/{username}"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/130.0.0.0 Safari/537.36",
        "Accept": "application/json",
    }

    response = requests.get(url,headers=headers)
    return response.json()

# Scraping player games archives
def get_player_games_archives(username: str) -> list:
    # Standard api call
    url = f"https://api.chess.com/pub/player/{username}/games/archives"
    headers = {
        "User-Agent": "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                      "AppleWebKit/537.36 (KHTML, like Gecko) "
                      "Chrome/130.0.0.0 Safari/537.36",
        "Accept": "application/json",
    }
    response = requests.get(url,headers=headers)

    return response.json().get("archives")

# Storing game to MongoDB
def store_games_to_MongoDB(game: dict, collection):
    collection.replace_one(
        {"_id": game["_id"]}, 
        game,                 
        upsert=True)
    
# Storing user to MongoDB
def store_user_to_MongoDB(user: dict, collection):
    collection.replace_one(
        {"_id": user["_id"]}, 
        user,                 
        upsert=True
    )

# Scraping values
scraping_values = {
    "max_scrap_archives": 3,
    "max_scrap_users_per_club": 10,
    "max_scrap_games_per_archive": 20
}


if __name__ == "__main__":
    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()
        db = client["chess_db_test"]
        collection_users = db["users_isaia"]
        collection_games = db["games_isaia"]
    except:
        sys.exit(1)

    # Most popular clubs with a large amount of players
    clubs = ["india-11"]

    for club in clubs:
        # Club scraping
        usernames = get_players_usernames(club)
        # Users scraping
        for i_user, user in enumerate(usernames):
            user_info = get_player_infos(user)
            user_archives = get_player_games_archives(user)
            # Slight variation to user object
            # Rename id property complying to mongo DB specification
            user_info["_id"] = user_info.pop("@id")
            # Add key property to store user players ids
            user_info["games"] = []
        
            if i_user >= scraping_values.get("max_scrap_users_per_club"):
                break

            # Archives scraping
            for i_archive, archive_url in enumerate(user_archives):
                games = scraping.fetch_chess_com_games_v2(archive_url)
                #If the archive is empty jump ahead
                if games is []:
                    continue

                # Formating
                formatted_games = []
                for i_game, game in enumerate(games):
                    formatted_game = scraping.format_chess_com_game(game)
                    # Rename id property complying to mongo DB specification
                    formatted_game["_id"] = formatted_game["url"]
                    formatted_games.append(formatted_game)

                    # Add id of game to user games
                    user_info["games"].append(formatted_game["_id"])

                    # Saving games to mongoDB
                    store_games_to_MongoDB(formatted_game,collection_games)

                    if i_game >= scraping_values.get("max_scrap_games_per_archive"):
                        break

                if i_archive >= scraping_values.get("max_scrap_archives"):
                    break

            #Saving user to mongoDB
            store_user_to_MongoDB(user_info,collection_users)


            
            
            
        

