package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveGameState {
    private String gameId;
    private String whitePlayer;
    private String blackPlayer;
    private String fen;
    private String status;
    private String lastMove;
    private String tournamentId;
    private String gameType;
    private long createdAt;
    private long lastMoveAt;
    
    // Opening detection fields
    private List<String> moveHistory;
    private String detectedOpening;
    private String detectedOpeningEco;

    public static final String STARTING_FEN = "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1";

    public static final String STATUS_IN_PROGRESS = "IN_PROGRESS";
    public static final String STATUS_WHITE_WINS = "WHITE_WINS";
    public static final String STATUS_BLACK_WINS = "BLACK_WINS";
    public static final String STATUS_DRAW = "DRAW";
    public static final String STATUS_STALEMATE = "STALEMATE";

    public static LiveGameState createNewRegularGame(String gameId, String whitePlayer, String blackPlayer, String gameType) {
        LiveGameState state = new LiveGameState();
        state.setGameId(gameId);
        state.setWhitePlayer(whitePlayer);
        state.setBlackPlayer(blackPlayer);
        state.setFen(STARTING_FEN);
        state.setStatus(STATUS_IN_PROGRESS);
        state.setLastMove(null);
        state.setTournamentId(null);
        state.setGameType(gameType);
        state.setCreatedAt(System.currentTimeMillis());
        state.setLastMoveAt(System.currentTimeMillis());
        state.setMoveHistory(new ArrayList<>());
        state.setDetectedOpening(null);
        state.setDetectedOpeningEco(null);
        return state;
    }

    public static LiveGameState createNewTournamentGame(String gameId, String whitePlayer, String blackPlayer, String tournamentId) {
        LiveGameState state = new LiveGameState();
        state.setGameId(gameId);
        state.setWhitePlayer(whitePlayer);
        state.setBlackPlayer(blackPlayer);
        state.setFen(STARTING_FEN);
        state.setStatus(STATUS_IN_PROGRESS);
        state.setLastMove(null);
        state.setTournamentId(tournamentId);
        state.setGameType(null);
        state.setCreatedAt(System.currentTimeMillis());
        state.setLastMoveAt(System.currentTimeMillis());
        state.setMoveHistory(new ArrayList<>());
        state.setDetectedOpening(null);
        state.setDetectedOpeningEco(null);
        return state;
    }

    public boolean isWhiteTurn() {
        String[] parts = fen.split(" ");
        return parts.length > 1 && "w".equals(parts[1]);
    }

    public boolean isTournamentGame() {
        return tournamentId != null && !tournamentId.isEmpty();
    }

    /**
     * Add a move to the move history.
     * @param move The move in SAN notation
     */
    public void addMove(String move) {
        if (moveHistory == null) {
            moveHistory = new ArrayList<>();
        }
        moveHistory.add(move);
    }

    /**
     * Get the total number of moves (half-moves) played.
     * @return The move count
     */
    public int getMoveCount() {
        return moveHistory != null ? moveHistory.size() : 0;
    }

    /**
     * Get moves as a PGN-style string.
     * @return Moves formatted as "1. e4 e5 2. Nf3 Nc6 ..."
     */
    public String getMovesPgn() {
        if (moveHistory == null || moveHistory.isEmpty()) {
            return "";
        }
        
        StringBuilder pgn = new StringBuilder();
        for (int i = 0; i < moveHistory.size(); i++) {
            if (i % 2 == 0) {
                if (i > 0) pgn.append(" ");
                pgn.append((i / 2 + 1)).append(". ");
            } else {
                pgn.append(" ");
            }
            pgn.append(moveHistory.get(i));
        }
        return pgn.toString();
    }
}
