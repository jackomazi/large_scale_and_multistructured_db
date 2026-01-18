package it.unipi.chessApp.repository;

import it.unipi.chessApp.model.Game;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface GameRepository extends MongoRepository<Game, String> {

    @Query("{'$or': [{'white_player': ?0}, {'black_player': ?0}]}")
    List<Game> findByPlayer(String username);
}
