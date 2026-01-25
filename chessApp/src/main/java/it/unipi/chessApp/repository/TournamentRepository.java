package it.unipi.chessApp.repository;

import it.unipi.chessApp.model.Tournament;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentRepository extends MongoRepository<Tournament, String> {
    
    @Query("{ 'status': 'active', 'finish_time': { $lte: ?0 } }")
    List<Tournament> findActiveTournamentsToFinish(String currentTime);
}
