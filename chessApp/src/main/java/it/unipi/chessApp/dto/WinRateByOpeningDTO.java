package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WinRateByOpeningDTO {
    private String opening;
    private int totalGames;
    private double winPercentage;
}
