package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MoveResultDTO {
    private boolean success;
    private String outcome;
    private String fen;
    private String nextTurn;
    private String gameStatus;
    private String errorMessage;
    private String detectedOpening;

    public static final String OUTCOME_MOVE_MADE = "MOVE_MADE";
    public static final String OUTCOME_CHECK = "CHECK";
    public static final String OUTCOME_CHECKMATE = "CHECKMATE";
    public static final String OUTCOME_STALEMATE = "STALEMATE";
    public static final String OUTCOME_DRAW = "DRAW";

    public static MoveResultDTO error(String errorMessage) {
        return new MoveResultDTO(false, null, null, null, null, errorMessage, null);
    }

    public static MoveResultDTO success(String outcome, String fen, String nextTurn, String gameStatus) {
        return new MoveResultDTO(true, outcome, fen, nextTurn, gameStatus, null, null);
    }

    public static MoveResultDTO success(String outcome, String fen, String nextTurn, String gameStatus, String detectedOpening) {
        return new MoveResultDTO(true, outcome, fen, nextTurn, gameStatus, null, detectedOpening);
    }
}
