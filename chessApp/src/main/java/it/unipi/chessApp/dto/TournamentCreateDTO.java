package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentCreateDTO {
    private String name;
    private String description;
    private String finishTime;
    private int minRating;
    private int maxRating;
}
