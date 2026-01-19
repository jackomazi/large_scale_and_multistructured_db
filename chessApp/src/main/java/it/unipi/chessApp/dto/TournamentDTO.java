package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.TournamentPlayer;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentDTO {
    private String id;
    private String name;
    private String description;
    private String creator;
    private String status;
    private String finishTime;
    private int minRating;
    private int maxRating;
    private int participants;
    private int maxParticipants;
    private String timeControl;
    private List<TournamentPlayer> players;
    private List<GameSummary> games;
}
