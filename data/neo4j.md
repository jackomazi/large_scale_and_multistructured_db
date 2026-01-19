# Neo4j Database Schema

The Neo4j database is used to model relationships between **Users**, **Clubs**, and **Tournaments**, storing the connections and specific performance metrics associated with these interactions.

## Nodes (Entities)

### `USER`
Represents a chess player.
- **Properties:**
  - `name`: The username of the player (String).
  - `mongo_id`: The unique reference ID linking to the MongoDB document (String).

### `CLUB`
Represents a chess club.
- **Properties:**
  - `name`: The name of the chess club (String).
  - `mongo_id`: The unique reference ID linking to the MongoDB document (String).

### `TOURNAMENT`
Represents a chess tournament.
- **Properties:**
  - `name`: The name of the tournament (String).
  - `mongo_id`: The unique reference ID linking to the MongoDB document (String).

---

## Relationships

### `(:USER)-[:JOINED]->(:CLUB)`
Indicates a user's membership in a club.
- **Properties:**
  - `country`: The country code associated with the user in the context of this club (String).
  - `bullet`: The user's **Bullet** rating at the time of scraping (Integer).
  - `blitz`: The user's **Blitz** rating (Integer).
  - `rapid`: The user's **Rapid** rating (Integer).

### `(:USER)-[:PARTECIPATED]->(:TOURNAMENT)`
Indicates a user's participation in a tournament.
- **Properties:**
  - `wins`: Number of wins in the tournament (Integer).
  - `draws`: Number of draws in the tournament (Integer).
  - `losses`: Number of losses in the tournament (Integer).
  - `placement`: The final rank or placement of the user in the tournament (Integer).

### `(:USER)-[:FOLLOWS]->(:USER)`
Indicates a social connection where one user follows another.
- **Properties:** None explicitly defined in the current ingestion script.
