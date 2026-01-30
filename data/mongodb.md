# MongoDB Collections
## User Data Structure

```json
{
  "_id": {
    "$oid": "69778de0cba6240a4bd04cf5"
  },
  "username": "--hugo--",
  "country": "IT",
  "last_online": "2026-01-26 16:49:07",
  "joined": "2012-09-28 15:44:42",
  "is_streamer": false,
  "verified": false,
  "streaming_platforms": [],
  "games": [
    {
      "_id": {
        "$oid": "69778dafcba6240a4bd04b03"
      },
      "white": "Dolfi112",
      "black": "--Hugo--",
      "opening": "Kings-Indian-Defense-Samisch-Normal-Defense...7.Qd2-Nbd7-8.Nge2-c5",
      "winner": "--Hugo--",
      "date": "2024-07-01 11:41:24"
    },
    {
      "_id": {
        "$oid": "69778dafcba6240a4bd04b04"
      },
      "white": "--Hugo--",
      "black": "glji11962",
      "opening": "Alapin-Sicilian-Defense...5.d4-cxd4-6.cxd4-d6",
      "winner": "glji11962",
      "date": "2024-07-01 11:47:00"
    },
    {
      "_id": {
        "$oid": "69778dafcba6240a4bd04b05"
      },
      "white": "200orbustQueen",
      "black": "--Hugo--",
      "opening": "Kings-Indian-Defense-Kramer-Variation",
      "winner": "--Hugo--",
      "date": "2024-07-01 11:55:33"
    },
    {
      "_id": {
        "$oid": "69778db5cba6240a4bd04b32"
      },
      "white": "--Hugo--",
      "black": "csyis1227",
      "opening": "Ruy-Lopez-Opening-Jaenisch-Gambit",
      "winner": "csyis1227",
      "date": "2023-10-03 18:55:39"
    },
    {
      "_id": {
        "$oid": "69778db5cba6240a4bd04b33"
      },
      "white": "--Hugo--",
      "black": "Daviandro",
      "opening": "Sicilian-Defense-Canal-Attack-3...Nc6-4.O-O-Bd7-5.Re1",
      "winner": "Daviandro",
      "date": "2023-10-07 14:34:27"
    },
    {
      "_id": {
        "$oid": "69778db5cba6240a4bd04b34"
      },
      "white": "Staroindiets",
      "black": "--Hugo--",
      "opening": "Modern-Defense-with-1-e4-2.d4-d6",
      "winner": "--Hugo--",
      "date": "2023-10-07 14:44:18"
    }
  ],
  "stats": {
    "bullet": 2177,
    "blitz": 2201,
    "rapid": 1967
  },
  "buffered_games": 50,
  "mail": "lsmith@example.org",
  "password": "29564b2b9e00093a2051708cce3fb75f59d6b04983b2efd6958021c5c8a32999",
  "admin": false
}

```

## Game Data Structure

```json
{
  "_id": {
    "$oid": "69778dafcba6240a4bd04b03"
  },
  "white_player": "Dolfi112",
  "black_player": "--Hugo--",
  "white_rating": 2083,
  "black_rating": 2188,
  "result_white": "resigned",
  "result_black": "win",
  "opening": "Kings-Indian-Defense-Samisch-Normal-Defense...7.Qd2-Nbd7-8.Nge2-c5",
  "moves": "d4 Nf6 c4 g6 Nc3 Bg7 e4 d6 f3 a6 Be3 Nbd7 Qd2 c5 Nge2 O-O h4 cxd4 Nxd4 h5 Be2 Ne5 Bh6 Bd7 g4 Rc8 gxh5 Nxh5 b3 b5 f4 Bxh6 Bxh5 gxh5 Qg2+ Ng4 Nce2 Qa5+ 0-1",
  "time_class": "blitz",
  "rated": true,
  "end_time": "2024-07-01 11:41:24",
  "historical":true
}
```

## Club Data Structure

```json
{
  "_id": {
    "$oid": "69778da9cba6240a4bd04b02"
  },
  "name": "Team Italia",
  "description": "<p><span style=\"font-size: 24px;\"><strong><span style=\"color: #2dc26b;\">Benvenuto </span>nel <span style=\"color: #e03e2d;\">Team Italia&hellip;</span></strong></span></p>\r\n<p><span style=\"font-size: 18px;\"><em><strong>Dal 2007 il Team Italia rappresenta ufficialmente la Repubblica Italiana nelle competizioni Nazionali ed Internazionali.</strong></em></span></p>\r\n<p><span style=\"font-size: 18px; color: #e03e2d;\"><strong>Non aspettare oltre, <a style=\"color: #e03e2d;\" href=\"https://www.chess.com/club/team-italia\"><span style=\"font-size: 24px;\">ISCRIVITI</span></a> subito...</strong></span><a href=\"https://chessteams.info/?myteam=Team+Italia\"><span style=\"font-size: 18px; color: #e03e2d;\"><strong><img src=\"https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/Cavusu/phpmcKkgv.jpg\" style=\"margin: 0 auto;\" class=\"imageUploaderImg\" alt=\"\" width=\"564\" height=\"123\" /></strong></span></a></p>\r\n<p><span style=\"font-size: 18px; color: #e03e2d;\"><strong><a style=\"color: #e03e2d;\" href=\"https://www.chess.com/club/team-italia\"><img src=\"https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/Cavusu/phpijdqhV.jpg\" style=\"margin: 0px auto;\" class=\"imageUploaderImg\" alt=\"\" width=\"657\" height=\"219\" /></a></strong></span></p>\r\n<p><span style=\"color: #e03e2d;\"><strong><span style=\"font-size: 24px;\">&nbsp;- INDICE LISTE -&nbsp;</span></strong></span></p>\r\n<ul>\r\n<li><span style=\"font-size: 18px;\"><span style=\"color: #2dc26b;\"><a style=\"color: #2dc26b;\" href=\"https://www.chess.com/club/matches/team-italia/1319073\"><strong>DAILY</strong></a></span></span></li>\r\n</ul>\r\n<ul style=\"list-style-type: circle;\">\r\n<li><a href=\"https://www.chess.com/club/matches/team-italia/1319075\"><span style=\"font-size: 18px;\"><strong>LIVE</strong></span></a></li>\r\n</ul>\r\n<ul>\r\n<li><strong><span style=\"color: #e03e2d;\"><a style=\"color: #e03e2d;\" href=\"https://www.chess.com/club/matches/team-italia/1319079\"><span style=\"font-size: 18px;\">960DAILY</span></a></span></strong></li>\r\n</ul>\r\n<p><span style=\"font-size: 24px; color: #e03e2d;\"><strong>&nbsp;- MATCH LIVE -&nbsp;</strong></span></p>\r\n<ul>\r\n<li><span style=\"color: #2dc26b;\"><strong>LCEL 2024 R Bullet: Team Italia vs</strong></span></li>\r\n</ul>\r\n<ul style=\"list-style-type: circle;\">\r\n<li><span style=\"color: #2dc26b;\"><strong>LCEL 2024 R Blitz: Team Italia vs</strong></span></li>\r\n</ul>\r\n<ul>\r\n<li><strong>LCEL 2024 R Rapid: Team Italia vs</strong></li>\r\n</ul>\r\n<ul style=\"list-style-type: circle;\">\r\n<li><strong>LCWL 2024 Round Bullet: Team Italia vs</strong></li>\r\n</ul>\r\n<ul>\r\n<li><span style=\"color: #e03e2d;\"><strong>LCWL 2024 Round Blitz: Team Italia vs</strong></span></li>\r\n</ul>\r\n<ul style=\"list-style-type: circle;\">\r\n<li><span style=\"color: #e03e2d;\"><strong>LCWL 2024 Round Rapid: Team Italia vs</strong></span></li>\r\n</ul>\r\n<p><span style=\"color: #e03e2d; font-size: 24px;\"><strong>&nbsp;- VOTE CHESS -&nbsp;</strong></span></p>\r\n<p><span style=\"font-size: 18px;\"><strong><span style=\"color: #e03e2d;\">Regolamento </span>Vote Chess <span style=\"color: #2dc26b;\">Team Italia </span>➤ <a href=\"https://www.chess.com/it/clubs/forum/view/regolamento-vote-chess-team-italia\">LINK</a></strong></span></p>\r\n<p><span style=\"font-size: 18px;\"><strong><span style=\"color: #e03e2d;\">Vote Chess</span> (Matches) - <span style=\"color: #2dc26b;\">2024 SEASON</span> ➤ &nbsp;<a href=\"https://www.chess.com/it/clubs/forum/view/vote-chess-2024-season\">LINK</a></strong></span></p>\r\n<p><span style=\"color: #e03e2d; font-size: 24px;\"><strong>&nbsp;- INDICE GENERALE -&nbsp;</strong></span></p>\r\n<ul>\r\n<li><a href=\"https://www.chess.com/clubs/forum/view/team-italia-presentazione-dei-nuovi-iscritti\"><span style=\"font-size: 18px; color: #2dc26b;\">Presentazione <strong>nuovi iscritti</strong></span></a></li>\r\n</ul>\r\n<ul style=\"list-style-type: circle;\">\r\n<li><span style=\"color: #2dc26b;\"><a style=\"color: #2dc26b;\" href=\"https://www.chess.com/clubs/forum/view/le-nostre-partite\"><span style=\"font-size: 18px;\">Le nostre <strong>partite </strong>- Angolo dei <strong>Match</strong></span></a></span></li>\r\n</ul>\r\n<ul>\r\n<li><a href=\"https://www.chess.com/it/clubs/forum/view/circoli-di-scacchi-tornei-otb-ed-eventi-in-italia\"><span style=\"font-size: 18px;\"><strong>Circoli </strong>Scacchistici - <strong>Tornei OTB</strong> - <strong>Eventi </strong>Nazionali ed Internazionali</span></a></li>\r\n</ul>\r\n<ul style=\"list-style-type: circle;\">\r\n<li><a href=\"https://www.chess.com/it/clubs/forum/view/team-delle-regioni-italiane\"><span style=\"font-size: 18px; color: #e03e2d;\"><strong><span style=\"color: #e03e2d;\">Regioni </span></strong>Italiane su Chess.com</span></a></li>\r\n</ul>\r\n<ul>\r\n<li><span style=\"color: #e03e2d;\"><a style=\"color: #e03e2d;\" href=\"https://www.chess.com/it/clubs/forum/view/pausa-caffe\"><span style=\"font-size: 18px;\"><strong>Pausa </strong>Caff&egrave; - <strong>OFF </strong>Topic</span></a></span></li>\r\n</ul>\r\n<p><span style=\"font-size: 24px; color: #e03e2d;\"><strong>&nbsp;- ALTRO -&nbsp;</strong></span></p>\r\n<p>Per ricevere avvisi sui prossimi incontri <a href=\"https://www.chess.com/club/matches/1319079\">LISTA 960 DAILY</a> | <a href=\"https://www.chess.com/club/matches/1319075\">LISTA LIVE</a> | <a href=\"https://www.chess.com/club/matches/team-italia/1319073\">LISTA DAILY</a> | <a href=\"https://chessteams.info/?myteam=Team+Italia\">CLASSIFICHE </a>e avere a disposizione le statistiche Team Italia cliccare <a href=\"https://chessteams.info/?myteam=Team+Italia\">QUI </a>- <a href=\"https://docs.google.com/spreadsheets/d/1Hfxq7ghW3-aI492yzvpaGf4N43u4X0_2VeKKxuCVoxo/edit#gid=1061213884\">QUI (LCWL)</a> - <a href=\"https://docs.google.com/spreadsheets/d/1h3WnBI-UoBeTsdZ6Xh9gHxcVkZ0nQYEPzhGO--_V88g/edit#gid=214438829\">QUI (LCWL 960 CUP)</a>.</p>\r\n<p>Per le dirette streaming con il National Master e Membro del Team Italia @NM_Antelacus consultare <a href=\"http://www.twitch.tv/antelacus\">QUI</a>.</p>",
  "country": "IT",
  "creation_date": "2007-10-12 03:01:15",
  "admin": "pacio8"
}
```

## Tournament Data Structure

```json
{
  "_id": {
    "$oid": "69778e17cba6240a4bd04dbe"
  },
  "name": "19th Chess.com Thematic Tournament - French (1601-1800)",
  "description": "<p>This is the 19th Official Chess.com Thematic Tournament. It will begin May 1st, so register now!</p>\r\n<p>&nbsp;</p>\r\n<p style=\"text-align: center;\"><strong><em>All games start with the French, </em></strong></p>\r\n<p>&nbsp;</p>\r\n<table class=\"hoverable\" style=\"width: 100%;\" border=\"0\" cellspacing=\"0\" cellpadding=\"0\">\r\n<tbody>\r\n<tr class=\"light\">\r\n<td>&nbsp;</td>\r\n<td style=\"text-align: center;\"><strong>1. e4 e6<br /></strong></td>\r\n</tr>\r\n</tbody>\r\n</table>\r\n<p>&nbsp;</p>\r\n<p><strong>NOTE:</strong> If you register now and your rating changes before the start date, you will be AUTOMATICALLY moved to the correct group when the tournament starts.&nbsp;</p>\r\n<p>You will play up to 10 games at a time in this event.</p>\r\n<p>Vacation time is allowed. &nbsp;</p>\r\n<p>New rounds will start when ALL games in the current round are complete. &nbsp;</p>\r\n<p>If you have other questions (group size, # of people advancing, etc) read the Tournament Description above. If you still have questions, try this <a href=\"http://www.chess.com/tournament/tournament/tournament/tournament/tournament/tournament/tournament/tournament/tournament/help.html\" rel=\"nofollow\">help file</a>!&nbsp;</p>\r\n<p>Good luck everyone!</p>",
  "creator": "CHESScom",
  "status": "finished",
  "finish_time": "2015-03-29 20:33:10",
  "games": [
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04cf6"
      },
      "white": "giottoric",
      "black": "jam1123",
      "opening": "Queens Pawn Opening 1...d5",
      "winner": "jam1123",
      "date": "2013-09-11 12:16:24"
    },
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04cf7"
      },
      "white": "timur100",
      "black": "jam1123",
      "opening": "French Defense",
      "winner": "jam1123",
      "date": "2013-07-13 22:59:52"
    },
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04cf8"
      },
      "white": "srikanth_narahari",
      "black": "jam1123",
      "opening": "Queens Pawn Opening Chigorin Variation",
      "winner": "jam1123",
      "date": "2013-07-10 19:42:24"
    },
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04d20"
      },
      "white": "PatrikAkerstrand",
      "black": "othon1693",
      "opening": "Van Geet Opening",
      "winner": "othon1693",
      "date": "2013-06-03 18:32:13"
    },
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04d21"
      },
      "white": "fevgrinder",
      "black": "PatrikAkerstrand",
      "opening": "Queens Pawn Opening Chigorin Variation",
      "winner": "PatrikAkerstrand",
      "date": "2013-06-02 16:02:19"
    },
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04dbc"
      },
      "white": "fakemoustache10",
      "black": "amorsel",
      "opening": "Queens Pawn Opening 1...d5",
      "winner": "amorsel",
      "date": "2013-05-27 21:58:18"
    },
    {
      "_id": {
        "$oid": "69778e17cba6240a4bd04dbd"
      },
      "white": "chesssherborne",
      "black": "fakemoustache10",
      "opening": "Queens Pawn Opening Chigorin Variation",
      "winner": "fakemoustache10",
      "date": "2013-05-27 15:16:59"
    }
  ],
  "max_rating": 1800,
  "min_rating": 600,
  "max_partecipants": 20,
  "time_control": "1/259200",
  "buffered_games": 200
}
```
