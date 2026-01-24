package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameStatusDTO {
    private String gameId;
    private String fen;
    private String currentTurn;
    private String whitePlayer;
    private String blackPlayer;
    private String gameStatus;
    private String lastMove;
    private String tournamentId;
    private long lastMoveAt;
}
