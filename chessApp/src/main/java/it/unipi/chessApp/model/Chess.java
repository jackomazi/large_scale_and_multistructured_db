package it.unipi.chessApp.model;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
//import jakarta.persistence.*;
import java.time.LocalDate;

@Data
@Document(collection = "games")
public class Chess {
    @Id
    private String id;  // corrisponde a _id

    private String url;
    private String black_player;
    private int black_rating;
    private String eco_url;
    private long end_time;
    private String opening;
    private String pgn;
    private boolean rated;
    private String result_black;
    private String result_white;
    private String time_class;
    private String white_player;
    private int white_rating;
    private String moves;
}
