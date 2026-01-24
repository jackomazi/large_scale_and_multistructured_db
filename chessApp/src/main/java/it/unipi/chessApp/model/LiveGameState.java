package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveGameState implements Serializable {
    private static final long serialVersionUID = 1L;

    private String gameId;
    private String whitePlayer;
    private String blackPlayer;
    private String fen;
    private String status;
    private String lastMove;
    private String tournamentId;
    private long createdAt;
    private long lastMoveAt;

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_WHITE_WINS = "WHITE_WINS";
    public static final String STATUS_BLACK_WINS = "BLACK_WINS";
    public static final String STATUS_DRAW = "DRAW";
    public static final String STATUS_STALEMATE = "STALEMATE";

    public static LiveGameState createNew(String gameId, String whitePlayer, String blackPlayer, String tournamentId) {
        LiveGameState state = new LiveGameState();
        state.setGameId(gameId);
        state.setWhitePlayer(whitePlayer);
        state.setBlackPlayer(blackPlayer);
        state.setFen(STARTING_FEN);
        state.setStatus(STATUS_IN_PROGRESS);
        state.setLastMove(null);
        state.setTournamentId(tournamentId);
        state.setCreatedAt(System.currentTimeMillis());
        state.setLastMoveAt(System.currentTimeMillis());
        return state;
    }

    public boolean isWhiteTurn() {
        String[] parts = fen.split(" ");
        return parts.length > 1 && "w".equals(parts[1]);
    }

    public boolean isTournamentGame() {
        return tournamentId != null && !tournamentId.isEmpty();
    }
}
