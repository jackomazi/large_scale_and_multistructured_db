# Data collection

The data collection required to populate the MongoDB database with the required user data and associated games information is done through Python scripts.
Any methods used to interface with Chess.com and Lichess.com are contained in the class declaration Python files in the "api" directory.

## API Wrapper

This directory contains wrappers for the public APIs of `chess.com` and `lichess.org`. Each module provides a class with static methods to interact with the respective APIs and format the obtained data.

### Chess.com API (`chess_com.py`)

The `chess_com.py` file contains the `chess_com_interface` class which exposes methods for querying the chess.com API.

#### Main Methods

- `get_players_usernames(club: str) -> list`:
  Retrieves a list of usernames of players registered in a specific club.

- `get_player_infos(username: str) -> dict`:
  Gets the profile information of a specific player.

- `get_player_games_archives(username: str) -> list`:
  Returns the list of URLs of a player's game archives.

- `get_chess_com_games(url: str) -> list`:
  Extracts games from a given archive URL.

- `format_chess_com_game(game: dict) -> dict`:
  Formats the data of a single game into a structured dictionary, normalizing the fields for greater consistency.

- `get_player_games_stats(username: str) -> dict`:
  Retrieves a player's stats and formats them to be compatible with Lichess data.

- `format_chess_com_player_stats(stats: dict) -> dict`:
  A helper method to format statistics, focusing on the main game modes (bullet, blitz, rapid).

- `get_country_players(country_code: str) -> list`:
  Gets a list of players from a specific country code (e.g., 'IT' for Italy).

### Lichess API (`lichess.py`)

The `lichess.py` file contains the `lichess_interface` class for interacting with the lichess.org API.

#### Main Methods

- `get_players_usernames(team: str) -> list`:
  Retrieves the usernames of players belonging to a Lichess team.

- `get_player_infos(username: str) -> dict`:
  Gets the profile information of a Lichess user.

- `get_lichess_games(user: str, n: int) -> list`:
  Retrieves the last `n` games played by a user, in NDJSON format.

- `format_lichess_game(game: dict) -> dict`:
  Converts game data from the Lichess format to a structured dictionary, similar to the one used for chess.com, to facilitate integration.

- `get_teams_list(page: int) -> list`:
  Retrieves a paginated list of teams from Lichess.

- `get_n_users_from_team(team: str, n: int) -> list`:
  Extracts `n` usernames from a specific team.

#### Config.json

- Collection by club name: 
    - "clubs": Name of clubs to collect data from.
    - "max_scrap_users_per_club": Maximum number of user to process per club (always the first n).
    - "max_scrap_archives": Users games data are organized in "archives" based on the date of the games, is the maximum number of archives to compute per user.
    - "max_scrap_games_per_archive": Maximum games to compute per archive.

### Chess.com data collection
The Python script file [by_club_chess_com.py](data/collectors/by_club_chess_com.py),
[by_country_chess_com.py](data/collectors/by_country_chess_com.py) and [by_player_chess_com.py](data/collectors/by_player_chess_com.py) will collect a tot. amount of user data and games data per user according to the [config.json](data/config.json) file.

- `get_player_usernames`-> "https://api.chess.com/pub/club/{club}/members":
```json
{
  "weekly": [],
  "monthly": [
    {
      "username": "kishore2801",
      "joined": 1764151871
    },
    {
      "username": "ridhima_25",
      "joined": 1763678225
    }
  ],
  "all_time": [
    {
      "username": "00sonal1234",
      "joined": 1462300054
    },
    {
      "username": "01990saurabh",
      "joined": 1482999290
    },
    {
      "username": "120192u91j2heoinks-jasjha",
      "joined": 1757258733
    },
    {
      "username": "123raksh",
      "joined": 1671713591
    },
    {
      "username": "1magnuscarlsen2",
      "joined": 1465633006
    }
  ]
}
```

#### Useful fields:
We just take the "**all_time**" field and create an array of usernames taking the "**username**" field from the object within.

- `get_player_infos`-> "https://api.chess.com/pub/player/{username}":
```json
{
  "avatar": "https://images.chesscomfiles.com/uploads/v1/user/13697762.dc1e21aa.200x200o.dd8e0600e6df.png",
  "player_id": 13697762,
  "@id": "https://api.chess.com/pub/player/00sonal1234",
  "url": "https://www.chess.com/member/00sonal1234",
  "name": "kittu kapil",
  "username": "00sonal1234",
  "followers": 1,
  "country": "https://api.chess.com/pub/country/IN",
  "location": "delhi",
  "last_online": 1484205470,
  "joined": 1380377504,
  "status": "basic",
  "is_streamer": false,
  "verified": false,
  "streaming_platforms": []
}
```

#### Useful fields:
We take everything as it's the base for the user json in our database.

- `get_player_games_archives` -> "https://api.chess.com/pub/player/{username}/games/archives":

```json
{
  "archives": [
    "https://api.chess.com/pub/player/00sonal1234/games/2013/09",
    "https://api.chess.com/pub/player/00sonal1234/games/2014/02",
    "https://api.chess.com/pub/player/00sonal1234/games/2014/03"
  ]
}
```

#### Useful fields:
We take the array "**archives**" that contains the urls of the player chess games organized in month and year.

- `get_chess_com_games` -> "https://api.chess.com/pub/player/00sonal1234/games/2013/09":

```json
{
  "games": [
    {
      "url": "https://www.chess.com/game/live/610659145",
      "pgn": "[Event \"Live Chess\"]\n[Site \"Chess.com\"]\n[Date \"2013.09.28\"]\n[Round \"-\"]\n[White \"00sonal1234\"]\n[Black \"pawnchewer\"]\n[Result \"1-0\"]\n[CurrentPosition \"2k4r/ppp3pp/8/2N1p3/8/bPKQPP2/P1P3q1/r7 b - -\"]\n[Timezone \"UTC\"]\n[ECO \"A01\"]\n[ECOUrl \"https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5\"]\n[UTCDate \"2013.09.28\"]\n[UTCTime \"14:14:03\"]\n[WhiteElo \"1326\"]\n[BlackElo \"1070\"]\n[TimeControl \"180\"]\n[Termination \"00sonal1234 won on time\"]\n[StartTime \"14:14:03\"]\n[EndDate \"2013.09.28\"]\n[EndTime \"14:18:40\"]\n[Link \"https://www.chess.com/game/live/610659145\"]\n\n1. b3 {[%clk 0:03:00]} 1... e5 {[%clk 0:03:00]} 2. Bb2 {[%clk 0:02:55]} 2... d5 {[%clk 0:02:57.1]} 3. e3 {[%clk 0:02:54.6]} 3... Nc6 {[%clk 0:02:44.7]} 4. g3 {[%clk 0:02:52.6]} 4... Nf6 {[%clk 0:02:40.7]} 5. Bg2 {[%clk 0:02:51.8]} 5... Bg4 {[%clk 0:02:34.6]} 6. Ne2 {[%clk 0:02:50.6]} 6... e4 {[%clk 0:02:27.6]} 7. h3 {[%clk 0:02:49.1]} 7... Bh5 {[%clk 0:02:22.9]} 8. O-O {[%clk 0:02:46.8]} 8... Qd7 {[%clk 0:02:16.4]} 9. g4 {[%clk 0:02:43.5]} 9... Nxg4 {[%clk 0:02:10]} 10. hxg4 {[%clk 0:02:41.2]} 10... Bxg4 {[%clk 0:02:04.8]} 11. Kh1 {[%clk 0:02:38.1]} 11... f6 {[%clk 0:01:58.6]} 12. d3 {[%clk 0:02:37.3]} 12... Qf7 {[%clk 0:01:54.6]} 13. dxe4 {[%clk 0:02:36]} 13... Qh5+ {[%clk 0:01:51.1]} 14. Kg1 {[%clk 0:02:35]} 14... Bxe2 {[%clk 0:01:47.3]} 15. Qe1 {[%clk 0:02:30.9]} 15... Bxf1 {[%clk 0:01:43.5]} 16. Qxf1 {[%clk 0:02:30]} 16... O-O-O {[%clk 0:01:32.8]} 17. exd5 {[%clk 0:02:27.8]} 17... Rxd5 {[%clk 0:01:28.8]} 18. Nc3 {[%clk 0:02:26.1]} 18... Rg5 {[%clk 0:01:18.8]} 19. Ne4 {[%clk 0:02:22.3]} 19... Rg4 {[%clk 0:01:07.3]} 20. f3 {[%clk 0:02:20.2]} 20... Rh4 {[%clk 0:01:03.4]} 21. Kf2 {[%clk 0:02:18.6]} 21... Rh2 {[%clk 0:00:54.2]} 22. Ke2 {[%clk 0:02:16.9]} 22... Ne5 {[%clk 0:00:38.7]} 23. Kd1 {[%clk 0:02:09.5]} 23... Qg6 {[%clk 0:00:27.8]} 24. Bxe5 {[%clk 0:02:06.9]} 24... fxe5 {[%clk 0:00:24.2]} 25. Qd3 {[%clk 0:01:46.9]} 25... Qxg2 {[%clk 0:00:17.6]} 26. Kc1 {[%clk 0:01:45.6]} 26... Rh1+ {[%clk 0:00:13.5]} 27. Kb2 {[%clk 0:01:44.4]} 27... Ba3+ {[%clk 0:00:09.9]} 28. Kc3 {[%clk 0:01:42]} 28... Rxa1 {[%clk 0:00:04.9]} 29. Nc5 {[%clk 0:01:37.7]} 1-0\n",
      "time_control": "180",
      "end_time": 1380377920,
      "rated": true,
      "tcn": "jr0KcjZJmu5Qow!Tfo6EgmKCpxENeg7ZwETExENEgh1TltZ1tC1NhgEmdemfef86CJ7JbsJMsCMEnvEFgnFpnmQKmdNUjKTKftUodcphcj9qjshaCI",
      "uuid": "ad0e125a-6344-11de-8000-000000010001",
      "initial_setup": "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1",
      "fen": "2k4r/ppp3pp/8/2N1p3/8/bPKQPP2/P1P3q1/r7 b - -",
      "time_class": "blitz",
      "rules": "chess",
      "white": {
        "rating": 1326,
        "result": "win",
        "@id": "https://api.chess.com/pub/player/00sonal1234",
        "username": "00sonal1234",
        "uuid": "e767b6aa-2847-11e3-803e-000000000000"
      },
      "black": {
        "rating": 1070,
        "result": "timeout",
        "@id": "https://api.chess.com/pub/player/pawnchewer",
        "username": "pawnchewer",
        "uuid": "0207d0e4-f471-11de-805e-000000000000"
      },
      "eco": "https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5"
    }
  ]
}
```

#### Useful fields:
The result is complicated and needs a little organizing as we take only the necessary fields cutting all the fuss: "**png**", "**time_class**", "**white**" and "**black**" for which we decompose the resulting object in more fields, "**end_time**", "**rated**" and "**eco**".

- `get_player_games_stats` -> "https://api.chess.com/pub/player/{username}/stats":

```json
{
  "chess_daily": {
    "last": {
      "rating": 1149,
      "date": 1549051027,
      "rd": 51
    },
    "best": {
      "rating": 1397,
      "date": 1461017318,
      "game": "https://www.chess.com/game/daily/128399326"
    },
    "record": {
      "win": 268,
      "loss": 316,
      "draw": 9,
      "time_per_move": 31613,
      "timeout_percent": 0
    }
  },
  "chess960_daily": {
    "last": {
      "rating": 970,
      "date": 1475997006,
      "rd": 126
    },
    "best": {
      "rating": 1454,
      "date": 1456081269,
      "game": "https://www.chess.com/game/daily/128807686"
    },
    "record": {
      "win": 5,
      "loss": 13,
      "draw": 0,
      "time_per_move": 31613,
      "timeout_percent": 0
    }
  },
  "chess_rapid": {
    "last": {
      "rating": 1054,
      "date": 1463667819,
      "rd": 263
    },
    "record": {
      "win": 0,
      "loss": 4,
      "draw": 0
    }
  },
  "chess_bullet": {
    "last": {
      "rating": 1464,
      "date": 1463666258,
      "rd": 73
    },
    "best": {
      "rating": 1800,
      "date": 1454073889,
      "game": "https://www.chess.com/game/live/1441173155"
    },
    "record": {
      "win": 31,
      "loss": 26,
      "draw": 0
    }
  },
  "chess_blitz": {
    "last": {
      "rating": 1144,
      "date": 1484228191,
      "rd": 73
    },
    "best": {
      "rating": 1429,
      "date": 1380378480,
      "game": "https://www.chess.com/game/live/1359896697"
    },
    "record": {
      "win": 366,
      "loss": 357,
      "draw": 10
    }
  },
  "tactics": {
    "highest": {
      "rating": 400,
      "date": 1380377504
    },
    "lowest": {
      "rating": 0,
      "date": 0
    }
  },
  "puzzle_rush": {

  }
}
```

#### Useful fields:
That's a lot of information, but for Lichess data compatibility (chess.com and lichess.org have different game modes classifications) we are interested only in the "**chess_blitz**", "**chess_bullet**" and "**chess_rapid**" fields, for each of them we consider the "**best**" field and the corresponding "**rating**".
Note: "**best**" is not always available and in its place we may take "**last**".

### Lichess data collection
There is a similar structure for Lichess data collection. We provide a first simple version of a script called [by_team_lichess.py](data/collectors/by_team_lichess.py) that collect users from a given team and fetch a number of games for each user.

A second and more advanced version of the script is the [by_top_teams_lichess.py](data/collectors/by_top_teams_lichess.py) that fetch users from the top teams in Lichess and fetch a number of games for each user.
It uses the [config_lichess.json](data/config_lichess.json) file to set the number of pages of teams to fetch, the number of users per team and the number of games per user. It is still under development, for now it dumps user infos into the [dump](data/collectors/dump) folder as json files.

An example of user data formatting from Lichess:

```json
{
    "id": "bandityamoisture",
    "username": "Bandityamoisture",
    "perfs": {
        "bullet": {
            "games": 0,
            "rating": 1500,
            "rd": 500,
            "prog": 0,
            "prov": true
        },
        "blitz": {
            "games": 0,
            "rating": 1500,
            "rd": 500,
            "prog": 0,
            "prov": true
        },
        "rapid": {
            "games": 0,
            "rating": 1500,
            "rd": 500,
            "prog": 0,
            "prov": true
        },
        "classical": {
            "games": 0,
            "rating": 1500,
            "rd": 500,
            "prog": 0,
            "prov": true
        },
        "correspondence": {
            "games": 0,
            "rating": 1500,
            "rd": 500,
            "prog": 0,
            "prov": true
        },
        "racingKings": {
            "games": 61,
            "rating": 1932,
            "rd": 56,
            "prog": 4
        }
    },
    "createdAt": 1764088470969,
    "profile": {
        "flag": "RU",
        "bio": "https://lichess.org/tournament/SYW5hOxp\r\nCash tourney on my bday"
    },
    "seenAt": 1764769023875,
    "playTime": {
        "total": 6093,
        "tv": 1309
    },
    "url": "https://lichess.org/@/Bandityamoisture",
    "count": {
        "all": 61,
        "rated": 61,
        "draw": 6,
        "loss": 17,
        "win": 38,
        "bookmark": 0,
        "playing": 0,
        "import": 0,
        "me": 0
    }
}
```

#### MongoDB data structure

##### User data formatting:
 
```json
{
  "_id": "karadere19",
  "username": "Karadere19",
  "perfs": {... 
  },
  "createdAt": {
    "$numberLong": "1621441600160"
  },
  "seenAt": {
    "$numberLong": "1659784330015"
  },
  "playTime": {
    "total": 88944,
    "tv": 0
  },
  "url": "https://lichess.org/@/Karadere19",
  "count": {
    "all": 617,
    "rated": 588,
    "draw": 14,
    "loss": 363,
    "win": 240,
    "bookmark": 0,
    "playing": 0,
    "import": 0,
    "me": 0
  },
  "games": [
    "https://lichess.org/meICeKtf",
    "https://lichess.org/tJNceLBD",
    ...
  ]
}
```

##### Game data formatting:

```json
{
  "_id": "https://lichess.org/tJNceLBD",
  "url": "https://lichess.org/tJNceLBD",
  "white_player": "Hikmet2014",
  "black_player": "Karadere19",
  "white_rating": 1600,
  "black_rating": 892,
  "result_white": "draw",
  "result_black": "draw",
  "eco_url": "https://www.365chess.com/eco/C40",
  "opening": "King's Knight Opening",
  "moves": "e4 e5 Nf3",
  "time_class": "rapid",
  "rated": false,
  "end_time": {
    "$date": "2021-12-09T18:58:00.539Z"
  }
}
```

## Join: MongoDB data structure

There are two main collections, one for user data (name, stats etc...) and one for games informations:

### User data formatting:
 
```json
{
  "_id": {"$oid": "6938501e3476babef72428a6"},
  "avatar": "https://images.chesscomfiles.com/uploads/v1/user/13697762.dc1e21aa.200x200o.dd8e0600e6df.png",
  "player_id": 13697762,
  "url": "https://www.chess.com/member/00sonal1234",
  "name": "kittu kapil",
  "username": "00sonal1234",
  "followers": 1,
  "country": "https://api.chess.com/pub/country/IN",
  "location": "delhi",
  "last_online": 1484205470,
  "joined": 1380377504,
  "status": "basic",
  "is_streamer": false,
  "verified": false,
  "streaming_platforms": [],
  "games": [
    {
      "$oid": "6938501d3476babef7242897"
    },
    {
      "$oid": "6938501d3476babef7242898"
    },
    {
      "$oid": "6938501d3476babef7242899"
    },
    {
      "$oid": "6938501d3476babef724289a"
    },
    {
      "$oid": "6938501d3476babef724289b"
    },
    {
      "$oid": "6938501d3476babef724289c"
    },
    {
      "$oid": "6938501d3476babef724289d"
    },
    {
      "$oid": "6938501d3476babef724289e"
    },
    {
      "$oid": "6938501d3476babef724289f"
    },
    {
      "$oid": "6938501e3476babef72428a0"
    },
    {
      "$oid": "6938501e3476babef72428a1"
    }
  ],
  "stats": {
    "bullet": 1800,
    "blitz": 1429,
    "rapid": 1054
  }
}
```

As you can see, user and games association is done through direct correlation with the "games" field that contains the _id of the game that links with the game in the games collection with the same _id.

### Game data formatting:

```json
{
  "_id": {
    "$oid": "6938501d3476babef7242897"
  },
  "url": "https://www.chess.com/game/live/610659145",
  "white_player": "00sonal1234",
  "black_player": "pawnchewer",
  "white_rating": 1326,
  "black_rating": 1070,
  "result_white": "win",
  "result_black": "timeout",
  "eco_url": "https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5",
  "opening": "Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5",
  "moves": "b3 e5 Bb2 d5 e3 Nc6 g3 Nf6 Bg2 Bg4 Ne2 e4 h3 Bh5 O-O Qd7 g4 Nxg4 hxg4 Bxg4 Kh1 f6 d3 Qf7 dxe4 Qh5+ Kg1 Bxe2 Qe1 Bxf1 Qxf1 O-O-O exd5 Rxd5 Nc3 Rg5 Ne4 Rg4 f3 Rh4 Kf2 Rh2 Ke2 Ne5 Kd1 Qg6 Bxe5 fxe5 Qd3 Qxg2 Kc1 Rh1+ Kb2 Ba3+ Kc3 Rxa1 Nc5 1-0",
  "time_class": "blitz",
  "rated": true,
  "end_time": 1380377920
}
```

## Ideas:
- User registers by entering:
    - username (unique)
    - password
    - email

- User can log in with username and password.
- User can add a game to mongoDB by entering a PGN file.
- User can search for games in the local DB by username, opening, date, etc.
- User can view statistics on their games (e.g. win percentage by opening, average time per move, etc.).
- User can download their games.
- Admin can add/edit/remove users. It feeds the massive DB with game data (scraping from chess.com, lichess.org, etc.).



- When the user searches for a game by username, opening, date, etc., the system first searches the local DB (if it exists), otherwise it scrapes the sites (chess.com, lichess.org) and saves the data in the local DB for future searches. The user can suggest which site to scrape from (default: both).
If the data is too old (e.g. older than 1 month), the system scrapes to update the data.


## To test the application from Postman

GET http://localhost:8080/games/6910afd789b0930404f6bbd6

```