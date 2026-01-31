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
        // Filter out placeholder games (where _id is null or missing) before slicing
        "{ '$project': { 'username': 1, 'realGames': { '$filter': { 'input': '$games', 'as': 'game', 'cond': { '$and': [ { '$ne': ['$$game._id', null] }, { '$gt': ['$$game._id', null] } ] } } } } }",
        // Get the last 3 real games
        "{ '$project': { 'username': 1, 'recentGames': { '$slice': ['$realGames', -3] } } }",
        // Only consider users with at least 3 real games
        "{ '$match': { '$expr': { '$gte': [{ '$size': '$recentGames' }, 3] } } }",
        // Check if user lost each of the 3 games
        "{ '$project': { 'username': 1, 'recentGames': 1, 'winResults': { '$map': { 'input': '$recentGames', 'as': 'game', 'in': { '$eq': ['$$game.winner', '$username'] } } } } }",
        // Match users who lost all 3 games (no wins in the array)
        "{ '$match': { 'winResults': { '$not': { '$elemMatch': { '$eq': true } } } } }",
        "{ '$project': { '_id': 0, 'username': 1, 'status': { '$literal': 'ON TILT' } } }"
    })
    List<TiltPlayerDTO> findTiltPlayers();

    @Aggregation(pipeline = {
        "{ '$match': { '_id': ?0 } }",
        "{ '$unwind': '$games' }",
        "{ '$match': { 'games._id': { '$ne': null }, 'games.winner': { '$ne': null } } }",
        "{ '$group': { '_id': '$_id', 'username': { '$first': '$username' }, 'totalGames': { '$sum': 1 }, 'wins': { '$sum': { '$cond': [{ '$eq': [{ '$toLower': '$games.winner' }, { '$toLower': '$username' }] }, 1, 0] } } } }",
        "{ '$project': { 'winRate': { '$multiply': [{ '$divide': ['$wins', '$totalGames'] }, 100] } } }"
    })
    UserWinRateDTO calcUserWinRate(String userId);

    @Aggregation(pipeline = {
            "{ '$match': { '_id': ?0 } }",
            "{ '$unwind': '$games' }",
            "{ '$match': { 'games._id': { '$exists': true, '$ne': null }, 'games.winner': { '$ne': 'name' } } }",
            "{ '$group': { '_id': '$games.opening', 'count': { '$sum': 1 } } }",
            "{ '$sort': { 'count': -1 } }",
            "{ '$limit': 1 }",
            "{ '$project': { 'opening': '$_id', 'count': 1, '_id': 0 } }"
    })
    UserFavoriteOpeningDTO calcFavoriteOpening(String userId);
}
