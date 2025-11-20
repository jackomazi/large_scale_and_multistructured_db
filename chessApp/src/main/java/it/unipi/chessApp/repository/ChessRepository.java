package it.unipi.chessApp.repository;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import it.unipi.chessApp.model.Chess;
import java.util.List;


@Repository
public interface ChessRepository extends MongoRepository<Chess,String> {
    List<Chess> findByWhitePlayerOrBlackPlayer(String whitePlayer, String blackPlayer);

}
