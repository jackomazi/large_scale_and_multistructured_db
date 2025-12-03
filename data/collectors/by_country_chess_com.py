import json
import os
import sys

from tqdm import tqdm

from api.chess_com import chess_com_interface

if __name__ == "__main__":
    scraping_values = {}
    script_dir = os.path.dirname(os.path.abspath(__file__))
    config_path = os.path.join(script_dir, "..", "config.json")
    with open(config_path, "r") as config:
        data = json.load(config)
    scraping_values["max_users_per_country"] = data.get("max_users_per_country", 10)
    scraping_values["countries"] = data.get("countries")

    max_users = scraping_values["max_users_per_country"]

    for country in scraping_values["countries"]:
        print(f"Processing country: {country}")

        player_usernames = chess_com_interface.get_country_players(country)

        if not player_usernames:
            print(f"No players found for {country} or error fetching data.")
            continue

        player_data_list = []

        # Limit the number of users to process
        users_to_process = player_usernames[:max_users]

        for username in tqdm(
            users_to_process, desc=f"Fetching player data for {country}"
        ):
            player_info = chess_com_interface.get_player_infos(username)
            # Add key property to store user game stats
            player_info["stats"] = chess_com_interface.get_player_games_stats(username)
            if player_info:
                player_data_list.append(player_info)

        if not player_data_list:
            print(f"No player data collected for {country}.")
            continue

        dumps_dir = os.path.join(os.path.dirname(script_dir), "dumps")
        os.makedirs(dumps_dir, exist_ok=True)
        output_path = os.path.join(dumps_dir, f"{country}_players.json")

        try:
            with open(output_path, "w") as f:
                json.dump(player_data_list, f, indent=4)
            print(
                f"Successfully saved {len(player_data_list)} players' data to {output_path}"
            )
        except IOError as e:
            print(f"Error saving data for country {country}: {e}")

    print("Finished processing all countries.")
