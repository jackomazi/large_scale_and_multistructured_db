package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

// Non serve @RelationshipProperties o @Node qui, è un puro contenitore dati
@Data
@AllArgsConstructor
@NoArgsConstructor
public class TournamentPlayerResultDTO {
    private String participantId;
    private String name;
    private int wins;
    private int losses;
    private int draws;
    private int placement;
}