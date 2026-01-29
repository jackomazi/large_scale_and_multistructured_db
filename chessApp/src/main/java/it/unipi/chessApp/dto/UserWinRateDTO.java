package it.unipi.chessApp.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserWinRateDTO {

    @Id
    private String userId;
    private double winRate;

}
