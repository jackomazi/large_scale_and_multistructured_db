# large_scale_and_multistructured_db

## Data collection

The data collection required to populate the MongoDB database with the required user data and associated games information is done through Python scripts in the "*data\api*" folder.
Any methods used to interface with Chess.com and Lichess.com are contained in the class declaration Python files in the "*data\api*" folder.

### Chess.com data collection

The Python script files "*data\collectors\by_club_chess_com.py*", "*data\collectors\by_country_chess_com.py*" and "*data\collectors\by_player_chess_com.py*" will collect a tot. amount of user data, games data per user according to the "*data/config.json*" file.

#### Config.json

- Collection by club name: 
    - "clubs": Name of clubs to collect data from.
    - "max_scrap_users_per_club": Maximum number of user to process per club (always the first n).
    - "max_scrap_archives": Users games data are organized in "archives" based on the date of the games, is the maximum number of archives to compute per user.
    - "max_scrap_games_per_archive": Maximum games to compute per archive.

#### MongoDB data structure

There are two main collections, one for user data (name, stats ecc...) and one for games informations:

##### User data formatting:
 
```

_id: "https://api.chess.com/pub/player/00sonal1234"
avatar: "https://images.chesscomfiles.com/uploads/v1/user/13697762.dc1e21aa.200…"
player_id: int,
url: "https://www.chess.com/member/00sonal1234",
name: String,
username: String,
followers: int,
country: "https://api.chess.com/pub/country/IN",
location: String,
last_online: "Epoch Unix date",
joined: "Epoch Unix date",
status: String,
is_streamer: bool,
verified: bool,
streaming_platforms: Array (empty),
games: Array (2):
    0: "https://www.chess.com/game/live/610659145"
    1: "https://www.chess.com/game/live/610665113"
stats: Object
```

As you can see, user and games association in done through direct correlation with the "games" field that contains the _id of the game that links with the game in the games collection with the same _id.

##### Game data formatting:

```

_id: "https://www.chess.com/game/live/610659145",
url: "https://www.chess.com/game/live/610659145",
white_player: String,
black_player: String,
white_rating: int,
black_rating: int,
result_white: String,
result_black: String,
eco_url: "https://www.chess.com/openings/Nimzowitsch-Larsen-Attack-Modern-Variat…"
opening: String,
moves: String,
time_class: String,
rated: Bool,
end_time:  "Epoch Unix date"
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
