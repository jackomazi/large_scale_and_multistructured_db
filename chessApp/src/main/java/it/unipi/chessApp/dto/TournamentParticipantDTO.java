package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Neo4jTournamentParticipationDTO {
    private int wins;
    private int draws;
    private int losses;
    private int placement;
}
