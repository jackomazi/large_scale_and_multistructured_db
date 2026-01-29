package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "games")
public class Game {
    @Id
    private String id;

    @Field("white_player")
    private String whitePlayer;

    @Field("black_player")
    private String blackPlayer;

    @Field("white_rating")
    private int whiteRating;

    @Field("black_rating")
    private int blackRating;

    @Field("result_white")
    private String resultWhite;

    @Field("result_black")
    private String resultBlack;

    private String opening;

    private String moves;

    @Field("time_class")
    private String timeClass;

    private boolean rated;

    @Field("end_time")
    private String endTime;
}
