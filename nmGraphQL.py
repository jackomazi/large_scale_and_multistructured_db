import json
import logging
import logging.config
import os
import sys
import traceback

import requests
from dotenv import load_dotenv

# Base URL for the Nexus Mods GraphQL API v2
GRAPHQL_API_URL = "https://api.nexusmods.com/v2/graphql"
MAX_GAMES_TO_FETCH = 10
MAX_MODS_PER_GAME = 5

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


def fetch_graphql_data(api_key, query, variables):
    """
    Fetches data from the Nexus Mods GraphQL API.
    """
    headers = get_headers(api_key)
    payload = {"query": query, "variables": variables}

    logger.info(f"Querying GraphQL API: {GRAPHQL_API_URL}")

    try:
        response = requests.post(
            GRAPHQL_API_URL, headers=headers, json=payload, timeout=30
        )

        if response.status_code == 200:
            data = response.json()

            if data.get("errors"):
                logger.error("GraphQL API returned errors:")
                for error in data["errors"]:
                    logger.error(f"  - {error.get('message')}")
                return None

            logger.info("Successfully retrieved data from GraphQL API.")
            return data.get("data")

        elif response.status_code == 401:
            logger.error(
                f"Error: Authentication failed (Status {response.status_code})."
            )
            logger.error("Please check if your API key is correct and valid.")
            return None
        else:
            logger.error(f"Error: Received status code {response.status_code}")
            logger.error(f"Response: {response.text}")
            return None

    except requests.exceptions.Timeout:
        logger.error("An error occurred during the API request: Request timed out.")
        return None
    except requests.exceptions.RequestException as e:
        logger.error(f"An error occurred during the API request: {e}")
        return None


def main():
    """
    Main function to fetch and save mod data using GraphQL.
    """
    load_dotenv()

    API_KEY = os.getenv("NEXUS_API_KEY")
    if not API_KEY:
        logger.error("Error: API key is not set or is still the placeholder.")
        return

    # Define the output path and ensure the directory exists
    output_path = r"dumps/nexus_mod_data_graphql.json"
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    # --- Query 1: Get the list of top games ---
    game_query_string = """
    query GetGames($maxGames: Int!) {
      games() {
        nodes {
          id
          name
          domainName
          gameStats {
            totalDownloads
            totalFiles
          }
        }
      }
    }
    """
    game_query_variables = {"maxGames": MAX_GAMES_TO_FETCH}

    logger.info("--- Starting Mod Data Fetch (GraphQL) ---")
    logger.info("Step 1: Fetching game list...")

    # Make the *first* API call to get games
    game_api_data = fetch_graphql_data(API_KEY, game_query_string, game_query_variables)

    if not game_api_data or not game_api_data.get("games"):
        logger.error("Failed to retrieve game list from the GraphQL API. Exiting.")
        return

    game_nodes = game_api_data.get("games", {}).get("nodes", [])

    if not game_nodes:
        logger.warning("API call succeeded but returned no games.")
        return

    logger.info(f"Successfully fetched {len(game_nodes)} games. Now fetching mods...")

    # --- Query 2: Get mods for each game ---
    mod_query_string = """
    query GetModsForGame($gameDomain: String!, $maxMods: Int!) {
      mods(
        gameDomainName: $gameDomain,
        sort: [LATEST_ADDED],
        first: $maxMods
      ) {
        nodes {
          modId
          name
          summary
          version
          author {
            name
          }
          pictureUrl
          updatedAt
          endorsements {
            count
          }
        }
      }
    }
    """

    output_data = {}

    for game_info in game_nodes:
        game_domain = game_info.get("domainName")
        if not game_domain:
            continue

        logger.info(f"Step 2: Fetching mods for {game_info.get('name')}...")

        mod_query_variables = {
            "gameDomain": game_domain,
            "maxMods": MAX_MODS_PER_GAME,
        }

        mod_api_data = fetch_graphql_data(
            API_KEY, mod_query_string, mod_query_variables
        )

        mods_list = []
        if mod_api_data and mod_api_data.get("mods"):
            mods_list = mod_api_data.get("mods", {}).get("nodes", [])
            logger.info(f"  > Found {len(mods_list)} mods.")
        else:
            logger.warning(f"  > Could not retrieve mods for {game_domain}.")

        output_data[game_domain] = {"game_info": game_info, "mods": mods_list}

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
