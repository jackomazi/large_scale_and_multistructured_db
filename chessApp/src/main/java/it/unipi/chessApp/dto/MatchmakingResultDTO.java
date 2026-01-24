package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingResultDTO {
    private String gameId;
    private String whitePlayer;
    private String blackPlayer;
    private String tournamentId;
    private boolean matched;
    private String message;
}
