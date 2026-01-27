package it.unipi.chessApp.repository;

import it.unipi.chessApp.dto.TiltPlayerDTO;
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
}
