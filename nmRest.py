import json
import logging
import logging.config
import os
import sys
import time  # potential rate limiting

import requests
from dotenv import load_dotenv

API_BASE_URL = "https://api.nexusmods.com/v1"
MAX_GAMES_TO_FETCH = 10

logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s - %(levelname)s - %(message)s",
    handlers=[
        logging.FileHandler("logs.log", mode="a", encoding="utf-8"),
        logging.StreamHandler(),
    ],
)

logger = logging.getLogger(__name__)


def get_headers(api_key):
    """Helper function to create the required API headers."""
    return {
        "apikey": api_key,
        "Content-Type": "application/json",
        "Accept": "application/json",
    }


def get_game_list(api_key):
    """
    Fetches the list of all supported games from the Nexus Mods API.
    """
    headers = get_headers(api_key)
    if not headers:
        return None

    url = f"{API_BASE_URL}/games.json"
    logger.info(f"Querying API for game list: {url}")

    try:
        response = requests.get(url, headers=headers)

        if response.status_code == 200:
            logger.info("Successfully retrieved game list.")
            return response.json()
        else:
            logger.error(f"Error: Received status code {response.status_code}")
            logger.info(f"Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        logger.error(f"An error occurred during the API request: {e}")
        return None


def get_latest_mods(game_domain, api_key):
    """
    Fetches a list of the latest added mods for a specific game.
    """
    headers = get_headers(api_key)
    if not headers:
        return None

    # Use the 'trending' endpoint to get the top 10 trending mods
    url = f"{API_BASE_URL}/games/{game_domain}/mods/trending.json"
    logger.info(f"Querying for latest mods in: {game_domain}")

    try:
        response = requests.get(url, headers=headers)

        if response.status_code == 200:
            logger.info(f"  > Success: Found latest mods for {game_domain}.")
            return response.json()

        else:
            logger.info(f"  > Error: Received status code {response.status_code}")
            logger.info(f"    Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        logger.info(f"An error occurred during the API request: {e}")
        return None


def get_mod_details(game_domain, mod_id, api_key):
    """
    Fetches details for a specific mod from the Nexus Mods API.
    """
    headers = get_headers(api_key)
    if not headers:
        return None

    url = f"{API_BASE_URL}/games/{game_domain}/mods/{mod_id}.json"
    logger.info(f"    Querying API for: {game_domain} mod {mod_id}")

    try:
        response = requests.get(url, headers=headers)

        if response.status_code == 200:
            logger.info(f"      > Success for mod {mod_id}.")
            return response.json()

        else:
            logger.error(f"    > Error: Received status code {response.status_code}")
            logger.error(f"      Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        logger.error(f"An error occurred during the API request: {e}")
        return None


def main():
    """
    Main function to fetch and save mod data.
    """
    # Load environment variables from .env file
    load_dotenv()

    API_KEY = os.getenv("NEXUS_API_KEY")
    if not API_KEY:
        logger.error("Error: API key is not set or is still the placeholder.")
        logger.error("Please create a .env file, add your API key as NEXUS_API_KEY,")
        logger.error(
            "and get your key from: https://www.nexusmods.com/users/myaccount?tab=api"
        )
        sys.exit(1)

    output_data = {}
    output_path = r"dumps/nexus_mod_data.json"

    logger.info("--- Starting Mod Data Fetch ---")

    all_games = get_game_list(API_KEY)

    if not all_games:
        logger.error("Failed to retrieve game list. Exiting.")
        sys.exit(1)

    # sort by 'downloads' to get more popular games first
    games_to_process = sorted(all_games, key=lambda g: g["downloads"], reverse=True)[
        :MAX_GAMES_TO_FETCH
    ]

    logger.info(f"\nWill process the first {len(games_to_process)} games (A-Z).")

    for game in games_to_process:
        game_domain = game.get("domain_name")
        game_name = game.get("name", game_domain)

        if not game_domain:
            continue

        logger.info(f"\n--- Processing game: {game_name} ({game_domain}) ---")
        output_data[game_domain] = {"game_info": game, "mods": []}
        latest_mods = get_latest_mods(game_domain, API_KEY)

        if not latest_mods:
            logger.error(f"  > No mods found or error for {game_domain}. Skipping.")
            continue

        for mod_summary in latest_mods:
            mod_id = mod_summary.get("mod_id")
            if not mod_id:
                continue

            # time.sleep(0.1)
            mod_data = get_mod_details(game_domain, mod_id, API_KEY)

            if mod_data:
                output_data[game_domain]["mods"].append(mod_data)

    try:
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(output_data, f, ensure_ascii=False, indent=4)
        logger.info(f"All mod data has been saved to {output_path}")

    except IOError as e:
        logger.error(f"Could not write to file {output_path}: {e}")
    except Exception as e:
        logger.error(f"An unexpected error occurred while writing JSON: {e}")


if __name__ == "__main__":
    main()
