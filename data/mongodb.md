# MongoDB Collections
## User Data Structure

```json
{
  "_id": {
    "$oid": "694ac4149f2506330229c5a7"
  },
  "name": "kittu kapil",
  "username": "00sonal1234",
  "followers": 1,
  "country": "IN",
  "last_online": "2017-01-12 08:17:50",
  "joined": "2013-09-28 16:11:44",
  "is_streamer": false,
  "streaming_platforms": [],
  "club": "ciaodkdk",
  "games": [
    {
      "_id": {
        "$oid": "694ac4139f2506330229c59f"
      },
      "white": "00sonal1234",
      "black": "pawnchewer",
      "opening": "Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2-Nc6-3.e3-d5",
      "winner": "pawnchewer",
      "date": 1380377920
    },
    {
      "_id": {
        "$oid": "694ac4139f2506330229c5a0"
      },
      "white": "InfiniteRegress",
      "black": "00sonal1234",
      "opening": "Owens-Defense",
      "winner": "00sonal1234",
      "date": 1380378480
    },
    {
      "_id": {
        "$oid": "694ac4139f2506330229c5a1"
      },
      "white": "00sonal1234",
      "black": "Wajdi_Mliki",
      "opening": "Nimzowitsch-Larsen-Attack-Modern-Variation-2.Bb2",
      "winner": "Wajdi_Mliki",
      "date": 1380379084
    },
    {
      "_id": {
        "$oid": "694ac4139f2506330229c5a2"
      },
      "white": "don_lubricant",
      "black": "00sonal1234",
      "opening": "Birds-Opening",
      "winner": "00sonal1234",
      "date": 1380379438
    }
  ],
  "stats": {
    "bullet": 1800,
    "blitz": 1429,
    "rapid": 1054
  },
  "tournaments":{
    "wins": 43,
    "losses": 1,
    "draws": 0,
    "best_placement": 2
  }
  "mail": "samuel34@example.org",
  "password": "fc76fc81d93e6c8a0ab7fcbbfc5f2cbf9974aa7d6b3f85214d8b55c48930de37"
}
```

## Game Data Structure

```json
{
  "_id": { "$oid": "6938501d3476babef7242897" },
  "white_player": "00sonal1234",
  "black_player": "pawnchewer",
  "white_rating": 1326,
  "black_rating": 1070,
  "result_white": "win",
  "result_black": "timeout",
  "opening": "Nimzowitsch-Larsen-Attack-Modern-Variation",
  "moves": "b3 e5 Bb2 d5 ...",
  "time_class": "blitz",
  "end_time": 1380377920
}
```

## Club Data Structure

```json
{
  "_id": {
    "$oid": "694ac46e9f2506330229c5c6"
  },
  "name": "India 11",
  "description": "<p>We invite Indians and all chess lovers to join group 'INDIA-11'. Our club represents India in all official International team matches at chess.com. We play serious chess to have good fun. We play team and vote chess matches as well. And, If you're looking to chat with other friends and chess players, then India 11 is best group for you. India 11, has the second most active chat room on Chess.com and the best for chatting, socializing and networking, where you can meet and discuss with great chess players all over the world. To access the instant chat room, &gt;&gt; STEP 1: Log into live chess. STEP 2: Go to the \"Chat\" tab located under the graph seek, STEP 3: Select \"Rooms\". STEP 4: Finally click \"Join\" which is next to 'India 11'. Please, invite your all Indian friends to join India 11. So enjoy and have nonstop 24/7 fun.</p>",
  "country": "IN",
  "creation_date": "2016-04-25 12:55:03",
  "admin": "cool64chess"
}
```

## Tournament Data Structure

```json
{
  "_id": {
    "$oid": "694ac40e9f2506330229c589"
  },
  "name": "2015 A LAST CHESS COMPETITION",
  "description": "",
  "creator": "1959JUANPEDROPABLO",
  "status": "finished",
  "finish_time": "2016-06-21 03:24:21",
  "min_rating": 600,
  "max_rating": 1300,
  "participants": 24,
  "max_participants": 20,
  "time_control": "1/259200"
  "players": [
    {
      "username": "dance_party",
      "status": "eliminated"
    },
    {
      "username": "biglouie",
      "status": "eliminated"
    },
    {
      "username": "ssbagley",
      "status": "eliminated"
    },
    {
      "username": "groshev",
      "status": "eliminated"
    },
    {
      "username": "dmarkg",
      "status": "active"
    },
    {
      "username": "flatearth47",
      "status": "eliminated"
    },
    {
      "username": "mr_sunshine",
      "status": "eliminated"
    },
    {
      "username": "panchromatic",
      "status": "eliminated"
    }
  ],
  "games": [
    {
      "_id": {
        "$oid": "694ac40d9f2506330229c575"
      },
      "white": "dmarkg",
      "black": "Dulli4Life",
      "opening": "London System",
      "winner": "Dulli4Life",
      "date": 1461608407
    },
    {
      "_id": {
        "$oid": "694ac40e9f2506330229c576"
      },
      "white": "dmarkg",
      "black": "1959JUANPEDROPABLO",
      "opening": "London System",
      "winner": "1959JUANPEDROPABLO",
      "date": 1461328192
    },
    {
      "_id": {
        "$oid": "694ac40e9f2506330229c577"
      },
      "white": "1959JUANPEDROPABLO",
      "black": "Panchromatic",
      "opening": "London System",
      "winner": "Panchromatic",
      "date": 1460688213
    },
    {
      "_id": {
        "$oid": "694ac40e9f2506330229c578"
      },
      "white": "1959JUANPEDROPABLO",
      "black": "LukichevSV",
      "opening": "London System",
      "winner": "LukichevSV",
      "date": 1460491428
    }
  ]
}
```
