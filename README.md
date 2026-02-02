# Chess Data Analytics Platform

A large-scale, multi-structured database project for chess game analysis, developed as part of the Large Scale and Multistructured Databases course at University of Pisa.

## Overview

This project implements a comprehensive chess data analytics platform that collects, stores, and analyzes chess game data from multiple sources (Chess.com, Lichess, and Kaggle). The system uses a polyglot persistence architecture combining **MongoDB** for document storage and **Neo4j** for graph-based relationships, with a **Spring Boot** REST API for data access and analysis.

## Architecture

### Data Storage Layer
- **MongoDB**: Stores user profiles, game details, tournaments, and clubs as JSON documents
- **Neo4j**: Models relationships between users, clubs, and tournaments as a graph database
- **Hybrid Approach**: Each Neo4j node contains a `mongo_id` reference to link graph relationships with detailed document data

### Data Collection Layer (Python)
- **API Integrations**: Chess.com, Lichess, and Kaggle chess data APIs
- **Collectors**: Automated scripts for gathering chess data from various sources
- **Data Processing**: Opening detection, game parsing, and relationship generation

### Application Layer (Java/Spring Boot)
- **REST API**: Exposes endpoints for querying users, games, clubs, and tournaments
- **Multi-Database Support**: Seamlessly integrates MongoDB and Neo4j data
- **OpenAPI Documentation**: Auto-generated Swagger UI for API exploration

## Project Structure

```
├── chessApp/              # Spring Boot application
│   ├── src/main/java/
│   │   └── it/unipi/chessApp/
│   │       ├── controller/    # REST API controllers
│   │       ├── service/       # Business logic layer
│   │       ├── model/         # Data models
│   │       └── repository/    # Database access layer
│   └── pom.xml
│
├── data/                  # Python data collection and processing
│   ├── api/              # API wrappers (Chess.com, Lichess, Kaggle)
│   ├── collectors/       # Data collection scripts
│   ├── storage/          # Database interface modules
│   ├── dumps/            # Collected player data
│   ├── kaggle_chess_data/ # Historical chess games dataset
│   ├── config.json       # Collection parameters
│   ├── requirements.txt  # Python dependencies
│   └── start.sh          # Data collection pipeline script
│
├── myenv/                # Python virtual environment
├── docs.typ              # Project documentation
└── README.md
```

## Database Schema

### MongoDB Collections

#### Users
```json
{
  "username": "player123",
  "country": "IT",
  "last_online": "2026-01-26 16:49:07",
  "joined": "2012-09-28 15:44:42",
  "is_streamer": false,
  "verified": false,
  "games": [
    {
      "white": "player123",
      "black": "opponent456",
      "opening": "Sicilian-Defense-Najdorf-Variation",
      "winner": "player123",
      "date": "2024-07-01 11:41:24"
    }
  ]
}
```

#### Clubs, Tournaments
See [data/mongodb.md](data/mongodb.md) for complete schema details.

### Neo4j Graph Model

**Nodes**: `USER`, `CLUB`, `TOURNAMENT`

**Relationships**:
- `(:USER)-[:JOINED]->(:CLUB)` - Club membership with ratings
- `(:USER)-[:PARTECIPATED]->(:TOURNAMENT)` - Tournament participation with results
- `(:USER)-[:FOLLOWS]->(:USER)` - Social connections

See [data/neo4j.md](data/neo4j.md) for complete graph schema.

## Setup and Installation

### Prerequisites
- Python 3.8+
- Java 17+
- Maven 3.6+
- MongoDB 4.4+
- Neo4j 5.0+

### Python Environment Setup

1. **Create and activate virtual environment**:
   ```bash
   python -m venv myenv
   # Windows
   myenv\Scripts\activate
   # Linux/Mac
   source myenv/bin/activate
   ```

2. **Install Python dependencies**:
   ```bash
   cd data
   pip install -r requirements.txt
   ```

3. **Configure database connections**:
   Create a `.env` file in the `data/` directory:
   ```env
   MONGO_URI=mongodb://localhost:27017/
   NEO4J_URI=bolt://localhost:7687
   NEO4J_USER=neo4j
   NEO4J_PASSWORD=your_password
   ```

### Data Collection

1. **Configure collection parameters** in [data/config.json](data/config.json):
   - Countries to scrape: `countries`
   - Chess.com clubs: `clubs`
   - Limits: `max_scrap_users_per_club`, `max_scrap_games_per_archive`, etc.

2. **Run data collection pipeline**:
   ```bash
   cd data
   bash start.sh
   ```
   This executes:
   - Historical games from Kaggle
   - Chess.com data collection by club
   - Lichess data collection

3. **Individual collectors** can also be run separately:
   ```bash
   python -m collectors.by_club_chess_com
   python -m collectors.load_lichess
   python -m collectors.historical_games
   ```

### Spring Boot Application

1. **Configure application properties** in `chessApp/src/main/resources/application.properties`:
   ```properties
   spring.data.mongodb.uri=mongodb://localhost:27017/chess_db
   spring.neo4j.uri=bolt://localhost:7687
   spring.neo4j.authentication.username=neo4j
   spring.neo4j.authentication.password=your_password
   ```

2. **Build and run the application**:
   ```bash
   cd chessApp
   ./mvnw spring-boot:run
   # Or with Maven wrapper on Windows
   mvnw.cmd spring-boot:run
   ```

3. **Access the API**:
   - REST API: http://localhost:8080
   - Swagger UI: http://localhost:8080/swagger-ui.html

## API Endpoints

The Spring Boot application exposes REST endpoints for:

- **Users**: `/api/users` - User profiles, games, statistics
- **Clubs**: `/api/clubs` - Club information and members
- **Tournaments**: `/api/tournaments` - Tournament data and results
- **Games**: `/api/games` - Game search and analysis

See Swagger UI documentation for complete API reference.

## Data Sources

### Chess.com
- Club member lists and profiles
- Player game archives
- Tournament data
- Countries covered: FR, IT, US, IN, DE

### Lichess
- Team member data (25+ top teams)
- Player profiles and games
- Community data

### Kaggle
- Historical chess games dataset
- PGN files for game analysis

## Key Features

- **Multi-database architecture** with MongoDB and Neo4j
- **Automated data collection** from multiple chess platforms
- **Chess opening detection** using ECO (Encyclopedia of Chess Openings) codes
- **Graph-based relationship analysis** (friendships, club memberships, tournament participation)
- **RESTful API** with comprehensive search and filtering
- **Scalable design** for handling large datasets (50+ games per user, 60+ users per club)

## Technologies Used

### Backend
- Spring Boot 3.5.7
- Spring Data MongoDB
- Spring Data Neo4j
- SpringDoc OpenAPI (Swagger)
- Lombok
- Maven

### Python Stack
- chess (python-chess library)
- pymongo
- neo4j
- requests
- python-dotenv
- tqdm (progress bars)
- Faker (test data generation)

## Data Collection Statistics

Based on [data/config.json](data/config.json) configuration:
- **Countries**: 5 (FR, IT, US, IN, DE)
- **Chess.com Clubs**: 60+
- **Lichess Teams**: 25+
- **Games per User**: Up to 50
- **Users per Club**: Up to 60

## Development

### Project Structure
- `chessApp/` - Java Spring Boot application
- `data/api/` - API wrapper classes for external services
- `data/collectors/` - Data collection scripts
- `data/storage/` - Database interface modules
- `data/dumps/` - Raw collected data

### Running Tests
```bash
cd chessApp
./mvnw test
```

## Contributing

This is an academic project for the Large Scale and Multistructured Databases course at University of Pisa.

## License

Academic project - University of Pisa

## Authors

Students of Large Scale and Multistructured Databases @ UNIPI

---

For detailed database schemas, see:
- MongoDB: [data/mongodb.md](data/mongodb.md)
- Neo4j: [data/neo4j.md](data/neo4j.md)
