import json
import logging
import os

import requests
from dotenv import load_dotenv

GRAPHQL_API_URL = "https://api.nexusmods.com/v2/graphql"
MAX_GAMES_TO_FETCH = 10
MAX_MODS_PER_GAME = 20

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
    return {
        "apikey": api_key,
        "Content-Type": "application/json",
        "Accept": "application/json",
        "User-Agent": "NexusModsGraphQLPython/1.0",
    }


def fetch_graphql_data(api_key, query, variables):
    headers = get_headers(api_key)
    payload = {"query": query, "variables": variables}

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
            return data.get("data")

        elif response.status_code == 401:
            logger.error(f"Authentication failed (Status {response.status_code}).")
            return None
        else:
            logger.error(f"Error: Received status code {response.status_code}")
            logger.error(f"Response: {response.text}")
            return None

    except requests.exceptions.RequestException as e:
        logger.error(f"An error occurred during the API request: {e}")
        return None


def main():
    load_dotenv()
    API_KEY = os.getenv("NEXUS_API_KEY")
    if not API_KEY:
        logger.error("Error: NEXUS_API_KEY is not set.")
        return

    output_path = r"dumps/nexus_mod_data_graphql.json"
    os.makedirs(os.path.dirname(output_path), exist_ok=True)

    logger.info("--- Starting Mod Data Fetch (GraphQL) ---")

    game_query_string = """
    query GetGames($count: Int) {
      games(
        sort: [{ downloads: { direction: DESC } }],
        count: $count
      ) {
        nodes {
          id
          name
          domainName
        }
      }
    }
    """

    game_query_variables = {"count": MAX_GAMES_TO_FETCH}

    logger.info("Step 1: Fetching top games list...")
    game_api_data = fetch_graphql_data(API_KEY, game_query_string, game_query_variables)

    if not game_api_data or not game_api_data.get("games"):
        logger.error("Failed to retrieve game list. Exiting.")
        return

    game_nodes = game_api_data.get("games", {}).get("nodes", [])
    logger.info(f"Successfully fetched {len(game_nodes)} games.")

    mod_query_string = """
    query GetModsForGame($gameDomain: String!, $count: Int) {
      mods(
        filter: {
            gameDomainName: [{ value: $gameDomain, op: EQUALS }]
        },
        sort: [{ downloads: { direction: DESC } }],
        count: $count
      ) {
        nodes {
          modId
          name
          summary
          version
          author
          pictureUrl
          createdAt
          updatedAt
          endorsements
        }
      }
    }
    """

    output_data = {}

    for game_info in game_nodes:
        game_domain = game_info.get("domainName")
        if not game_domain:
            continue

        logger.info(f"Step 2: Fetching top mods for {game_info.get('name')}...")

        mod_query_variables = {
            "gameDomain": game_domain,
            "count": MAX_MODS_PER_GAME,
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
    except Exception as e:
        logger.error(f"Error writing JSON: {e}")


if __name__ == "__main__":
    main()
