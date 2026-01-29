package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MatchmakingRequestDTO {
    private String gameType;
}
