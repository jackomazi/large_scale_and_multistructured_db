package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "tournaments")
public class Tournament {
    @Id
    private String id;
    private String name;
    private String description;
    private String creator;
    private String status;
    @Field("finish_time")
    private String finishTime;
    @Field("min_rating")
    private int minRating;
    @Field("max_rating")
    private int maxRating;
    @Field("max_partecipants")
    private int maxParticipants;
    @Field("time_control")
    private String timeControl;
    private List<GameSummary> games;
    @Field("buffered_games")
    private int bufferedGames;
}
