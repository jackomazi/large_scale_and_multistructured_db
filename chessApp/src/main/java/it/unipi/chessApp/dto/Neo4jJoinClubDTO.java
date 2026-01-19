package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Neo4jJoinClubDTO {
    private String country;
    private int bulletRating;
    private int blitzRating;
    private int rapidRating;
}
