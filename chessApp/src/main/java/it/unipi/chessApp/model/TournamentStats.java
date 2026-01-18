package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentStats {
    private int wins;
    private int losses;
    private int draws;
    @Field("best_placement")
    private int bestPlacement;
}
