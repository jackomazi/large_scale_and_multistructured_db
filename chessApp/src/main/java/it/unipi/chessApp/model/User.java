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
@Document(collection = "users")
public class User {
    @Id
    private String id;
    private String name;
    private String username;
    private int followers;
    private String country;
    @Field("last_online")
    private String lastOnline;
    private String joined;
    @Field("is_streamer")
    private boolean isStreamer;
    @Field("streaming_platforms")
    private List<String> streamingPlatforms;
    private String club;
    private List<GameSummary> games;
    private Stats stats;
    private TournamentStats tournaments;
    private String mail;
    private String password;
}
