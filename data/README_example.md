# API Documentation and Data Strategy

This document describes the APIs used for data collection, the specific endpoints targeted, the raw data structure, and the integration strategy.

## Sources & Endpoints

We utilize specific endpoints from Chess.com and Lichess to build our dataset. Below is the detailed breakdown of each endpoint, an example of the raw JSON response, and the fields we extract.

### 1. Chess.com API

**Base URL**: `https://api.chess.com`

#### A. Club Members
*   **Endpoint**: `/pub/club/{club-name}/members`
*   **Purpose**: To discover usernames of players belonging to specific clubs (e.g., "team-italy").
*   **Example Dump**:
    ```json
    {
      "weekly": [],
      "monthly": [],
      "all_time": [
        {
          "username": "erik",
          "joined": 1234567890
        },
        {
          "username": "hikaru",
          "joined": 1234567891
        }
      ]
    }
    ```
*   **Useful Fields**:
    *   `all_time`: A list of objects containing members.
    *   `username`: The unique identifier for the player.

#### B. Player Profile
*   **Endpoint**: `/pub/player/{username}`
*   **Purpose**: To retrieve demographic and profile information about a user.
*   **Example Dump**:
    ```json
    {
      "player_id": 41,
      "id": "https://api.chess.com/pub/player/erik",
      "url": "https://www.chess.com/member/erik",
      "name": "Erik Allebest",
      "username": "erik",
      "followers": 1650,
      "country": "https://api.chess.com/pub/country/US",
      "location": "Palo Alto",
      "last_online": 1618335031,
      "joined": 1157648351,
      "status": "premium",
      "is_streamer": false
    }
    ```
*   **Useful Fields**:
    *   `player_id`: Internal unique ID.
    *   `name`: Real name (if public).
    *   `username`: The handle used for linking to games.
    *   `country`: URL to the country resource (we extract the code, e.g., "US").
    *   `joined`: Timestamp of account creation.

#### C. Player Stats
*   **Endpoint**: `/pub/player/{username}/stats`
*   **Purpose**: To get current ratings across different chess categories (Blitz, Rapid, Bullet).
*   **Example Dump**:
    ```json
    {
      "chess_daily": {
        "last": { "rating": 1056, "date": 1618335031 },
        "best": { "rating": 1200, "date": 1518335031 }
      },
      "chess_rapid": {
        "last": { "rating": 1500, "date": 1618335031 },
        "best": { "rating": 1600, "date": 1618335031 }
      },
      "chess_bullet": {
        "last": { "rating": 900, "date": 1618335031 }
      }
    }
    ```
*   **Useful Fields**:
    *   `chess_rapid.last.rating`: Current Rapid rating.
    *   `chess_blitz.last.rating`: Current Blitz rating.
    *   `chess_bullet.last.rating`: Current Bullet rating.
    *   Note: We prefer `best.rating` if available, otherwise `last.rating`.

#### D. Player Game Archives
*   **Endpoint**: `/pub/player/{username}/games/archives`
*   **Purpose**: Returns a list of URLs, where each URL points to a month's worth of games.
*   **Example Dump**:
    ```json
    {
      "archives": [
        "https://api.chess.com/pub/player/erik/games/2021/01",
        "https://api.chess.com/pub/player/erik/games/2021/02"
      ]
    }
    ```
*   **Useful Fields**:
    *   `archives`: List of strings (URLs) to iterate over for game collection.

#### E. Monthly Games (Archive)
*   **Endpoint**: (Retrieved from Archives list, e.g., `/pub/player/{username}/games/2021/01`)
*   **Purpose**: Contains the actual game data for a specific month.
*   **Example Dump**:
    ```json
    {
      "games": [
        {
          "url": "https://www.chess.com/game/live/123456",
          "pgn": "[Event \"Live Chess\"] [Site \"Chess.com\"] ... 1. e4 e5 ...",
          "time_control": "600",
          "end_time": 1612137600,
          "rated": true,
          "time_class": "rapid",
          "white": { "username": "erik", "rating": 1500, "result": "win" },
          "black": { "username": "opponent", "rating": 1450, "result": "checkmated" },
          "eco": "https://www.chess.com/openings/King-Pawn-Game"
        }
      ]
    }
    ```
*   **Useful Fields**:
    *   `url`: Unique game link (used as ID).
    *   `pgn`: Portable Game Notation (we parse this to extract the move list).
    *   `white` / `black`: Objects containing `username`, `rating`, and `result`.
    *   `time_class`: Helps categorize the game (blitz, rapid, bullet).
    *   `eco`: Opening classification URL (we extract the opening name/code).

---

### 2. Lichess API

**Base URL**: `https://lichess.org`

#### A. Team Users
*   **Endpoint**: `/api/team/{teamId}/users`
*   **Purpose**: Returns a stream of members belonging to a team.
*   **Format**: NDJSON (Newline Delimited JSON)
*   **Example Dump** (One line):
    ```json
    {
      "id": "george",
      "username": "George",
      "perfs": { "blitz": { "rating": 1800, "games": 200 } },
      "createdAt": 1357020000000,
      "seenAt": 1618335031000
    }
    ```
*   **Useful Fields**:
    *   `username` (or `id`): Extracted to build the list of players to scrape.

#### B. User Games
*   **Endpoint**: `/api/games/user/{username}`
*   **Parameters**: `max={n}`, `format=ndjson`, `opening=true`, `pgnInJson=true`
*   **Purpose**: Streams the game history of a specific user.
*   **Format**: NDJSON
*   **Example Dump** (One line):
    ```json
    {
      "id": "gameId123",
      "rated": true,
      "speed": "blitz",
      "createdAt": 1612137600000,
      "status": "mate",
      "players": {
        "white": { "user": { "name": "George", "id": "george" }, "rating": 1800 },
        "black": { "user": { "name": "Opponent", "id": "opponent" }, "rating": 1750 }
      },
      "winner": "white",
      "opening": { "eco": "C50", "name": "Italian Game" },
      "moves": "e4 e5 Nf3 Nc6 Bc4"
    }
    ```
*   **Useful Fields**:
    *   `id`: Game ID (used to construct the URL `https://lichess.org/{id}`).
    *   `players`: Nested structure to get names and ratings.
    *   `winner`: "white", "black", or null (draw).
    *   `opening`: Contains `eco` code and full `name`.
    *   `moves`: Space-separated string of moves (easier to use than raw PGN).
    *   `speed`: Maps to `time_class` (e.g., blitz, rapid).

## Modeling

### Unified Data Model (MongoDB)

We transform data from both sources into this unified structure:

**User Collection**:
```json
{
    "_id": "normalized_username",
    "username": "OriginalUsername",
    "country": "US",
    "stats": {
        "blitz": 1500,
        "rapid": 1600,
        "bullet": 1400
    },
    "source": "chess.com" // or "lichess"
}
```

**Games Collection**:
```json
{
    "_id": "https://www.chess.com/game/live/123456",
    "url": "https://www.chess.com/game/live/123456",
    "white_player": "erik",
    "black_player": "opponent",
    "white_rating": 1500,
    "black_rating": 1450,
    "result_white": "win",
    "result_black": "loss",
    "opening": "King's Pawn Game",
    "moves": "e4 e5 Nf3 ...",
    "time_class": "rapid",
    "rated": true,
    "end_time": "2021-02-01T00:00:00.000Z"
}
```

## Join Strategy

1.  **Normalization**:
    *   **Moves**: Chess.com PGNs are stripped of timestamps and comments to match the simple move string from Lichess.
    *   **Results**: Chess.com's granular results (e.g., "agreed", "repetition") are mapped to simple "win"/"loss"/"draw" unless specific detail is required.
    *   **Dates**: All timestamps are converted to ISO 8601 strings or MongoDB Date objects.

2.  **Deduplication**:
    *   We use the Game URL as the unique `_id`. Since cross-platform games don't exist, this naturally avoids duplicates between sources.
