# Data Collection & API Documentation

The data collection required to populate the MongoDB database with user data and associated games is performed through Python scripts. These scripts utilize wrappers for the public APIs of Chess.com and Lichess.org, located in the `api` directory.

## Sources & Endpoints

We utilize specific endpoints from Chess.com and Lichess to build our dataset. Below is a detailed breakdown of the endpoints used by our API wrappers (`chess_com.py` and `lichess.py`), along with the raw data structure and extracted fields.

### Chess.com API (`chess_com.py`)

**Base URL**: `https://api.chess.com`

#### Clubs

* **Method**: coming soon
* **Endpoint**: `/pub/club/{club-name}`
* **Purpose**: Retrieve the information about a given chess.com chess club, could be useful if we want to have a club collection, especially if we want to give this app a "social media" aspect, with also the possibility of integrating this with graphdb.
* **Example Dump**:

    ```json
    {
      "@id":"https://api.chess.com/pub/club/team-italy",
      "name":"Team Italy",
      "club_id":65766,
      "country":"https://api.chess.com/pub/country/IT",
      "average_daily_rating":1111,
      "members_count":1,
      "created":1531739180,
      "last_activity":1531739180,
      "admin":["https://api.chess.com/pub/player/puntaala"],
      "visibility":"public",
      "join_request":"https://www.chess.com/club/join/65766",
      "url":"https://www.chess.com/club/team-italy"}
    ```

  * **Useful fields**: Potentially all of them. A noteworthy mention is that clubs are linked to a certain country so we can potentially make queries about that (Ex: which country has the best players? etc..)

#### Club Members

* **Method**: `get_players_usernames(club)`
* **Endpoint**: `/pub/club/{club}/members`
* **Purpose**: Retrieves a list of usernames of players registered in a specific club. Useful to find users of any level of skill since clubs can be joined by anyone
* **Example Dump**:

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

* **Useful Fields**:
  * `all_time`: We iterate through this array.
  * `username`: Extracted to help build a list of users for our app.

#### Player Profile

* **Method**: `get_player_infos(username)`
* **Endpoint**: `/pub/player/{username}`
* **Purpose**: Gets the profile information of a specific player.
* **Example Dump**:

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

* **Useful Fields**: We utilize this object as a starting point for our Player (or User?) collection so most fields are important. We should remove all those that link to chess.com, the `avatar`, the `followers` count since it is part of a social media functionality that is not our own, and `verified` which is meaningless for us.
`is_streamer` is something we can build some queries around.

#### Player Stats

* **Method**: `get_player_games_stats(username)`
* **Endpoint**: `/pub/player/{username}/stats`
* **Purpose**: Retrieves player's stats like their elo and win rate in different types of games
* **Example Dump**:

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

* **Useful Fields**:
  * `chess_blitz`, `chess_bullet`, `chess_rapid`: We extract the `best.rating` (or `last.rating` if best is unavailable) for these categories. This is a gold mine for potential queries so we should try to integrate this in our app as much as possible

#### Player Game Archives

* **Method**: `get_player_games_archives(username)`
* **Endpoint**: `/pub/player/{username}/games/archives`
* **Purpose**: Returns the list of URLs for a player's game archives (organized by month/year). This is not something that will go in our database but its useful to find information about games
* **Example Dump**:

    ```json
    {
      "archives": [
        "https://api.chess.com/pub/player/user/games/2013/09",
        "https://api.chess.com/pub/player/user/games/2014/02"
      ]
    }
    ```

#### Monthly Games

* **Method**: `get_chess_com_games(url)`
* **Endpoint**: (Derived from Archives, e.g., `.../games/2013/09`)
* **Purpose**: Extracts games from a given archive URL.
* **Example Dump**:

    ```json
    {
      "games": [
        {
          "url": "https://www.chess.com/game/live/610659145",
          "pgn": "[Event \"Live Chess\"]\n[Site \"Chess.com\"]\n[Date \"2013.09.28\"]\n[Round \"-\"]\n[White \"00sonal1234\"]\n[Black \"pawnchewer\"]\n[Result \"1-0\"]\n[CurrentPosition \"2k4r/ppp3pp/8/2N1p3/8/bPKQPP2/P1P3q1/r7 b - -\"]\n[Timezone \"UTC\"]\n[ECO \"A01\"]\n[ECOUrl \"https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5\"]\n[UTCDate \"2013.09.28\"]\n[UTCTime \"14:14:03\"]\n[WhiteElo \"1326\"]\n[BlackElo \"1070\"]\n[TimeControl \"180\"]\n[Termination \"00sonal1234 won on time\"]\n[StartTime \"14:14:03\"]\n[EndDate \"2013.09.28\"]\n[EndTime \"14:18:40\"]\n[Link \"https://www.chess.com/game/live/610659145\"]\n\n1. b3 {[%clk 0:03:00]} 1... e5 {[%clk 0:03:00]} 2. Bb2 {[%clk 0:02:55]} 2... d5 {[%clk 0:02:57.1]} 3. e3 {[%clk 0:02:54.6]} 3... Nc6 {[%clk 0:02:44.7]} 4. g3 {[%clk 0:02:52.6]} 4... Nf6 {[%clk 0:02:40.7]} 5. Bg2 {[%clk 0:02:51.8]} 5... Bg4 {[%clk 0:02:34.6]} 6. Ne2 {[%clk 0:02:50.6]} 6... e4 {[%clk 0:02:27.6]} 7. h3 {[%clk 0:02:49.1]} 7... Bh5 {[%clk 0:02:22.9]} 8. O-O {[%clk 0:02:46.8]} 8... Qd7 {[%clk 0:02:16.4]} 9. g4 {[%clk 0:02:43.5]} 9... Nxg4 {[%clk 0:02:10]} 10. hxg4 {[%clk 0:02:41.2]} 10... Bxg4 {[%clk 0:02:04.8]} 11. Kh1 {[%clk 0:02:38.1]} 11... f6 {[%clk 0:01:58.6]} 12. d3 {[%clk 0:02:37.3]} 12... Qf7 {[%clk 0:01:54.6]} 13. dxe4 {[%clk 0:02:36]} 13... Qh5+ {[%clk 0:01:51.1]} 14. Kg1 {[%clk 0:02:35]} 14... Bxe2 {[%clk 0:01:47.3]} 15. Qe1 {[%clk 0:02:30.9]} 15... Bxf1 {[%clk 0:01:43.5]} 16. Qxf1 {[%clk 0:02:30]} 16... O-O-O {[%clk 0:01:32.8]} 17. exd5 {[%clk 0:02:27.8]} 17... Rxd5 {[%clk 0:01:28.8]} 18. Nc3 {[%clk 0:02:26.1]} 18... Rg5 {[%clk 0:01:18.8]} 19. Ne4 {[%clk 0:02:22.3]} 19... Rg4 {[%clk 0:01:07.3]} 20. f3 {[%clk 0:02:20.2]} 20... Rh4 {[%clk 0:01:03.4]} 21. Kf2 {[%clk 0:02:18.6]} 21... Rh2 {[%clk 0:00:54.2]} 22. Ke2 {[%clk 0:02:16.9]} 22... Ne5 {[%clk 0:00:38.7]} 23. Kd1 {[%clk 0:02:09.5]} 23... Qg6 {[%clk 0:00:27.8]} 24. Bxe5 {[%clk 0:02:06.9]} 24... fxe5 {[%clk 0:00:24.2]} 25. Qd3 {[%clk 0:01:59.8]} 25... Rg4 {[%clk 0:01:50.8]} 26. Ne4 {[%clk 0:01:49.2]} 26... Ba3 {[%clk 0:01:39.1]} 27. bxa3 {[%clk 0:01:47.4]} 27... Rg2 {[%clk 0:01:31.9]} 28. Qc3 {[%clk 0:01:45.3]} 28... Rh2 {[%clk 0:01:24.8]} 29. Kc1 {[%clk 0:01:38.2]} 29... Ba3+ {[%clk 0:01:14]} 30. Kd1 {[%clk 0:01:37.4]} 30... Rxa1 {[%clk 0:01:13.6]} 31. Nc5 {[%clk 0:01:26.9]} 31... Qg2 {[%clk 0:01:06.3]} 32. Nb3 {[%clk 0:01:25.5]} 32... Rxa2 {[%clk 0:01:05]} 33. Kc1 {[%clk 0:01:21.8]} 33... Rh2 {[%clk 0:01:03]} 34. Nc5 {[%clk 0:01:20.8]} 34... Qg3 {[%clk 0:00:53.6]} 35. Kb2 {[%clk 0:01:16.6]} 35... Ba3+ {[%clk 0:00:50.9]} 36. Kc3 {[%clk 0:01:16.3]} 36... Rxa1 {[%clk 0:00:46.3]} 37. Nc5 1-0",
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
            "@id": "https://api.chess.com/pub/player/00sonal1234",
            "username": "00sonal1234",
            "uuid": "e767b6aa-2847-11e3-803e-000000000000"
          },
          "black": {
            "rating": 1070,
            "@id": "https://api.chess.com/pub/player/pawnchewer",
            "username": "pawnchewer",
            "uuid": "0207d0e4-f471-11de-805e-000000000000"
          },
          "eco": "https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5"
        }
      ]
    }
    ```

* **Useful Fields**:
  * `pgn`: Parsed to extract moves. We can potentially analyze this to find statistics about matches that are give to us by the api
  * `time_class`, `end_time`, `rated`.
  * `white`, `black`: Decomposed for player details and results.

### 2. Lichess API (`lichess.py`)

**Base URL**: `https://lichess.org`

#### Teams

* **Method**: `get_teams_list(page)`
* **Endpoint**: `/api/team/all`
* **Purpose**: Retrieve a list of the most popular teams and their infos on Lichess from a specific page. The number of teams per page is fixed by the API (15 teams per page).
* **Example Dump**:
```json
{
    "id": "lichess-swiss",
    "name": "Lichess Swiss",
    "description": "The official Lichess Swiss team. We organize regular swiss tournaments for all to join.",
    "open": true,
    "leader": {
      "name": "Lichess",
      "flair": "activity.lichess",
      "patron": true,
      "patronColor": 10,
      "id": "lichess"
    },
    "nbMembers": 657383,
    "flair": "food-drink.cheese-wedge",
    "leaders": [
      {
        "name": "thibault",
        "flair": "nature.seedling",
        "patron": true,
        "patronColor": 10,
        "id": "thibault"
      },
      {
        "name": "Lichess",
        "flair": "activity.lichess",
        "patron": true,
        "patronColor": 10,
        "id": "lichess"
      }
    ]
}
```
* **Useful fields**:
  * `id`: used to fetch users of the team.
  * `nbMembers`: could be useful to filter teams with a minimum number of members or make statistics about team sizes.

#### Team Users

* **Method**: `get_players_usernames(team)`
* **Endpoint**: `/api/team/{teamId}/users`
* **Purpose**: Retrieves up to 5000 usernames of players belonging to a Lichess team, sorted by joining date.
* **Example Dump**:
    ```json
    {
      "name":"Karadere19",
      "id":"karadere19",
      "url":"https://lichess.org/@/Karadere19",
      "joinedTeamAt":1622462979699
    }
    ```
* **Useful fields**:
  * `name`: We will use these lists of users to populate our db app.

#### n Users from Team
* **Method**: `get_n_users_from_team(team, n)`
* **Endpoint**: `/api/team/{teamId}/users`
* **Purpose**: Retrieves up to `n` usernames of players belonging to a Lichess team, sorted by joining date. This is a more contolled version of the previous method.



#### User Profile Infos

* **Method**: `get_player_infos(username)`
* **Endpoint**: `/api/user/{username}` (This is an inferred endpoint as the original README just had a dump)
* **Parameters**: 
  * `rank`=true: shows user global rank for each perf (if the user has a rank)
* **Purpose**: Retrieves profile information for a Lichess user.
* **Example Dump**:

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
* **Useful fields**:
  * `perfs`: contains, for each perf, infos about: # of games, current rating, rating deviation (`rd`) which shows the reliability of the rank (initially the deviation is high, then when the user reach more stable rating it become lower), progress (`prog`) which is the elo gained/lost in the last game. 
  * `profile`: contains user profile information. Not all fields are always available. For our purpose the `flag` field could be useful.
  * `playTime`: contains information about the total playtime in minutes and the time spent on TV mode (watching other games).
    * `total`: total minutes played
  * `count`: contains stats about games and interactions:
      - `all`: total games played
      - `draw`, `loss`, `win`: breakdown of results

#### User Games

* **Method**: `get_lichess_games(user, n)`
* **Endpoint**: `/api/games/user/{username}`
* **Purpose**: Retrieves the last `n` games played by a user in NDJSON format.
* **Example Dump**:
```json
  {
  "id": "su6fmJh1",
  "rated": true,
  "variant": "standard",
  "speed": "bullet",
  "perf": "bullet",
  "createdAt": 1765443797836,
  "lastMoveAt": 1765443853385,
  "status": "outoftime",
  "source": "swiss",
  "players": {
    "white": {
      "user": {
        "name": "harshitsuperboy",
        "id": "harshitsuperboy"
      },
      "rating": 1963,
      "ratingDiff": -4
    },
    "black": {
      "user": {
        "name": "VanyaDemyan",
        "id": "vanyademyan"
      },
      "rating": 2077,
      "ratingDiff": 4
    }
  },
  "winner": "black",
  "opening": {
    "eco": "A01",
    "name": "Nimzo-Larsen Attack",
    "ply": 1
  },
  "moves": "b3 Nc6 Bb2 d6 d3 e6 Nd2 e5 Rb1 f6 g3 f5 Bg2 f4 Ngf3 fxg3 fxg3 Nf6 O-O Be7 e4 h6 c3 O-O d4 Nh7 dxe5 dxe5 c4 Bd6 Qe2 Ne7 Bxe5 Bc5+ Kh1 Bd7 Bb2 Bd6 e5 Bxe5 Qxe5 Nf6 Nh4 Nc6 Bxc6 Bxc6+ Kg1 Qe8 Qxe8 Rfxe8 Ng6 Re2 Ne5 Rxd2 Nxc6 Rc2 Bxf6 Rb2",
  "pgn": "[Event \"HyperBullet\"]\n[Site \"https://lichess.org/su6fmJh1\"]\n[Date \"2025.12.11\"]\n[White \"harshitsuperboy\"]\n[Black \"VanyaDemyan\"]\n[Result \"0-1\"]\n[GameId \"su6fmJh1\"]\n[UTCDate \"2025.12.11\"]\n[UTCTime \"09:03:17\"]\n[WhiteElo \"1963\"]\n[BlackElo \"2077\"]\n[WhiteRatingDiff \"-4\"]\n[BlackRatingDiff \"+4\"]\n[Variant \"Standard\"]\n[TimeControl \"30+0\"]\n[ECO \"A01\"]\n[Opening \"Nimzo-Larsen Attack\"]\n[Termination \"Time forfeit\"]\n\n1. b3 Nc6 2. Bb2 d6 3. d3 e6 4. Nd2 e5 5. Rb1 f6 6. g3 f5 7. Bg2 f4 8. Ngf3 fxg3 9. fxg3 Nf6 10. O-O Be7 11. e4 h6 12. c3 O-O 13. d4 Nh7 14. dxe5 dxe5 15. c4 Bd6 16. Qe2 Ne7 17. Bxe5 Bc5+ 18. Kh1 Bd7 19. Bb2 Bd6 20. e5 Bxe5 21. Qxe5 Nf6 22. Nh4 Nc6 23. Bxc6 Bxc6+ 24. Kg1 Qe8 25. Qxe8 Rfxe8 26. Ng6 Re2 27. Ne5 Rxd2 28. Nxc6 Rc2 29. Bxf6 Rb2 0-1\n\n\n",
  "swiss": "MxNTY1WE",
  "clock": {
    "initial": 30,
    "increment": 0,
    "totalTime": 30
  }
}
```
* **Useful fields**: here everything is potentially useful, pgn is a redundancy so it could be removed. 
From this data we can analyze `opening` played by a player when he is playing as black or as white, like a match on players.white.user.name = {username} and group by opening.name, with a count of documents.


## Configuration & Scripts

Data collection is driven by Python scripts in the `collectors` directory, configured via JSON files.

### Configuration

* **`config.json`** (Chess.com):
  * `clubs`: List of clubs to scrape.
  * `max_scrap_users_per_club`: Limit on users per club.
  * `max_scrap_archives`: Limit on history depth (months) per user.
  * `max_scrap_games_per_archive`: Limit on games per month.
* **`config_lichess.json`** (Lichess):
  * Sets pages of teams to fetch, users per team, and games per user.

### Collectors

* **Chess.com**: `by_club_chess_com.py`, `by_country_chess_com.py`, `by_player_chess_com.py`.
* **Lichess**: `by_team_lichess.py`, `by_top_teams_lichess.py`.

## Modeling

### Unified Data Model (MongoDB)

We transform data from both sources into a unified structure to facilitate analysis.

#### User Data Structure

```json
{
  "_id": {
    "$oid": "69397642ce8d9e08bfd261bf"
  },
  "avatar": "https://images.chesscomfiles.com/uploads/v1/user/13697762.dc1e21aa.200x200o.dd8e0600e6df.png",
  "player_id": 13697762,
  "name": "kittu kapil",
  "username": "00sonal1234",
  "followers": 1,
  "country": "IN",
  "location": "delhi",
  "last_online": "2025-12-07 14:31:28",
  "joined": "2013-09-28 16:11:44",
  "status": "basic",
  "is_streamer": false,
  "verified": false,
  "streaming_platforms": [],
  "games": [
    {
      "$oid": "6939763ece8d9e08bfd261bb"
    },
    {
      "$oid": "6939763ece8d9e08bfd261bc"
    },
    {
      "$oid": "69397642ce8d9e08bfd261bd"
    },
    {
      "$oid": "69397642ce8d9e08bfd261be"
    }
  ],
  "stats": {
    "bullet": 1800,
    "blitz": 1429,
    "rapid": 1054
  },
  "mail": "wilsonmarcus@example.org",
  "password": "73467ed48d7084f764505c6c4735331b2d0bdfaf144559b958e99c2c2e5c1fa3"
}
```

#### Game Data Structure

```json
{
  "_id": { "$oid": "6938501d3476babef7242897" },
  "url": "https://www.chess.com/game/live/610659145",
  "white_player": "00sonal1234",
  "black_player": "pawnchewer",
  "white_rating": 1326,
  "black_rating": 1070,
  "result_white": "win",
  "result_black": "timeout",
  "opening": "Nimzowitsch-Larsen-Attack-Modern-Variation",
  "moves": "b3 e5 Bb2 d5 ...",
  "time_class": "blitz",
  "rated": true,
  "end_time": 1380377920
}
```

## Join Strategy

The association between users and games is maintained through a direct reference in the User document.

* The **User** document contains a `games` field, which is an array of ObjectIDs.
* Each ObjectID in this array links to a specific document in the **Games** collection.
* This allows for efficient retrieval of all games associated with a specific user.
