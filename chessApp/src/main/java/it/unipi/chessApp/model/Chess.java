package it.unipi.chessApp.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
//import jakarta.persistence.*;
import org.springframework.data.mongodb.core.mapping.Field;


@Data
@Document(collection = "games")
public class Chess {
    @Id
    private String id;  // corrisponde a _id

    private String url;

    @Field("black_player")
    private String blackPlayer;

    @Field("black_rating")
    private int blackRating;
    @Field("eco_url")
    private String ecoUrl;
    @Field("end_time")
    private long endTime;
    private String opening;
    private String pgn;
    private boolean rated;
    @Field("result_black")
    private String resultBlack;
    @Field("result_white")
    private String resultWhite;
    @Field("time_class")
    private String timeClass;
    @Field("white_player")
    private String whitePlayer;
    @Field("white_rating")
    private int whiteRating;
    private String moves;
}
