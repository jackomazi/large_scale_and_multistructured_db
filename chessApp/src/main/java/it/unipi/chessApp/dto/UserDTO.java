package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.Stats;
import it.unipi.chessApp.model.TournamentStats;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {

  private String id;
  private String name;
  private String username;
  private int followers;
  private String country;
  private String lastOnline;
  private String joined;
  private boolean isStreamer;
  private List<String> streamingPlatforms;
  private String club;
  private List<GameSummary> games;
  private Stats stats;
  private TournamentStats tournaments;
  private String mail;
  private String password;
}
