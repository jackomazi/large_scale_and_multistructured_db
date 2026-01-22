package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyOpeningStatDTO {
    private int year;
    private int month;
    private String mostUsedOpening;
    private int usageCount;
}
