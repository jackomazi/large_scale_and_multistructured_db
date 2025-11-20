package it.unipi.chessApp.service;
import it.unipi.chessApp.model.Chess;
import it.unipi.chessApp.repository.ChessRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.http.HttpStatus;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChessService {
    private final ChessRepository chessRepository;

    // Ricerca una partita per ID
    public Chess getGameById(String id) {
        return chessRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Game not found"));
    }

    // Ricerca partite giocate da un utente (come bianco o nero)
    public List<Chess> getGamesByUsername(String username) {
        return chessRepository.findByWhitePlayerOrBlackPlayer(username, username);
    }


}
