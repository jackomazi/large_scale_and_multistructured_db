package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    private String id;
    private String whitePlayer;
    private String blackPlayer;
    private int whiteRating;
    private int blackRating;
    private String resultWhite;
    private String resultBlack;
    private String opening;
    private String moves;
    private String timeClass;
    private long endTime;
}
