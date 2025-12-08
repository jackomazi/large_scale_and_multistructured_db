# large_scale_and_multistructured_db

## Data collection

The data collection required to populate the MongoDB database with the required user data and associated games information is done through Python scripts in the "*data\api*" folder.
Any methods used to interface with Chess.com and Lichess.com are contained in the class declaration Python files in the "*data\api*" folder.

### Chess.com data collection

The Python script files [by_club_chess_com.py](data\collectors\by_club_chess_com.py), [by_country_chess_com.py](data\collectors\by_country_chess_com.py) and [by_player_chess_com.py](data\collectors\by_player_chess_com.py) will collect a tot. amount of user data, games data per user according to the [config.json](data/config.json) file.

#### Config.json

- Collection by club name: 
    - "clubs": Name of clubs to collect data from.
    - "max_scrap_users_per_club": Maximum number of user to process per club (always the first n).
    - "max_scrap_archives": Users games data are organized in "archives" based on the date of the games, is the maximum number of archives to compute per user.
    - "max_scrap_games_per_archive": Maximum games to compute per archive.

#### MongoDB data structure

There are two main collections, one for user data (name, stats ecc...) and one for games informations:

##### User data formatting:
 
```json
{
    "_id": "https://api.chess.com/pub/player/00sonal1234"
    "avatar": "https://images.chesscomfiles.com/uploads/v1/user/13697762.dc1e21aa.200…",
    "player_id": 98541127,
    "url": "https://www.chess.com/member/00sonal1234",
    "name": "String",
    "username": "String",
    "followers": 2,
    "country": "https://api.chess.com/pub/country/IN",
    "location": "String",
    "last_online": "Epoch Unix date",
    "joined": "Epoch Unix date",
    "status": "String",
    "is_streamer": false,
    "verified": false,
    "streaming_platforms": "Array",
    "games": [
        0: "https://www.chess.com/game/live/610659145",
        1: "https://www.chess.com/game/live/610665113",
        ...
    ],
    "stats": {
        "bullet": 1800,
        "blitz":1429,
        "rapid":1054
    }

}
```

As you can see, user and games association in done through direct correlation with the "games" field that contains the _id of the game that links with the game in the games collection with the same _id.

##### Game data formatting:

```json
{
    "_id": "https://www.chess.com/game/live/610659145",
    "url": "https://www.chess.com/game/live/610659145",
    "white_player": "String",
    "black_player": "String",
    "white_rating": 1500,
    "black_rating": 1200,
    "result_white": "String",
    "result_black": "String",
    "eco_url": "https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variat…",
    "opening": "String",
    "moves": "String",
    "time_class": "String",
    "rated": true,
    "end_time":  "Epoch Unix date"
}
```

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

Idee:
- Utente si registra inserendo:
    - username (unico)
    - password
    - email

- Utente può loggarsi con username e password.
- Utente può aggiungere una partita al mongoDB inserendo un file PGN.
- Utente può cercare partite nel DB locale per username, opening, data, ecc.
- Utente può visualizzare statistiche sulle proprie partite (es. percentuale vittorie per apertura, tempo medio per mossa, ecc).
- Utente può scaricare le proprie partite.
- Admin può aggiungere/modificare/rimuovere utenti. Alimenta il DB massivo con dati di partite (scraping da chess.com, lichess.org, ecc).



- Quando l'utente cerca una partita per username, opening, data, ecc, il sistema prima cerca nel DB locale (se esiste), altrimenti fa scraping sui siti (chess.com, lichess.org) e salva i dati nel DB locale per future ricerche. L'utente può suggerire da quale sito fare scraping (default: entrambi).
Se i dati sono troppo vecchi (es. più vecchi di 1 mese), il sistema fa scraping per aggiornare i dati.


## Per testare l'applicazione da Postman

GET http://localhost:8080/games/6910afd789b0930404f6bbd6
