package it.unipi.chessApp.repository;

import it.unipi.chessApp.dto.TiltPlayerDTO;
import it.unipi.chessApp.dto.UserFavoriteOpeningDTO;
import it.unipi.chessApp.dto.UserWinRateDTO;
import it.unipi.chessApp.model.User;
import org.springframework.data.mongodb.repository.Aggregation;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserRepository extends MongoRepository<User, String> {

    Optional<User> findByUsername(String name);
    boolean existsByUsername(String username);

    @Aggregation(pipeline = {
        "{ '$project': { 'username': 1, 'recentGames': { '$slice': ['$games', -3] } } }",
        "{ '$project': { 'username': 1, 'recentGames': 1, 'winResults': { '$map': { 'input': '$recentGames', 'as': 'game', 'in': { '$eq': ['$$game.winner', '$username'] } } } } }",
        "{ '$match': { 'winResults': { '$ne': true }, '$expr': { '$eq': [{ '$size': '$winResults' }, 3] } } }",
        "{ '$project': { '_id': 0, 'username': 1, 'status': { '$literal': 'ON TILT' } } }"
    })
    List<TiltPlayerDTO> findTiltPlayers();

    @Aggregation(pipeline = {
            // Stadio 1: Match
            "{ '$match': { '_id': ?0 } }",

            // Stadio 2: Unwind
            "{ '$unwind': '$games' }",

            // Stadio 3: Match (Filter)
            "{ '$match': { 'games.winner': { '$ne': 'name' } } }",

            // Stadio 4: Group
            "{ '$group': { " +
                    "'_id': '$_id', " +
                    "'totalGames': { '$sum': 1 }, " +
                    "'wins': { " +
                    "'$sum': { " +
                    "'$cond': [ " +
                    "{ '$eq': [{ '$toLower': '$games.winner' }, { '$toLower': '$username' }] }, " +
                    "1, " +
                    "0 " +
                    "] " +
                    "} " +
                    "} " +
                    "} }",

            // Stadio 5: Project
            "{ '$project': { " +
                    "'winRate': { '$multiply': [ { '$divide': ['$wins', '$totalGames'] }, 100 ] } " +
                    "} }"
    })
    UserWinRateDTO calcUserWinRate(String userId);

    @Aggregation(pipeline = {
            "{ '$match': { '_id': ?0 } }",
            "{ '$unwind': '$games' }",
            "{ '$match': { 'games.opening': { '$ne': 'name' } } }",
            "{ '$group': { '_id': '$games.opening', 'count': { '$sum': 1 } } }",
            "{ '$sort': { 'count': -1 } }",
            "{ '$limit': 1 }",
            "{ '$project': { 'opening': '$_id', 'count': 1, '_id': 0 } }"
    })
    UserFavoriteOpeningDTO calcFavoriteOpening(String userId);
}
