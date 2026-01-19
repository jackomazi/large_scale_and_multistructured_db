package it.unipi.chessApp.service.impl;

import it.unipi.chessApp.service.Neo4jService;
import lombok.RequiredArgsConstructor;
import org.neo4j.driver.Driver;
import org.neo4j.driver.Session;
import org.springframework.stereotype.Service;

import static org.neo4j.driver.Values.parameters;

@Service
@RequiredArgsConstructor
public class Neo4jServiceImpl implements Neo4jService {

    private final Driver driver;

    @Override
    public void createUser(String mongoId, String username) {
        try (Session session = driver.session()) {
            session.run("CREATE (usr:USER { name: $name, mongo_id: $mongo_id })",
                    parameters("name", username, "mongo_id", mongoId));
        }
    }

    @Override
    public void createClub(String mongoId, String clubName) {
        try (Session session = driver.session()) {
            session.run("CREATE (clb:CLUB { name: $name, mongo_id: $mongo_id })",
                    parameters("name", clubName, "mongo_id", mongoId));
        }
    }

    @Override
    public void createTournament(String mongoId, String tournamentName) {
        try (Session session = driver.session()) {
            session.run("CREATE (trn:TOURNAMENT { name: $name, mongo_id: $mongo_id })",
                    parameters("name", tournamentName, "mongo_id", mongoId));
        }
    }

    @Override
    public void joinClub(String userMongoId, String clubMongoId, String country, int bulletRating, int blitzRating, int rapidRating) {
        try (Session session = driver.session()) {
            session.run("MATCH (usr:USER), (clb:CLUB) " +
                            "WHERE usr.mongo_id = $user_id AND clb.mongo_id = $club_id " +
                            "CREATE (usr)-[jnd:JOINED {country: $country, butter: $bullet, blitz: $blitz, rapid: $rapid}]->(clb)",
                    parameters("user_id", userMongoId, "club_id", clubMongoId,
                            "country", country, "bullet", bulletRating, "blitz", blitzRating, "rapid", rapidRating));
        }
    }

    @Override
    public void participateTournament(String userMongoId, String tournamentMongoId, int wins, int draws, int losses, int placement) {
        try (Session session = driver.session()) {
            session.run("MATCH (usr:USER), (trn:TOURNAMENT) " +
                            "WHERE usr.mongo_id = $user_id AND trn.mongo_id = $tournament_id " +
                            "CREATE (usr)-[part:PARTECIPATED {wins: $wins, draws: $draws, losses: $losses, placement: $placement}]->(trn)",
                    parameters("user_id", userMongoId, "tournament_id", tournamentMongoId,
                            "wins", wins, "draws", draws, "losses", losses, "placement", placement));
        }
    }

    @Override
    public void followUser(String followerMongoId, String followedMongoId) {
        try (Session session = driver.session()) {
            session.run("MATCH (usr1:USER), (usr2:USER) " +
                            "WHERE usr1.mongo_id = $follower_id AND usr2.mongo_id = $followed_id " +
                            "CREATE (usr1)-[:FOLLOWS]->(usr2)",
                    parameters("follower_id", followerMongoId, "followed_id", followedMongoId));
        }
    }
}
