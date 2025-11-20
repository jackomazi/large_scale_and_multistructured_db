# large_scale_and_multistructured_db

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
