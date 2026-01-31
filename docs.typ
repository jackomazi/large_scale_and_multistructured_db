#import "template/docs-template.typ": *

#show: project

= Introduction
Square64: A Comprehensive Platform for Competitive Chess and Strategic Analysis

This project introduces Square64, a digital framework designed to facilitate real-time chess gameplay and foster a global strategic community. The platform aims to modernize the traditional chess experience by integrating competitive matchmaking with social connectivity and advanced data analytics.

The system architecture enables users to engage in synchronous matches while navigating a structured ecosystem of clubs and organized tournaments, stratified by skill level to ensure competitive balance. Beyond gameplay, Square64 incorporates a robust social layer, utilizing algorithms to suggest relevant player connections and maintain user engagement within the chess community.

Furthermore, the platform provides a suite of analytical tools designed to support player development. Users can access granular statistics regarding opening repertoires and track progress across standard time controls, including bullet, blitz, and rapid formats. Square64 serves as a centralized interface for the practical application, analysis, and mastery of chess strategy.

The source code is available on GitHub at: #show link: set text(fill: blue)
#link("https://github.com/jackomazi/large_scale_and_multistructured_db")

= Application Manual

This documentation provides a comprehensive guide to Square64, the web application developed for chess enthusiasts. Square64 caters to players of all levels, offering a platform where users can compete in real-time matches, participate in organized tournaments, and monitor their progress. Furthermore, the application facilitates community building, allowing users to search for other players and follow them, explore thematic clubs, and join them.

== Application highlights
Square64 combines the features of a competitive gaming platform with those of a vertical social network for chess, emphasizing three main functionalities:

- *Competition* (Gameplay): Users can start single matches or register for structured tournaments, competing against opponents from the global community.

- *Community* (Social): The application is designed to connect enthusiasts. Users can search for and follow specific players , as well as search for and join Clubs, creating aggregation groups based on common interests or skill levels.

- *Analysis* (Analytics): Square64 includes performance tracking tools, allowing users to view detailed personal statistics to track their improvement over time.


== User Manual

=== Unregistered User
Visitors to Square64 who have not created an account can still explore significant portions of the platform. This open access model encourages new users to discover the community before committing to registration, allowing them to evaluate whether the platform meets their needs.

An unregistered user can:
- *Browse profiles*: View any registered player's profile, including their username, country, and recent game history. This transparency helps potential users understand the skill levels present in the community.
- *View club information*: Explore the various chess clubs on the platform, read their descriptions, see member counts, and view the list of members along with their ratings. This helps users identify clubs they might want to join after registration.
- *Explore tournament listings*: Access information about past tournament. Users can view tournament parameters (rating requirements, participant limits), see who participated, and review final results and standings.
- *Access game archives*: Browse the historical game database, search for games by player names, openings, or time controls, and view the moves of completed games. This provides educational value even without an account.


=== Registered User
Creating an account unlocks the full potential of Square64, transforming the experience from passive browsing to active participation. Registration requires a unique username, email address, and password. Once registered, users gain access to all interactive features of the platform.

A registered user gains full access to the platform:
- *Play Games*: The core feature of Square64 is live chess gameplay. Users can join the matchmaking queue, where the system pairs them with other waiting players for a game. The matchmaking system considers factors like time control preferences to create balanced and enjoyable matches.
- *Social Features*: Users can follow other players to create their personal network within the platform. The follower system works unidirectionally—following another player does not require their approval. Based on the social graph, the platform provides intelligent friend suggestions by analyzing mutual connections (friends of friends), shared club memberships and common tournament participations.
- *Club Membership*: Users can join any chess club on the platform. Upon joining, their current ratings are recorded as a snapshot, allowing clubs to track member progression over time. Club membership provides a sense of community and enables club-specific statistics and rankings.
- *Tournament Participation*: Users can subscribe to tournaments within a specific time window. This window system ensures adequate preparation time while preventing last-minute flooding of participants. Only users whose ratings fall within the tournament's specified range can participate.
- *Profile Management*: Users have a personal dashboard displaying their complete statistics. This includes separate ratings for bullet (1-minute), blitz (3-5 minute), and rapid (10+ minute) time controls, win/loss records, favorite openings.
- *Game History*: Every game a user plays is permanently recorded and accessible through their profile. Games are classified by opening, allowing users to track which openings they perform best with and identify areas for improvement.
- *View opening statistics*: Access aggregated data about opening popularity and success rates. This analytical feature helps players understand current trends and make informed decisions about their opening repertoire.

=== Key Pages
The application is organized around several key pages, each serving a specific purpose:

- *Home*: The central dashboard serves as the launching point for all activities. Users can immediately enter the matchmaking queue to find an opponent, view any active games in progress.

\

#figure(
  image("template/images/main_page.png",height: 30%),
  caption: [Registered user main interface for join games]
)

\

- *Profile*: A comprehensive view of a player's chess identity on the platform. The profile displays recent games with outcomes, followers and following lists, and performance statistics broken down by opening and time control. Users can also manage and modify their account settings from this page.

\

#figure(
  image("template/images/user_profile.png",height: 36%),
  caption: [Registered user profile page]
)

\

- *Clubs*: A directory of all chess clubs on the platform. Users can browse clubs by country, search by name, and view detailed information about each club including member lists with ratings. The clubs page also displays rankings of club members, fostering friendly competition within communities.

\

#figure(
  image("template/images/club_page.png",height: 36%),
  caption: [Registered user profile page]
)

\

- *Tournaments*: The tournament hub shows all active and completed tournaments. Users can see tournament details, subscribe to upcoming events (when within the subscription window), view current standings in active tournaments, and browse results of completed events. The interface clearly indicates which tournaments a user is eligible for based on their rating.

\

#figure(
  image("template/images/tournament_page.png",height: 36%),
  caption: [Registered user profile page]
)

\

- *Games*: The live game interface where actual chess is played. This page features a full chessboard with piece movement, move validation feedback, the current game position in standard FEN notation, move history, and the detected opening. The interface is designed to be responsive and provide immediate feedback on every action.

\

#figure(
  image("template/images/game_page.png",height: 36%),
  caption: [Registered user profile page]
)

\

== Admin Manual

Administrators play a crucial role in maintaining the Square64 ecosystem. Admin accounts possess elevated privileges that enable platform management and oversight. The admin role is not available during registration—users must be promoted to admin status by an existing administrator.

Administrators have exclusive access to:
- *User Management*: Admins can promote regular users to admin status through the dedicated `/users/promote` endpoint. This capability should be used judiciously, as admin accounts have significant control over the platform. The promotion system creates a chain of trust starting from the initial platform administrators.
- *Tournament Control*: While regular users can only participate in tournaments, administrators can create new tournaments with full control over parameters. This includes setting the tournament name, rating requirements (minimum and maximum allowed ratings), and schedule. Admins can also modify or cancel tournaments if necessary.

#pagebreak()

= Design Overview

== Functional Requirements

=== Main Actors

The system recognizes distinct actor types, each with specific capabilities and restrictions. Understanding these actors helps clarify the boundaries of functionality and guides implementation decisions.

*User (Unregistered)*

Unregistered users can explore the platform without creating an account. They have read-only access to browse player profiles, view club information and member lists, explore tournament listings and results, and access the historical game archive. This open access model allows potential users to evaluate the platform before committing to registration.

*User (Registered)*

The registered user represents the primary actor in the system—the chess player who actively uses the platform. After completing registration and authentication, users have access to all standard platform features:

- *Play live chess games through matchmaking*: Users can queue for games and be matched with other waiting players. The matchmaking system creates games with appropriate time controls and ensures fair pairings.
- *Follow/unfollow other users*: The social graph is built through unidirectional follow relationships. Users can follow anyone without approval, creating a Twitter-like social model rather than a Facebook-like mutual friendship model.
- *Join clubs with rating statistics*: When joining a club, the system captures a snapshot of the user's current ratings, this enables tracking of member growth.
- *Subscribe to tournaments*: Users can enter tournaments that match their rating range during the subscription window. The subscription system manages participant lists and validates eligibility.
- *View game history and analytics*: All played games are recorded and accessible, with detailed statistics computed from game outcomes, openings played.

*Administrator*

Administrators are trusted users with elevated privileges for platform management. The admin role supplements rather than replaces user capabilities—admins retain all user features while gaining additional powers:

- *All user capabilities*: Admins can play games, join clubs, and participate in tournaments just like regular users. This ensures admins remain engaged with the platform and understand the user experience.
- *Promote users to admin role*: The ability to create new administrators cascades from existing admins. This creates an auditable chain of privilege escalation.
- *Create and manage tournaments*: Admins have full control over tournament lifecycle, from creation with custom parameters to management and conclusion.
- *Access administrative endpoints*: Certain API endpoints are restricted to admin access, providing operational capabilities not exposed to regular users.

== Non-Functional Requirements

Beyond specific features, Square64 must meet several quality requirements that influence architectural decisions and implementation approaches.

=== Consistency
The system prioritizes data consistency over availability (CP in CAP theorem), ensuring that all nodes see the same data at the same time:

- *Strong consistency for game state*: Live game operations ensure that all participants see the same game state, preventing conflicting moves or race conditions.
- *Write concerns for critical operations*: Database writes for games, tournaments, and user data use appropriate write concerns to guarantee data durability before acknowledging success.
- *Transactional integrity*: Operations that modify multiple related entities maintain consistency through appropriate transaction handling.

=== Security
User data and system integrity are protected through multiple security layers:

- *Role-based access control*: The system distinguishes between USER and ADMIN roles, with certain endpoints restricted to admin access.
- *Input validation*: All user inputs are validated before processing, preventing injection attacks and ensuring data integrity. This includes validation of move notation, username formats, and request parameters.

=== Performance
The system is optimized for responsiveness across all operations:

- *Fast response times for real-time game state*: Given the real time games functionality of this application it is important to have fast read/write access for live games data
- *Server-side aggregation for analytics*: Complex statistical queries are implemented as aggregation pipelines that execute within the database layer. This pushes computation to the data layer, reducing network overhead and enabling efficient processing of large datasets.
- *Graph traversal for friend recommendations*: Friend suggestions leverage native graph traversal capabilities, efficiently exploring relationship paths that would be expensive to compute with traditional join operations.

== Use Case Diagram

The following diagram illustrates the primary use cases and their association with system actors:

#image("images/docs/usecase.drawio.png")


== UML Diagrams

=== Class Diagram - Core Entities

The following class diagram depicts the main domain entities and their relationships:

#image("images/docs/UML.drawio.png")

The *Player* entity represents registered players with their credentials, personal information, statistics, and embedded game summaries. The stats field contains separate ratings for different time controls.

The *ChessGame* entity captures completed games with full move notation, player information at the time of the game (ratings), and classification data like opening and time control.

The *Club* entity represents chess communities with descriptive information.

The *Tournament* entity manages competitive events with eligibility criteria, participant tracking, and embedded game results.

The *Move* entity represents the chess moves that can be done during a chess game

The *Opening* entity represents the possible openings of a chess game

#pagebreak()

= Data Modeling and Structure

== Dataset Creation

Building a realistic chess platform requires substantial initial data to enable meaningful testing, demonstrate analytics capabilities, and provide a populated environment for demonstration purposes. The dataset for Square64 was assembled from publicly available chess platform APIs, ensuring authentic data that reflects real player behavior and game patterns.

The primary data sources were:
- *Chess.com API*: This comprehensive API provided access to user profiles including ratings, country information, and account statistics. Game archives were retrieved with complete move notation in PGN format. Club data including member lists and club metadata was also sourced from this API.
- *Lichess API*: Supplementary data came from Lichess, mainly for tournament information and additional game records. The remaining data collected from Lichess is analogous to that obtained from Chess.com, including player information and game data.
- *Kaggle*: Historical chess game data was collected from Kaggle, specifically World Chess Championship matches played between 1980 and 2021.

 All data collection respected API rate limits and terms of service.

== Building documents
- *Users*: For users, we retained the information obtained directly from the APIs. In addition, we generated synthetic personal data using the Faker library, including first name, last name, email address, and a password hashed with the SHA-256 algorithm.

- *Clubs*: For clubs, we preserved only the most relevant information, such as the club name, description, and essential metadata.

- *Games*: For games, we maintained the core and most significant attributes, including game metadata and move information, while discarding less relevant fields.

- *Tournaments*: For tournaments, we retained the principal information, such as tournament name, type, date, and main structural details.

== Documents standardization

To ensure data uniformity across Chess.com, Lichess, and Kaggle sources, we defined a standardized schema for each entity type and applied transformation rules during collection.

*Users*: User documents follow a unified schema with the following standardizations:
  - *Username normalization*: Chess.com uses `username`, Lichess uses `name`/`id` — both are mapped to a common `username` field.
  - *Rating alignment*: Lichess ratings tend to be inflated compared to Chess.com. To normalize, Lichess ratings are scaled by a factor of 0.75 (e.g., a 2000 Lichess rating becomes 1500).
  - *Date formatting*: Timestamps are converted from Unix epochs (Chess.com) or millisecond epochs (Lichess) to a standard `YYYY-MM-DD HH:MM:SS` string format.
  - *Country handling*: Chess.com provides country as a URL path; Lichess provides it in the profile. Both are extracted to a simple country code.
  - *Synthetic data*: Email addresses and SHA-256 hashed passwords are generated using the Faker library to enable authentication functionality.

*Clubs*: Club documents are standardized as follows:
  - *Country assignment*: For Lichess teams (which lack country data), a random country is assigned from a predefined list.
  - *Date generation*: Lichess teams lack creation dates, so a random date between 2006-2010 is generated.

*Games*: Game documents are unified with these transformations:
  - *Rating scaling*: Lichess game ratings are scaled by 0.75 for consistency with Chess.com ratings.
  - *Result normalization*: Chess.com uses descriptive results (e.g., "checkmated", "timeout"); Lichess uses "white"/"black"/"draw" as winner. Both are converted to `result_white` and `result_black` with values "win", "loss", or "draw".
  - *Time control*: Chess.com uses `time_class` (bullet/blitz/rapid); Lichess uses `speed`. Both are mapped to `time_class`.
  - *Opening detection*: Chess.com provides openings via ECO URL; Lichess includes an `opening` object. Kaggle data uses ECO codes mapped to opening names via a lookup table. When missing, an opening detector analyzes the first moves.
  - *Move extraction*: Chess.com PGN moves are parsed to remove clock annotations and move numbers. Lichess provides moves directly.
  - *Kaggle historical games*: These are marked with `historical: true` and `time_class: "classical"`.

*Tournaments*: Tournament documents are standardized with:
  - *Name unification*: Lichess uses `fullName`, Chess.com uses `name` — both mapped to `name`.
  - *Time fields*: Start and finish times are converted to `YYYY-MM-DD HH:MM:SS` format. For Lichess, finish time is calculated from start time plus duration in minutes.
  - *Rating bounds*: `min_rating` and `max_rating` define eligibility. Lichess ratings are scaled by 0.75. Chess.com tournaments default `min_rating` to 600 when not specified.

== Scripts for Dataset Creation

A suite of Python scripts, organized in the `data/collectors` directory, automated the data collection and import process:

- `by_club_chess_com.py`: Club information including names, descriptions, member lists, and administrator information is collected. The script also retrieves member statistics, played games and tournament partecipated with relative stats where available.
- `load_lichess.py`: This script is responsible for collecting data from the Lichess API. It retrieves user information, game records, and tournament data, structured to mirror the data obtained from Chess.com in order to ensure consistency and uniformity across the two data sources.
- `historical_games.py`: This script loads historical chess game data from Kaggle, focusing on World Chess Championship matches played between 1980 and 2021. The data includes complete game records and metadata, and is integrated into the dataset following the same schema adopted for the API-sourced data.
- `mongo_interface.py`: This script takes the cleaned JSON data and performs inserts into MongoDB collections, creating appropriate "\_id" field during import.
- `neo4j_interface.py`: Graph data is imported through this script, which creates nodes for users, clubs, and tournaments, then establishes relationships based on follower data, club memberships, and tournament participation.

== Volume Considerations

The system is designed to handle substantial data volumes typical of a moderately popular online chess platform. The following estimates are based on 10,000 daily active users (DAU).

#table(
  columns: (auto, 1fr),
  [*Parameter*], [*Value*],
  [Daily Active Users (DAU)], [10,000],
  [Games played per user per day], [2],
  [Average moves per game], [40],
  [Concurrent live games (peak)], [2,500],
  [Users browsing profiles per day], [8,000],
  [Profile views per browsing user], [10],
  [Users viewing statistics per day], [3,000],
  [Follow/unfollow actions per day], [5,000],
  [Club joins per day], [500],
  [Tournament subscriptions per day], [1,000],
  [Friend suggestion requests per day], [15,000],
)

== Database Choices

The multi-database architecture represents a deliberate design decision to leverage specialized tools for specific data patterns. This persistence approach acknowledges that no single database technology optimally handles all data access patterns.

== MongoDB

MongoDB serves as the primary data store for persistent application data, including user profiles, completed games, clubs, and tournaments. Its document model provides flexibility for evolving schemas while maintaining strong consistency within each collection.

=== Collections

*Users* - Player profiles with embedded game summaries

The users collection stores comprehensive player information in denormalized documents:

```json
{
  "_id": ObjectId,
  "username": "player123",
  "name": "John Doe",
  "country": "US",
  "last_online": "2026-01-26 16:49:07",
  "joined": "2012-09-28 15:44:42",
  "is_streamer": false,
  "verified": false,
  "streaming_platforms": [],
  "stats": { "bullet": 1800, "blitz": 1600, "rapid": 1500 },
  "games": [
    {
    "_id": "...",
    "white": "...",
    "black": "...",
    "opening": "...",
    "winner": "...",
    "date": "..."
    }
  ],
  "buffered_games": 50,
  "mail": "user@example.com",
  "password": "<hashed>",
  "admin": false
}
```

The embedded `games` array contains abbreviated summaries of recent games, enabling quick profile display without joining to the games collection, the array is realized through partial embedding of the game document, game digests contains an "\_id" field that directly link to the refered game, this allows to access the complete document allowing the user to see the replay. The `stats` object separates ratings by time control, reflecting how chess platforms differentiate skill levels across different game speeds. Furthermore the presence of the `games` array enables quick and efficient MongoDB aggregation operating on the last user played games allowing the user page to display their `favorite opening` and `win rate`, again, based on their last performances.

At user creation, the `games` array is initialized with a default amount of placeholders, this has the advatage of defining the user document size avoiding document relocation due to size increases, so this way the array only displays the user most recent played games, enough for our statistical analysis.

*Games* - Complete game records with moves

Every completed game receives a permanent record in this collection:

```json
{
  "_id": ObjectId,
  "white_player": "player1",
  "black_player": "player2",
  "white_rating": 1500,
  "black_rating": 1480,
  "result_white": "resigned",
  "result_black": "win",
  "opening": "Sicilian Defense",
  "moves": "e4 c5 Nf3 d6...",
  "time_class": "blitz",
  "end_time": "2023-10-03 18:55:39",
  "historical": false
}
```

Ratings are captured at game time rather than referenced from user documents, preserving historical accuracy. The `opening` field is populated automatically based on the move sequence, typically identified within the first 5-10 moves. The `end_time` timestamp enables temporal queries for analytics.

*Clubs* - Club information

Clubs are stored as lightweight documents with references to related data:

```json
{
  "_id": ObjectId,
  "name": "Chess Masters",
  "description": "A club for serious players",
  "country": "US",
  "admin": "clubadmin"
}
```

Club membership is managed through Neo4j relationships rather than embedded in the club document, avoiding unbounded array growth and enabling efficient membership queries.

*Tournaments* - Tournament configuration and results

Tournaments contain both configuration and results in a single document:

```json
{
  "_id": ObjectId,
  "name": "Weekly Blitz",
  "description": "<p>This is the 19th Official Chess.com Thematic Tournament. It will begin May 1st, so register now!</p>",
  "status": "active",
  "creator": "CHESScom"
  "finish_time": "2015-03-29 20:33:10",
  "min_rating": 1000,
  "max_rating": 2000,
  "max_participants": 32,
  "games": [
    {
    "_id": "...",
    "white": "...",
    "black": "...",
    "opening": "...",
    "winner": "...",
    "date": "..."
    }
  ],
  "buffered_games": 150,
  "time_control": "1/259200",
}
```

The embedded `games` array work well for tournaments because participant counts are bounded by `max_participants`, preventing unbounded document growth. The `status` field transitions through values like "active", and "finished" as the tournament progresses.

== Neo4j

Neo4j stores the relationship graph that powers social features. By maintaining relationships as first-class entities, Neo4j enables efficient traversal queries that would be prohibitively expensive in document or relational databases.

=== Nodes

*USER* - Chess player node
Each registered user has a corresponding node in Neo4j with minimal properties:
- `name`: Username matching the MongoDB document
- `mongo_id`: Reference to the full user document in MongoDB

The lightweight node design keeps graph operations fast while delegating detailed data to MongoDB.

*CLUB* - Chess club node
Club nodes enable relationship queries for membership:
- `name`: Club name for display purposes
- `mongo_id`: Reference to full club details in MongoDB

*TOURNAMENT* - Tournament node
Tournament nodes track participation relationships:
- `name`: Tournament name
- `mongo_id`: Reference to tournament document

=== Relationships

*FOLLOWS* - Social connection between users
```cypher
(:USER)-[:FOLLOWS]->(:USER)
```
This simple relationship type enables powerful social queries. Finding all followers of a user, all users someone follows, or mutual followers are all single-hop traversals. Friend-of-friend suggestions require two-hop traversals, which Neo4j handles efficiently.

*JOINED* - Club membership with statistics
```cypher
(:USER)-[:JOINED {country, bullet, blitz, rapid}]->(:CLUB)
```
The JOINED relationship stores a snapshot of user ratings at join time, and updated as the relative user chess skills evolve oevr time. This enables club leaderboards and historical tracking without requiring temporal queries against MongoDB.

*PARTECIPATED* - Tournament participation with results
```cypher
(:USER)-[:PARTECIPATED {wins, draws, losses, placement}]->(:TOURNAMENT)
```
Tournament relationships record each player's performance, enabling queries like "find users who placed top 3 in multiple tournaments" without aggregating from game-level data.

\

#figure(
  image("template/images/neo4j_demo_image.png",height: 46%),
  caption: [Neo4j example]
)

== Redis

Redis provides the real-time backbone for live gameplay. Its in-memory architecture delivers consistent sub-millisecond latency, while its versatile data structures map naturally to application requirements.

=== Key Patterns

All Redis keys in Square64 follow a hierarchical namespace convention using `chess:` as the root prefix, with colons as separators to create logical groupings. This naming scheme makes the key space self-documenting and enables pattern-based operations for bulk key management.

The namespace is organized into four primary domains:

- *Matchmaking* (`chess:matchmaking:*`): Contains the queues used to pair players for games. Regular matchmaking uses `chess:matchmaking:queue:{gameType}` where gameType is bullet, blitz, or rapid. Tournament matchmaking uses separate queues at `chess:matchmaking:tournament:{tournamentId}` to ensure players are only matched within their subscribed tournament.

- *Games* (`chess:game:*` and `chess:player:game:*`): Stores active game state and player-to-game mappings. The key `chess:game:{gameId}` holds the complete game state as a JSON string, while `chess:player:game:{username}` provides a reverse lookup from player to their current game.

- *Tournaments* (`chess:tournament:*`): Groups all tournament-related data under a common prefix. Each tournament has metadata at `chess:tournament:{tournamentId}:data` (JSON string), a subscriber set at `chess:tournament:{tournamentId}:subscribers`, and per-player game counters at `chess:tournament:{tournamentId}:player:{username}:games`. These keys have no TTL and are instead cleaned up by the `TournamentScheduler` when a tournament finishes.

- *Openings* (`chess:opening:*`): Caches chess opening data for real-time detection during games. Keys follow the pattern `chess:opening:{normalizedFen}` where the FEN is normalized to include only board position and side to move. These are static reference data with no expiration.

=== Data Structures

*LiveGameState* (stored as JSON String):

```json
{
  "gameId": "uuid",
  "whitePlayer": "player1",
  "blackPlayer": "player2",
  "fen": "rnbqkbnr/pppppppp/...",
  "status": "IN_PROGRESS",
  "moveHistory": ["e4", "e5", "Nf3"],
  "detectedOpening": "Italian Game",
  "createdAt": 1699900000,
  "lastMoveAt": 1699900100
}
```

The `fen` field contains the current board position in FEN Notation, a standard chess position encoding. The `moveHistory` array preserves the complete game for eventual persistence to MongoDB. The `detectedOpening` field is updated as moves are played until the game exits known opening theory (first 10 moves). Timestamps enable timeout detection and analytics on game duration.

*LiveTournamentData* (stored as JSON String):

Tournament configuration is stored as JSON strings, consistent with the LiveGameState storage pattern:

```json
{
  "status": "active",
  "minRating": 1000,
  "maxRating": 2000,
  "maxParticipants": 32,
  "finishTime": 1699900000
}
```

This structure allows the application to validate tournament eligibility and subscription status without querying MongoDB. The JSON object is serialized when the tournament is created and remains static until the tournament finishes.

#pagebreak()


= Implementation

== Framework and Project Structure

Square64 is implemented as a Spring Boot application leveraging the mature Spring ecosystem for database connectivity, security, and web services.

*Technology Stack:*
- *Spring Boot*: The foundation framework providing dependency injection, configuration management, and application structure.
- *Maven*: The build system manages dependencies and provides a standardized project structure. Maven's dependency management ensures consistent library versions across development and deployment.
- *Spring Data MongoDB, Neo4j, Redis*: These Spring Data modules provide consistent repository abstractions across all three databases. Developers work with familiar interfaces while Spring handles connection management and query translation.
- *Spring Security with JWT*: The security framework handles authentication and authorization. JWT integration enables stateless authentication suitable for distributed deployment.
- *ChessLib 1.3.4*: This specialized library provides chess move validation, position management, and opening detection. It ensures all moves conform to chess rules without requiring custom validation logic.
- *SpringDoc OpenAPI*: Automatic API documentation generation from controller annotations. Developers can explore and test endpoints through the Swagger UI without maintaining separate documentation.

*Project Structure:*

The `config` package contains configuration classes for each database connection plus security configuration. Each database has its own configuration bean managing connection parameters, authentication, and connection pooling.

The `controller` package exposes REST endpoints organized by feature area (users, games, clubs, tournaments). Controllers delegate to services for business logic and handle HTTP-specific concerns like request parsing and response formatting.

The `dto` package contains Data Transfer Objects used for API requests and responses. These objects are separate from domain models, allowing API contracts to evolve independently of internal data structures.

The `model` packages define domain entities mapped to their respective databases. MongoDB models use Spring Data annotations for document mapping, while Neo4j models use relationship annotations for graph mapping.

The `repository` packages extend Spring Data repository interfaces, adding custom query methods where needed. MongoDB repositories include aggregation pipeline definitions, while Neo4j repositories contain Cypher queries.

The `service` package defines interfaces for business operations, with implementations in the `impl` subpackage. This separation enables testing with mock implementations and allows alternative implementations without changing dependent code.

The `scheduler` package contains scheduled tasks like tournament status updates, configured using Spring's scheduling support.

== MongoDB Operations

=== User Repository

The user repository extends Spring Data's MongoRepository with custom query methods:

```java
Optional<User> findByUsername(String username);
boolean existsByUsername(String username);
```

The `findByUsername` method enables user lookup during authentication and profile viewing. Spring Data automatically generates the query from the method name. The `existsByUsername` method provides an efficient existence check for username validation during registration without loading the full document.

==== User defined anylitics aggregations

Square64 allows users to calculate user specific statistics, in particular their scope begins and ends at the user specific document inside the "users" collection, this is in contraposition to the computational heavy aggregation pipelines\ in the next session.

These rapresent different endpoint in the effective implementation of Square64, but their execution are fundomental for displaying user profiles.

*User tilt*

```java
@Aggregation(pipeline = {
  // Stage 1: Filter out placeholder games (where _id is null)
  """
  { '$project': {
      'username': 1,
      'realGames': {
        '$filter': {
          'input': '$games',
          'as': 'game',
          'cond': { '$and': [
            { '$ne': ['$$game._id', null] },
            { '$gt': ['$$game._id', null] }
          ]}
        }
      }
  }}
  """,
  // Stage 2: Only consider users with at least 3 real games
  """
  { '$match': {
      '$expr': { '$gte': [{ '$size': '$realGames' }, 3] }
  }}
  """,
  // Stage 3: Get the last 3 real games
  """
  { '$project': {
      'username': 1,
      'recentGames': { '$slice': ['$realGames', -3] }
  }}
  """,
  // Stage 4: Check if user lost each of the 3 games
  """
  { '$project': {
      'username': 1,
      'recentGames': 1,
      'winResults': {
        '$map': {
          'input': '$recentGames',
          'as': 'game',
          'in': { '$eq': ['$$game.winner', '$username'] }
        }
      }
  }}
  """,
  // Stage 5: Match users who lost all 3 games
  """
  { '$match': {
      'winResults': { '$not': { '$elemMatch': { '$eq': true }}}
  }}
  """,
  // Stage 6: Project final result
  """
  { '$project': {
      '_id': 0,
      'username': 1,
      'status': { '$literal': 'ON TILT' }
  }}
  """
})
List<TiltPlayerDTO> findTiltPlayers();
```

"Tilting" means that a player is in a losing streak. This aggregation filters out placeholder games, takes the last 3 real games, and identifies users who lost all of them.

*User most used openings*

```java
@Aggregation(pipeline = {
  "{ '$match': { '_id': ?0 } }",
  "{ '$unwind': '$games' }",
  // Filter out placeholder games (where _id is null)
  "{ '$match': { 'games._id': { '$ne': null } } }",
  "{ '$group': { '_id': '$games.opening', 'count': { '$sum': 1 } } }",
  "{ '$sort': { 'count': -1 } }",
  "{ '$limit': 1 }",
  "{ '$project': { 'opening': '$_id', 'count': 1, '_id': 0 } }"
})
UserFavoriteOpeningDTO calcFavoriteOpening(String userId);
```

Operates on the buffered game digests in the user document, displays the most used opening of the user in their last 50 games. Placeholder games are filtered out before aggregation.

*User win rate*

```java
@Aggregation(pipeline = {
  "{ '$match': { '_id': ?0 } }",
  "{ '$unwind': '$games' }",
  """
  { '$match': {
      'games._id': { '$ne': null },
      'games.winner': { '$ne': null }
  }}
  """,
  """
  { '$group': {
      '_id': '$_id',
      'username': { '$first': '$username' },
      'totalGames': { '$sum': 1 },
      'wins': {
        '$sum': {
          '$cond': [
            { '$eq': [
              { '$toLower': '$games.winner' },
              { '$toLower': '$username' }
            ]},
            1,
            0
          ]
        }
      }
  }}
  """,
  """
  { '$project': {
      'winRate': {
        '$multiply': [{ '$divide': ['$wins', '$totalGames'] }, 100]
      }
  }}
  """
})
UserWinRateDTO calcUserWinRate(String userId);
```

Displays user win rate, a value between 0 and 100%, based on the user's last 50 played games. Filters out placeholder games (null id) and games without a winner before calculating.

=== Game Repository - Aggregation Pipelines

*Monthly Top Openings by Rating*

```java
@Aggregation(pipeline = {
  // Stage 1: Filter by date range and minimum ratings
  """
  { '$match': {
      '$and': [
        { 'end_time': { '$gte': ?2, '$lt': ?3 } },
        { '$or': [
          { 'white_rating': { '$gte': ?0 } },
          { 'black_rating': { '$gte': ?1 } }
        ]}
      ]
  }}
  """,
  // Stage 2: Parse date string to Date object
  """
  { '$addFields': {
      'parsedDate': {
        '$dateFromString': {
          'dateString': '$end_time',
          'format': '%Y-%m-%d %H:%M:%S'
        }
      }
  }}
  """,
  // Stage 3: Group by month, year, and opening
  """
  { '$group': {
      '_id': {
        'month': { '$month': '$parsedDate' },
        'year': { '$year': '$parsedDate' },
        'opening': '$opening'
      },
      'count': { '$sum': 1 }
  }}
  """,
  // Stage 4: Sort by date and count
  "{ '$sort': { '_id.year': 1, '_id.month': 1, 'count': -1 } }",
  // Stage 5: Get top opening per month/year
  """
  { '$group': {
      '_id': { 'month': '$_id.month', 'year': '$_id.year' },
      'most_used_opening': { '$first': '$_id.opening' },
      'usage_count': { '$first': '$count' }
  }}
  """,
  // Stage 6: Project final result
  """
  { '$project': {
      '_id': 0,
      'year': '$_id.year',
      'month': '$_id.month',
      'mostUsedOpening': '$most_used_opening',
      'usageCount': '$usage_count'
  }}
  """
})
List<MonthlyOpeningStatDTO> getMonthlyTopOpenings(
    int minWhite, int minBlack, String startDate, String endDate);
```

This pipeline filters games by date range and minimum rating thresholds, parses the date strings, groups by month/year and opening to count occurrences, then selects the most used opening per month. Note: only one opening is selected per month/year; in case of ties, a random one is selected. Dates are stored as strings in format "yyyy-MM-dd HH:mm:ss" for lexicographic comparison.

*Average ELO for Opening*

```java
@Aggregation(pipeline = {
  // Stage 1: Filter by opening and date range
  """
  { '$match': {
      'opening': ?0,
      'end_time': { '$gte': ?1, '$lt': ?2 }
  }}
  """,
  // Stage 2: Calculate average rating per game
  """
  { '$project': {
      'avg_game_rating': { '$avg': ['$white_rating', '$black_rating'] }
  }}
  """,
  // Stage 3: Calculate overall average
  """
  { '$group': {
      '_id': null,
      'finalAverageElo': { '$avg': '$avg_game_rating' }
  }}
  """
})
AverageEloResult getAverageEloForOpening(
    String opening, String startDate, String endDate);
```

This pipeline computes the average rating of players using a specific opening within a date range. It first calculates the average rating per game (between white and black), then averages across all matching games. This indicates the typical skill level where an opening appears, useful for recommending openings to players at different levels.

*Win Rate by Opening*

```java
@Aggregation(pipeline = {
  // Stage 1: Filter by rating range and time class
  """
  { '$match': {
      'white_rating': { '$gte': ?0, '$lte': ?1 },
      'time_class': ?2
  }}
  """,
  // Stage 2: Project opening and win indicator
  """
  { '$project': {
      'opening': 1,
      'white_won': {
        '$cond': [{ '$eq': ['$result_white', 'win'] }, 1, 0]
      }
  }}
  """,
  // Stage 3: Group by opening with stats
  """
  { '$group': {
      '_id': '$opening',
      'totalGames': { '$sum': 1 },
      'whiteWins': { '$sum': '$white_won' }
  }}
  """,
  // Stage 4: Filter by minimum games
  "{ '$match': { 'totalGames': { '$gte': ?3 } } }",
  // Stage 5: Calculate win percentage
  """
  { '$project': {
      'opening': '$_id',
      '_id': 0,
      'totalGames': 1,
      'winPercentage': {
        '$multiply': [
          { '$divide': ['$whiteWins', '$totalGames'] },
          100
        ]
      }
  }}
  """,
  // Stage 6: Sort by win percentage
  "{ '$sort': { 'winPercentage': -1 } }"
})
List<WinRateByOpeningDTO> getWinRateByOpening(
    int minRating, int maxRating, String timeClass, int minGames);
```

This pipeline analyzes white's win rate for each opening within a rating range and time class. It filters games by rating bounds and time class, groups by opening counting total games and white wins, filters out openings with fewer games than the minimum threshold, and returns the win percentage sorted from highest to lowest.

=== Tournament Repository

Tournament queries support the scheduling system:

```java
List<Tournament> findActiveTournamentsToFinish(LocalDateTime now);
```

This query finds tournaments with status "active" whose finish time has passed, enabling the scheduler to update their status appropriately.


== Neo4j Operations

=== User Node Repository

Neo4j repositories use Cypher queries for graph operations:

*Friend Suggestions via Graph Traversal*
```cypher
MATCH (me:USER {mongo_id: "6970a8eafdb64c9b443d55de"})
            MATCH (me)-[:FOLLOWS|JOINED|PARTECIPATED*2]-(consigliato:USER)
            WHERE consigliato <> me
              AND NOT (me)-[:FOLLOWS]->(consigliato)
            WITH me, consigliato
            OPTIONAL MATCH (me)-[:FOLLOWS]->(amico:USER)-[:FOLLOWS]->(consigliato)
            WITH me, consigliato, collect(DISTINCT amico.name) AS amiciInComune
            OPTIONAL MATCH (me)-[:JOINED|PARTECIPATED]->(comune)<-[:JOINED|PARTECIPATED]-(consigliato)
            WITH me, consigliato, amiciInComune, collect(DISTINCT labels(comune)[0]) AS interessiLabels
            RETURN
                consigliato.mongo_id AS mongoID,
                consigliato.name AS name,
                CASE
                    WHEN size(amiciInComune) > 0 THEN "Seguito da " + amiciInComune[0] + (CASE WHEN size(amiciInComune) > 1 THEN " + altri" ELSE "" END)
                    WHEN size(interessiLabels) > 0 THEN "Insieme in " + interessiLabels[0]
                    ELSE "Suggerito per te"
                END AS connectionType
            ORDER BY size(amiciInComune) DESC, size(interessiLabels) DESC
            LIMIT 10
```

This query finds friend suggestions through two paths: users followed by people you follow (friend-of-friend), and users in the same clubs, or have participated in the same tournament. Filtering ensures suggestions aren't already followed. The `connectionType` return value explains why each user was suggested.

#pagebreak()

*Club Membership with Stats*
```cypher
MATCH (u:USER {mongo_id: $userId})-[r:JOINED]->(c:CLUB {mongo_id: $clubId})
RETURN c, r.country, r.bullet, r.blitz, r.rapid
```

This retrieves a user's club membership including the rating snapshot stored on the relationship. The relationship properties capture the user's ratings updated with every game the user concludes.

=== Club Node Repository

Club queries support leaderboard and member listing:

```cypher
MATCH (p)-[r:JOINED]->(c)
            WHERE c.name STARTS WITH $name
            RETURN
                p.mongo_id AS id,
                p.name AS name,
                r.country AS country,
                r.bullet AS bulletRating,
                r.blitz AS blitzRating,
                r.rapid AS rapidRating
```

This lists all club members.

=== Tournament Node Repository

Tournament queries retrieve participation information:

```cypher
MATCH (u:USER)-[r:PARTECIPATED]->(t:TOURNAMENT {mongo_id: $tournamentId})
RETURN u.name, r.wins, r.draws, r.losses, r.placement
ORDER BY r.placement ASC
```

This returns the final standings for a tournament, ordered by placement. The relationship properties store aggregated performance data.

== Redis Operations

Redis operations in Square64 are implemented through Spring Data Redis, which provides a high-level abstraction over the Redis protocol. The `StringRedisTemplate` class offers type-safe operations for different Redis data structures, with methods organized by data type: `opsForValue()` for strings, `opsForList()` for lists, and `opsForSet()` for sets.

=== Matchmaking Service

The matchmaking system demonstrates how Redis lists naturally model queue-based workflows. Square64 supports two types of matchmaking: regular matchmaking (by game type) and tournament matchmaking.

*Regular Matchmaking*

When a player requests to join regular matchmaking, the service validates the game type (bullet, blitz, or rapid) and checks whether the player already has an active game. The queue key follows the pattern `chess:matchmaking:queue:{gameType}`. If an opponent is already waiting, they are immediately paired; otherwise, the requesting player joins the queue and polls for a match.

*Tournament Matchmaking*

Tournament matchmaking operates similarly but includes additional validation: the player must be subscribed to the tournament, and they must not have exceeded the maximum games per player limit (8 games by default). The queue key follows the pattern `chess:matchmaking:tournament:{tournamentId}`.

The core matching logic operates as follows:

```java
String opponent = redisTemplate.opsForList().leftPop(queueKey);
if (opponent != null && !opponent.equals(username)) {
    // Match found - create game between opponent and username
    return createGameForMatch(username, opponent, tournamentId, queueKey);
} else {
    // No opponent available - add to queue and poll for match
    redisTemplate.opsForList().rightPush(queueKey, username);
    // ... polling logic (500ms intervals, 60s timeout) ...
}
```

The `leftPop` operation atomically removes and returns the first element from the queue. If a different player is found, a game is created immediately. The atomic nature of this operation prevents race conditions where two players might both pop each other simultaneously. When no opponent is available, the player is added to the queue's tail using `rightPush`, ensuring FIFO ordering where players who have waited longest are matched first.

The polling mechanism checks every 500ms whether a `chess:player:game:{username}` key has been created (indicating the player was matched by another user who popped them from the queue). The polling timeout is configurable (default: 60 seconds).

Players may also leave the queue before finding a match, which requires removing their username from the list:

```java
redisTemplate.opsForList().remove(queueKey, 0, username);
```

The second parameter (0) instructs Redis to remove all occurrences of the username, handling edge cases where network issues might have caused duplicate entries.

=== Live Game Management

Game state management requires both fast reads and writes, as every move involves reading the current position, validating the move, and persisting the updated state. The service serializes the complete game state to JSON and stores it as a simple string value.

When a game is created, both the game state and player mappings are established:

```java
// Store game state with TTL
String json = objectMapper.writeValueAsString(gameState);
redisTemplate.opsForValue().set(
    GAME_STATE_PREFIX + gameState.getGameId(),
    json,
    gameExpirationHours,
    TimeUnit.HOURS
);

// Map both players to the game
redisTemplate.opsForValue().set(PLAYER_GAME_PREFIX + whitePlayer, gameId, ...);
redisTemplate.opsForValue().set(PLAYER_GAME_PREFIX + blackPlayer, gameId, ...);
```

For tournament games, the game counter for each player is also incremented:

```java
private void incrementTournamentGameCount(String tournamentId, String username) {
    String key = TOURNAMENT_GAME_COUNT_PREFIX + tournamentId + ":player:" + username + ":games";
    redisTemplate.opsForValue().increment(key);
}
```

Note: Tournament game counters do not have a TTL set during increment. They are cleaned up by the `TournamentScheduler` when the tournament finishes.
The player-to-game mapping enables O(1) lookup when a player attempts to join matchmaking, preventing them from starting a new game while one is already in progress.

Processing a move involves retrieving the current state, applying the validated move, and saving the updated state:

```java
LiveGameState gameState = getGameState(gameId);
// Validate move using ChessLib
Board board = new Board();
board.loadFromFen(gameState.getFen());
board.doMove(parsedMove);

// Update state
gameState.setFen(board.getFen());
gameState.setLastMove(move);
gameState.setLastMoveAt(System.currentTimeMillis());
gameState.addMove(move);

// Detect opening (within configured move limit)
if (gameState.getMoveCount() <= maxMoveCheckForOpening) {
    detectOpening(gameState, board.getFen());
}

saveGameState(gameState);
```

The `lastMoveAt` timestamp enables detection of abandoned games and provides data for analytics on game duration and player activity patterns.

When a game ends (checkmate, stalemate, draw, or resignation), the system persists the game to MongoDB, updates user statistics, and cleans up Redis keys:

```java
// Save completed game to MongoDB via GameService
gameService.createGame(gameDTO);

// Buffer game to user documents (updates ELO for regular games only)
if (!gameState.isTournamentGame()) {
    userService.bufferGame(whiteUser.getId(), summary, timeClass);
    userService.bufferGame(blackUser.getId(), summary, timeClass);
}

// Cleanup Redis
redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getWhitePlayer());
redisTemplate.delete(PLAYER_GAME_PREFIX + gameState.getBlackPlayer());
```

For tournament games, the `TournamentService.bufferTournamentGame()` method is called to update the tournament document in MongoDB and the participation stats (wins, draws, losses) in Neo4j.

=== Opening Detection

During the opening phase of each game (configurable via `chess.openings.max-move-check`, default: 30 moves), the system queries the opening cache to identify the current position. The `OpeningServiceImpl` handles the lookup:

```java
public ChessOpening findOpeningByFen(String fen) {
    String normalizedFen = normalizeFen(fen);
    String result = redisTemplate.opsForValue().get(KEY_PREFIX + normalizedFen);

    if (result != null) {
        JsonNode node = objectMapper.readTree(result);
        return new ChessOpening(
            node.path("eco").asText(),
            node.path("name").asText(),
            null, null
        );
    }
    return null;
}
```

The FEN normalization extracts only the board position and side to move from the full FEN string (e.g., `rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b` from `rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1`). This ensures that positions reached through different move orders (transpositions) map to the same cache entry, and ignores castling rights and en passant which don't affect opening classification.

When a match is found, the opening name and ECO code are stored in the game state and displayed to players in real-time. The `LiveGameServiceImpl` calls this service during move processing:

```java
private void detectOpening(LiveGameState gameState, String fen) {
    ChessOpening opening = openingService.findOpeningByFen(fen);
    if (opening != null) {
        gameState.setDetectedOpening(opening.getName());
        gameState.setDetectedOpeningEco(opening.getEco());
    }
}
```

=== Tournament Subscriptions

Tournament subscription management leverages Redis strings (for JSON data) and sets for efficient validation and membership tracking.

*Tournament Creation*

When a tournament is created, the service stores tournament metadata as a JSON string and initializes an empty subscribers set:

```java
// Store tournament data in Redis as JSON string
LiveTournamentData liveTournamentData = LiveTournamentData.fromTournament(
    createdTournament.getStatus(),
    createdTournament.getMinRating(),
    createdTournament.getMaxRating(),
    createdTournament.getMaxParticipants(),
    createdTournament.getFinishTime()
);
saveLiveTournamentData(createdTournament.getId(), liveTournamentData);

// Redis Set for subscribers will be populated when users subscribe
```

*Subscribing to Tournaments*

Subscription involves multiple validations and uses an add-then-check pattern to avoid race conditions:

```java
// Get tournament data from Redis JSON string
LiveTournamentData tournamentData = getLiveTournamentData(tournamentId);

// Check subscription window (6 days before finish)
validateSubscriptionWindow(tournamentData.getFinishTime());

// Check user's bullet elo is within tournament rating range
int minRating = tournamentData.getMinRating();
int maxRating = tournamentData.getMaxRating();

// Add first, then check - avoids race condition where multiple users
// could pass the count check simultaneously and exceed maxParticipants
int maxParticipants = tournamentData.getMaxParticipants();
Long added = redisTemplate.opsForSet().add(subscribersKey, username);

if (added == null || added == 0) {
    // User was already in set
    throw new BusinessException("You are already subscribed to this tournament");
}

// Now check if we exceeded the limit
Long currentCount = redisTemplate.opsForSet().size(subscribersKey);
if (currentCount != null && currentCount > maxParticipants) {
    // Over limit - remove ourselves and reject
    redisTemplate.opsForSet().remove(subscribersKey, username);
    throw new BusinessException("Tournament has reached maximum participants");
}
```

The return value indicates whether the element was newly added (1) or already existed (0). By adding first and then checking the count, we avoid a potential race condition that would occur if we checked the count before adding.

*Unsubscribing from Tournaments*

Users can unsubscribe from a tournament only if they haven't played any games yet:

```java
// Get tournament data from Redis JSON string
LiveTournamentData tournamentData = getLiveTournamentData(tournamentId);

// Check subscription window is still open
validateSubscriptionWindow(tournamentData.getFinishTime());

// Check if user has already played a game in this tournament
String gameCountKey = TOURNAMENT_PREFIX + tournamentId + ":player:" + username + ":games";
String gameCountStr = redisTemplate.opsForValue().get(gameCountKey);
int gameCount = gameCountStr != null ? Integer.parseInt(gameCountStr) : 0;
if (gameCount > 0) {
    throw new BusinessException("Cannot unsubscribe: you have already played games in this tournament");
}

// Remove from Redis Set
redisTemplate.opsForSet().remove(subscribersKey, username);
```

*Tournament Matchmaking Validation*

Before allowing a player to join tournament matchmaking, the `LiveGameServiceImpl` verifies subscription and game limits:

```java
// Check if player is subscribed to the tournament
String subscribersKey = "chess:tournament:" + tournamentId + ":subscribers";
Boolean isSubscribed = redisTemplate.opsForSet().isMember(subscribersKey, username);
if (!Boolean.TRUE.equals(isSubscribed)) {
    throw new BusinessException("You are not subscribed to this tournament");
}

// Check game count against limit (default: 8 games per player)
int gameCount = getTournamentGameCount(tournamentId, username);
if (gameCount >= maxTournamentGames) {
    throw new BusinessException("You have reached the maximum of " + maxTournamentGames + " games in this tournament");
}
```

*Tournament Cleanup*

The tournament scheduler runs daily and cleans up all Redis data for finished tournaments:

```java
// Delete subscribers set
String subscribersKey = TOURNAMENT_PREFIX + tournament.getId() + TOURNAMENT_SUBSCRIBERS_SUFFIX;
redisTemplate.delete(subscribersKey);

// Delete tournament data key
String dataKey = TOURNAMENT_PREFIX + tournament.getId() + TOURNAMENT_DATA_SUFFIX;
redisTemplate.delete(dataKey);

// Delete all player game counters (pattern: chess:tournament:{id}:player:*:games)
Set<String> counterKeys = redisTemplate.keys(TOURNAMENT_GAME_COUNT_PREFIX + tournamentId + ":player:*:games");
if (counterKeys != null && !counterKeys.isEmpty()) {
    redisTemplate.delete(counterKeys);
}
```

This cleanup removes all three types of tournament-related keys: the subscribers set, the tournament metadata JSON, and all per-player game counters.

== RESTful API Documentation

Square64 exposes a comprehensive REST API that allows clients to interact with all platform features. The API follows REST conventions with JSON request/response bodies and standard HTTP methods. All responses are wrapped in a consistent `ResponseWrapper` structure containing a message and data payload.

*Base URL:* `http://localhost:8080`

*Authentication:* Most endpoints require JWT authentication. Include the token in the `Authorization` header as `Bearer <token>`.

*Response Format:*
```json
{
  "message": "Operation successful",
  "data": { ... }
}
```

=== User Endpoints

#table(
  columns: (auto, auto, auto, auto),
  inset: 6pt,
  align: left,
  table.header([*Method*], [*Endpoint*], [*Auth*], [*Description*]),
  [GET], [`/users`], [Public], [List all users (paginated)],
  [GET], [`/users/{id}`], [Public], [Get user by ID],
  [POST], [`/users/register`], [Public], [Register new user],
  [POST], [`/users/login`], [Public], [Login and get JWT token],
  [POST], [`/users/{id}/edit`], [Owner/Admin], [Update user profile],
  [DELETE], [`/users/{id}`], [Admin], [Delete user],
  [POST], [`/users/promote`], [Admin], [Promote user to admin],
  [GET], [`/users/{userId}/win_rate`], [Public], [Get user win rate statistics],
  [GET], [`/users/{userId}/most_used_opening`], [Public], [Get user's favorite opening],
  [GET], [`/users/stats/tilt`], [Public], [Get players on tilt (3 consecutive losses)],
  [POST], [`/users/{userName}/clubs/{clubName}`], [User], [Join a club],
  [POST], [`/users/{sourceId}/follows/{targetId}`], [User], [Follow another user],
  [POST], [`/users/{sourceId}/unfollows/{targetId}`], [User], [Unfollow a user],
  [GET], [`/users/{userId}/follows/suggestions`], [User], [Get friend suggestions],
  [GET], [`/users/{userId}/follows`], [User], [Get users followed by user],
  [GET], [`/users/{userId}/followers`], [User], [Get user's followers],
  [GET], [`/users/{userId}/tournaments`], [User], [Get user's tournament history],
)

*Example: User Registration*
```bash
curl -X POST http://localhost:8080/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "magnus_fan",
    "name": "Chess Enthusiast",
    "password": "securePass123",
    "mail": "user@example.com",
    "country": "IT"
  }'
```

Response:
```json
{
  "message": "User created successfully",
  "data": {
    "id": "6789abc...",
    "username": "magnus_fan",
    "name": "Chess Enthusiast",
    "country": "IT",
    "stats": { "bullet": 1200, "blitz": 1200, "rapid": 1200 }
  }
}
```

*Example: User Login*
```bash
curl -X POST http://localhost:8080/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "magnus_fan",
    "password": "securePass123"
  }'
```

Response:
```json
{
  "message": "Login successful",
  "data": "eyJhbGciOiJIUzI1NiJ9..."
}
```

*Example: Follow User*
```bash
curl -X POST http://localhost:8080/users/magnus_fan/follows/hikaru_fan \
  -H "Authorization: Bearer <token>"
```

*Example: Get Friend Suggestions*
```bash
curl -X GET "http://localhost:8080/users/magnus_fan/follows/suggestions?userId=magnus_fan" \
  -H "Authorization: Bearer <token>"
```

Response:
```json
{
  "message": "User friends suggestions include",
  "data": [
    { "username": "chess_master", "mutualConnections": 5 },
    { "username": "blitz_king", "mutualConnections": 3 }
  ]
}
```

=== Game Endpoints

#table(
  columns: (auto, auto, auto, auto),
  inset: 6pt,
  align: left,
  table.header([*Method*], [*Endpoint*], [*Auth*], [*Description*]),
  [GET], [`/games`], [Public], [List all games (paginated)],
  [GET], [`/games/{id}`], [Public], [Get game by ID],
  [GET], [`/games/user/{username}`], [Public], [Get games by username],
  [POST], [`/games/{id}/edit`], [Admin], [Edit game],
  [DELETE], [`/games/{id}`], [Admin], [Delete game],
  [GET], [`/games/stats/top-openings`], [User], [Get monthly top openings],
  [GET], [`/games/stats/average-elo`], [User], [Get average Elo for an opening],
  [GET], [`/games/stats/win-rate-by-opening`], [User], [Get win rate statistics by opening],
  [POST], [`/games/live/matchmaking`], [User], [Join matchmaking queue],
  [POST], [`/games/live/matchmaking/tournament/{id}`], [User], [Join tournament matchmaking],
  [DELETE], [`/games/live/matchmaking`], [User], [Leave matchmaking queue],
  [DELETE], [`/games/live/matchmaking/tournament/{id}`], [User], [Leave tournament matchmaking],
  [GET], [`/games/live/{gameId}/status`], [Public], [Get live game status],
  [POST], [`/games/live/{gameId}/move`], [User], [Make a move],
  [POST], [`/games/live/{gameId}/resign`], [User], [Resign from game],
)

*Example: Get User Games*
```bash
curl -X GET http://localhost:8080/games/user/magnus_fan
```

Response:
```json
{
  "message": "Games retrieved successfully",
  "data": [
    {
      "id": "game123",
      "whitePlayer": "magnus_fan",
      "blackPlayer": "opponent",
      "whiteRating": 1500,
      "blackRating": 1480,
      "opening": "Sicilian Defense",
      "timeClass": "blitz",
      "endTime": "2024-01-15T14:30:00Z"
    }
  ]
}
```

*Example: Get Top Openings*
```bash
curl -X GET "http://localhost:8080/games/stats/top-openings?year=2024&month=1" \
  -H "Authorization: Bearer <token>"
```

*Example: Get Win Rate by Opening*
```bash
curl -X GET "http://localhost:8080/games/stats/win-rate-by-opening?minRating=1400&maxRating=1800&timeClass=blitz&minGames=10" \
  -H "Authorization: Bearer <token>"
```

Response:
```json
{
  "message": "Win rate by opening retrieved successfully",
  "data": [
    { "opening": "Italian Game", "winRate": 0.54, "totalGames": 1250 },
    { "opening": "Sicilian Defense", "winRate": 0.51, "totalGames": 2100 }
  ]
}
```

*Example: Join Matchmaking*
```bash
curl -X POST http://localhost:8080/games/live/matchmaking \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{ "gameType": "blitz" }'
```

Response (match found):
```json
{
  "message": "Match found! Game created.",
  "data": {
    "gameId": "live-game-abc123",
    "whitePlayer": "magnus_fan",
    "blackPlayer": "opponent",
    "matched": true
  }
}
```

Response (no match found):
```json
{
  "message": "No opponent found. Removed from matchmaking queue.",
  "data": {
    "matched": false,
    "message": "No opponent found. Removed from queue."
  }
}
```

*Example: Make a Move*
```bash
curl -X POST http://localhost:8080/games/live/live-game-abc123/move \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{ "move": "e2e4" }'
```

Response:
```json
{
  "message": "Move made successfully",
  "data": {
    "success": true,
    "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
    "outcome": "CONTINUE"
  }
}
```

*Example: Get Game Status*
```bash
curl -X GET http://localhost:8080/games/live/live-game-abc123/status
```

Response:
```json
{
  "message": "Game status retrieved successfully",
  "data": {
    "gameId": "live-game-abc123",
    "fen": "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1",
    "currentTurn": "black",
    "whitePlayer": "magnus_fan",
    "blackPlayer": "opponent",
    "gameStatus": "IN_PROGRESS",
    "detectedOpening": "King's Pawn Opening",
    "detectedOpeningEco": "B00"
  }
}
```

=== Club Endpoints

#table(
  columns: (auto, auto, auto, auto),
  inset: 6pt,
  align: left,
  table.header([*Method*], [*Endpoint*], [*Auth*], [*Description*]),
  [GET], [`/clubs`], [Public], [List all clubs (paginated)],
  [GET], [`/clubs/{name}`], [Public], [Get club by name],
  [POST], [`/clubs`], [User], [Create new club],
  [POST], [`/clubs/{id}/edit`], [Owner/Admin], [Update club],
  [DELETE], [`/clubs/{id}`], [Admin], [Delete club],
)

*Example: Create Club*
```bash
curl -X POST http://localhost:8080/clubs \
  -H "Authorization: Bearer <token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Italian Chess Masters",
    "description": "A club for Italian chess enthusiasts",
    "country": "IT"
  }'
```

Response:
```json
{
  "message": "Club created successfully",
  "data": {
    "id": "club123",
    "name": "Italian Chess Masters",
    "description": "A club for Italian chess enthusiasts",
    "country": "IT",
    "admin": "magnus_fan",
    "members": []
  }
}
```

=== Tournament Endpoints

#table(
  columns: (auto, auto, auto, auto),
  inset: 6pt,
  align: left,
  table.header([*Method*], [*Endpoint*], [*Auth*], [*Description*]),
  [GET], [`/tournaments`], [Public], [List all tournaments (paginated)],
  [GET], [`/tournaments/active`], [Public], [List active tournaments],
  [GET], [`/tournaments/{id}`], [Public], [Get tournament by ID],
  [GET], [`/tournaments/{id}/participants`], [Public], [Get tournament participants],
  [POST], [`/tournaments/create`], [Admin], [Create tournament],
  [POST], [`/tournaments/{id}/edit`], [Admin], [Update tournament],
  [DELETE], [`/tournaments/{id}`], [Admin], [Delete tournament],
  [POST], [`/tournaments/{id}/register`], [User], [Register for tournament],
  [POST], [`/tournaments/{id}/unregister`], [User], [Unregister from tournament],
)

*Example: Create Tournament*
```bash
curl -X POST http://localhost:8080/tournaments/create \
  -H "Authorization: Bearer <admin-token>" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "Winter Blitz Championship",
    "description": "Annual winter tournament for intermediate players",
    "minRating": 1200,
    "maxRating": 1600,
    "maxParticipants": 32,
    "timeControl": "blitz",
    "finishTime": "2024-02-15T18:00:00Z"
  }'
```

Response:
```json
{
  "message": "Tournament created successfully",
  "data": {
    "id": "tournament456",
    "name": "Winter Blitz Championship",
    "status": "ACTIVE",
    "minRating": 1200,
    "maxRating": 1600,
    "maxParticipants": 32
  }
}
```

*Example: Register for Tournament*
```bash
curl -X POST http://localhost:8080/tournaments/tournament456/register \
  -H "Authorization: Bearer <token>"
```

Response:
```json
{
  "message": "Successfully registered for tournament",
  "data": null
}
```

#pagebreak()



= Database Deployment

== Infrastructure Overview

#table(
  columns: (1fr, 1fr, 1fr, 1fr),
  [*VM*], [*MongoDB Role*], [*Redis Role*], [*Neo4j*],
  [Server 1 (10.1.1.48)], [Primary], [Slave], [-],
  [Server 2 (10.1.1.52)], [Secondary], [Slave], [Single instance],
  [Server 3 (10.1.1.50)], [Secondary], [Master], [-],
)

This co-location strategy provides redundancy for the most critical components. MongoDB and Redis both benefit from replication across all three nodes, while Neo4j—used primarily for read-heavy social graph queries—operates as a single instance.

== Indexes

Proper indexing is critical for query performance at scale. Each database requires indexes tailored to its access patterns. The following recommendations are derived from analyzing the actual queries executed by the application.

=== MongoDB Indexes

*Users Collection*

The users collection requires indexes to support authentication and profile lookups:

```javascript
db.users.createIndex({ "username": 1 }, { unique: true })
```

The `username` index is essential as it supports the `findByUsername` query used during authentication and profile viewing. The unique constraint ensures no duplicate usernames exist.

*Games Collection*

The games collection benefits from several indexes to support both user-facing queries and analytics aggregations:

```javascript
db.games.createIndex({ "white_player": 1 })
db.games.createIndex({ "black_player": 1 })
db.games.createIndex({ "opening": 1, "end_time": -1 })
db.games.createIndex({ "time_class": 1, "white_rating": 1 })
```

The player indexes support the `findByPlayer` query that retrieves a user's game history. Since games are queried by either color, separate single-field indexes outperform a compound index here. The compound `opening` + `end_time` index accelerates the average ELO aggregation for specific openings. The `time_class` + `white_rating` compound index supports the win rate by opening aggregation that filters first the `time_class` property and then filtering for the `white_rating`.

A quick comparison demonstretes the `obvious` performance advatages of the implemented indexes:

*Win rate by opening:*
- *Without index*: 183752 documents examined (all) & 169 ms execution time.
- *With index*: 38519 documents examined & 115 ms execution time

*Average elo by opening*:
- *Without index*: 183752 documents examined (all) & 133 ms execution time time.
- *With index*: 1 documents examined & 3 ms execution time. *Note*: The openings used for testing were very niche.

*Tournaments Collection*

Tournament queries focus on status and scheduling:

```javascript
db.tournaments.createIndex({ "status": 1, "finish_time": 1 })
```

This compound index directly supports the scheduler's query `{ 'status': 'active', 'finish_time': { $lte: currentTime } }` which finds tournaments ready to transition to "finished" status. The index allows MongoDB to efficiently filter by status first, then scan only active tournaments for finish time comparison.

*Clubs Collection*

Club lookups use name-based queries:

```javascript
db.clubs.createIndex({ "name": 1 }, { unique: true })
```

The unique index on `name` supports the `findByName` query and prevents duplicate club names.

=== Neo4j Indexes

Neo4j indexes accelerate node lookups in graph traversal queries. Since most queries begin by finding specific nodes before traversing relationships, indexes on node properties significantly improve performance:

```cypher
CREATE INDEX user_mongo_id FOR (u:USER) ON (u.mongo_id);
CREATE INDEX user_name FOR (u:USER) ON (u.name);
CREATE INDEX club_mongo_id FOR (c:CLUB) ON (c.mongo_id);
CREATE INDEX club_name FOR (c:CLUB) ON (c.name);
CREATE INDEX tournament_mongo_id FOR (t:TOURNAMENT) ON (t.mongo_id);
```

The `mongo_id` indexes are critical for cross-database queries where the application looks up Neo4j nodes using identifiers from MongoDB documents. The `name` indexes support queries that match users or clubs by their display names, such as the friend suggestions and club membership queries that use `STARTS WITH` predicates for partial matching.


== Replication

The production deployment is distributed across three virtual machine servers. This architecture balances fault tolerance, performance, and operational complexity. Each VM hosts both MongoDB and Redis instances, while Neo4j runs on a single dedicated server.

=== MongoDB Replica Set

MongoDB is deployed as a three-node replica set, providing both high availability and data durability. The replica set operates with one primary and two secondary nodes:

- *Primary node* receives all write operations and maintains the authoritative copy of data. All client writes are directed to the primary, which then replicates changes to all the secondaries.
- *Secondary nodes* continuously replicate data from the primary, maintaining near-real-time copies. They can serve read operations when configured with appropriate read preferences, distributing load for read-heavy workloads.
- *Automatic failover* occurs when the primary becomes unavailable. The secondaries detect the failure through heartbeat messages and conduct an election to promote one secondary to primary. This process completes within 10-12 seconds, minimizing downtime.

The three-node configuration ensures a majority quorum is always achievable even if one node fails, preventing split-brain scenarios where multiple nodes might believe they are primary.

*Write Concern and Read Preference Configuration*

The MongoDB connection is configured with specific consistency settings aligned with the CP (Consistency and Partition Tolerance) model:

```
mongodb://10.1.1.48:27017,10.1.1.50:27017,10.1.1.52:27017/square64?replicaSet=lsmdb&w=3&readPreference=nearest
```

- *Write Concern (`w=3`)*: Every write operation must be acknowledged by all three nodes (the primary and both secondaries) before returning success to the application. This is the strongest write consistency guarantee, ensuring that data is never lost even if multiple nodes fail immediately after a write. The trade-off is increased write latency, as the application must wait for all replicas to confirm the write.

- *Read Preference (`readPreference=nearest`)*: Read operations are directed to the node with the lowest network latency, regardless of whether it is the primary or a secondary. This optimizes read performance by reducing round-trip time. Since replication in MongoDB is asynchronous, reads from secondaries may return slightly stale data (typically milliseconds behind the primary). This is acceptable for Square64 because:
  - Critical real-time game state is managed by Redis, not MongoDB
  - User profile views and game history browsing can tolerate minimal staleness


=== Redis Master-Replica Replication

Redis is configured in a master-replica topology where one master handles all write operations and two replicas maintain synchronized copies for redundancy. The Spring application is configured to distribute reads across replicas.

*Application Configuration*

The Java application connects to Redis using a master-replica aware configuration:

```java
@Bean
public LettuceConnectionFactory redisConnectionFactory() {
    RedisStaticMasterReplicaConfiguration config =
        new RedisStaticMasterReplicaConfiguration(masterHost, masterPort);
    config.addNode(replica1Host, replica1Port);
    config.addNode(replica2Host, replica2Port);

    LettuceClientConfiguration clientConfig = LettuceClientConfiguration.builder()
        .readFrom(ReadFrom.REPLICA_PREFERRED)
        .commandTimeout(Duration.ofMillis(timeout))
        .build();

    return new LettuceConnectionFactory(config, clientConfig);
}
```

The `ReadFrom.REPLICA_PREFERRED` strategy directs read operations to replica nodes when available, distributing load across the cluster while ensuring writes go to the master. This improves read scalability for operations like opening lookups and subscription checks.

*Redis Server Configuration*

The Redis servers are configured with the following key parameters:

```
# Network - allow connections from all VMs
bind 0.0.0.0
port 6379
protected-mode no

# Memory Management
maxmemory 1gb
maxmemory-policy volatile-lru

# Persistence
appendonly yes
appendfsync everysec
save 900 1

# Performance
tcp-keepalive 60

# Replication (on replica nodes)
replicaof 10.1.1.50 6379
```

The `maxmemory-policy volatile-lru` setting ensures that when memory limits are reached, Redis evicts keys with expiration times set (volatile) using a least-recently-used algorithm. This is appropriate for Square64 because live game states and tournament counters all have TTLs, so they can be safely evicted under memory pressure while the opening cache (without TTL) remains stable.

The persistence configuration combines AOF (Append Only File) with periodic RDB snapshots. `appendonly yes` with `appendfsync everysec` provides a good balance between durability and performance: writes are logged to disk every second, limiting potential data loss to approximately one second of operations in a crash scenario. The `save 900 1` directive creates RDB snapshots every 15 minutes if at least one key changed, providing point-in-time recovery capability.

Replica nodes are configured with `replicaof 10.1.1.50 6379` to replicate from the master. Replication is asynchronous, meaning the master does not wait for replicas to acknowledge writes before responding to clients. This prioritizes write latency over strict consistency.


=== Neo4j Single Instance

Neo4j operates as a single instance rather than a cluster. This decision reflects several factors:

- *Read-heavy workload*: Social graph queries (friend suggestions, club memberships) are predominantly reads. The single instance can serve these efficiently with proper indexing.
- *Lower write volume*: Graph modifications (following users, joining clubs) occur less frequently than game operations, reducing the need for write scalability.

The tradeoff is reduced availability: if the Neo4j server fails, social features become unavailable until recovery. However, core gameplay (managed by Redis) and user data (in MongoDB) remain accessible.

=== CAP Theorem Considerations

As a competitive chess platform where game outcomes affect player ratings and tournament standings, Square64 leans toward the *CP (Consistency and Partition Tolerance)* model, prioritizing data correctness over availability where feasible:

- *Consistency for Live Game State*: During active play, both players observe the same board position. Redis atomic operations ensure that move updates are immediately visible to both participants through a single shared key.
- *Rating Updates*: ELO rating changes are triggered upon game completion. Each player's rating is updated via a single MongoDB update operation per user, reducing (though not eliminating) the risk of partial updates. The current implementation does not use distributed transactions, so edge cases involving failures between updates may result in temporary inconsistencies.
- *Tournament Statistics*: When a tournament game concludes, participant statistics in Neo4j are updated. These updates occur sequentially rather than within a distributed transaction, meaning that failures during the update process may not be fully propagated to the caller.

The following strategies are employed to improve consistency:

- *Sequential Cross-Database Updates*: Critical operations (game completion, rating updates) write to relevant databases in sequence. While this provides ordering guarantees, it does not guarantee atomicity across databases.
- *Write Concerns*: MongoDB is configured with a write concern of 3 (`w=3`), ensuring writes are acknowledged by multiple replica set members before returning success. This improves durability but does not address cross-database consistency.
- *Fail-Fast for Live Operations*: Redis operations for live games fail explicitly if the connection is unavailable, preventing games from starting or moves from being made in an inconsistent state. However, failures during post-game processing (MongoDB persistence, Neo4j updates) are currently logged rather than propagated as errors.

This approach provides reasonable consistency for the expected use case while acknowledging that full distributed transaction guarantees across MongoDB, Neo4j, and Redis are not implemented. For a platform of this scale, the trade-off between implementation complexity and consistency guarantees is acceptable, though operators should be aware that edge-case failures may require manual reconciliation.

=== Sharding Considerations

Based on the volume estimates (10,000 DAU, 2 games/user/day), we can evaluate whether sharding is necessary.

#table(
  columns: (auto, auto, auto),
  [*Metric*], [*Calculation*], [*Result*],
  [Daily new games], [10,000 users × 2 games ÷ 2 players], [10,000 games/day],
  [Yearly game growth], [10,000 × 365], [~3.65M games/year],
  [Average game document size], [moves + metadata], [~1.5 KB],
  [Yearly storage growth], [3.65M × 1.5 KB], [~5.5 GB/year],
  [Peak concurrent games], [given], [2,500 games],
  [Redis memory per game], [FEN + moves + metadata], [~2 KB],
  [Redis peak memory], [2,500 × 2 KB], [~5 MB],
)

*MongoDB*: With ~5.5 GB of new data per year, storage remains far below single-node limits. Sharding typically becomes relevant when data approaches terabytes or when write throughput exceeds tens of thousands of operations per second. At this scale, a single replica set is sufficient. Additionally, aggregation pipelines perform better without the scatter-gather overhead of sharded queries.

*Redis*: With peak memory under 10 MB for live game states, a single Redis instance with replication provides sufficient capacity.

#pagebreak()

= AI Tools Usage

This section documents how Large Language Model (LLM) tools were utilized during the development of Square64, including the purposes, methodologies, and critical evaluation of AI-assisted work.

== Purpose of AI Usage

AI tools, primarily Gemini and Claude, were employed for secondary and supportive tasks during development, while the core architectural decisions and business logic were developed independently by the team.

=== Images

- *User client images mockups*: Generated html pages that visualizes the information retrieved from the implemented endpoints and organized in the relative web pages.

=== Boilerplate Code Generation

- *DTO and model classes*: LLMs helped generate repetitive Java boilerplate such as Data Transfer Objects with getters, setters, and constructors.
- *REST controller stubs*: We used AI to generate basic CRUD endpoint that we then customized with our specific validation logic and error handling.
- *Configuration templates*: Initial Spring configuration classes for MongoDB, Neo4j, and Redis connections were generated as starting points, then adapted to our specific connection parameters and requirements.

=== Brainstorming and Idea Exploration

- *Feature ideation*: When designing the social features, we brainstormed with LLMs about what types of friend suggestions would be meaningful in a chess context (mutual followers, shared clubs, tournament co-participants).
- *Schema design alternatives*: We discussed different approaches to storing game data (for example full embedding vs. referencing) to understand trade-offs before making our own design decisions.
- *Edge case identification*: LLMs helped us think through edge cases we might have missed, such as "what happens if a player queues for matchmaking while already in a game?" or "how should tournament subscriptions behave at boundary times?"

=== Syntax and API Lookup

- *Cypher query syntax*: We used LLMs to quickly look up Neo4j's Cypher syntax for relationship patterns, variable-length paths, and aggregation functions instead of searching through documentation.
- *MongoDB aggregation operators*: When building aggregation pipelines, we asked about specific operators to understand their behavior and correct usage.
- *Spring annotations*: We consulted LLMs about Spring Data annotations like `@Query`, `@Aggregation`, and `@Transactional` to understand their parameters and limitations.

=== Documentation Drafting

- *Section outlines*: LLMs helped create initial outlines for documentation sections, which we then filled in with project-specific content.
- *Proofreading*: We used AI to review draft documentation for clarity and consistency in technical terminology.

== Prompts and Tasks Performed

Below are some examples of prompts used and the tasks they addressed:

=== Boilerplate Generation

*Prompt example 1:*
_"Generate a Java DTO class called GameSummaryDTO with fields: gameId (String), opponent (String), result (String), opening (String)..."_

*Task performed:* The LLM generated the class structure. We adjusted field types and the occasional errors to better match our requirements.

*Prompt example 2:*
_"Create a basic Spring REST controller for a Tournament entity with endpoints for create, read, update, delete operations."_

*Task performed:* The LLM provided a controller skeleton. We replaced the generic CRUD logic with our tournament-specific business rules (rating range validation, subscription windows, participant limits).

=== Syntax Lookup

*Prompt example 3:*
_"What's the Cypher syntax to match all nodes connected to a user through either FOLLOWS, JOINED, or PARTECIPATED relationships within 2 hops?"_

*Task performed:* The LLM explained the syntax `[:FOLLOWS|JOINED|PARTECIPATED*1..2]` for variable-length paths with multiple relationship types. We used this knowledge to write our friend suggestion query.

*Prompt example 4:*
_"How do I use Spring Data MongoDB's "Aggregation" annotation to pass method parameters into the pipeline?"_

*Task performed:* The LLM showed the `?0`, `?1` placeholder syntax for parameter binding. We applied this to our statistics aggregations.
