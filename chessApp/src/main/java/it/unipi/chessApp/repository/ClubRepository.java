package it.unipi.chessApp.repository;

import it.unipi.chessApp.model.Club;
import it.unipi.chessApp.service.exception.BusinessException;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ClubRepository extends MongoRepository<Club, String> {
    Optional<Club> findByName(String name);
}
