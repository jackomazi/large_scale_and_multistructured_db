package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.model.Role;
import it.unipi.chessApp.model.Stats;
import it.unipi.chessApp.model.TournamentStats;
import java.util.List;

import it.unipi.chessApp.model.User;
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
  private Role role;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public int getFollowers() {
        return followers;
    }

    public void setFollowers(int followers) {
        this.followers = followers;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getLastOnline() {
        return lastOnline;
    }

    public void setLastOnline(String lastOnline) {
        this.lastOnline = lastOnline;
    }

    public String getJoined() {
        return joined;
    }

    public void setJoined(String joined) {
        this.joined = joined;
    }

    public boolean isStreamer() {
        return isStreamer;
    }

    public void setStreamer(boolean streamer) {
        isStreamer = streamer;
    }

    public List<String> getStreamingPlatforms() {
        return streamingPlatforms;
    }

    public void setStreamingPlatforms(List<String> streamingPlatforms) {
        this.streamingPlatforms = streamingPlatforms;
    }

    public String getClub() {
        return club;
    }

    public void setClub(String club) {
        this.club = club;
    }

    public List<GameSummary> getGames() {
        return games;
    }

    public void setGames(List<GameSummary> games) {
        this.games = games;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
    }

    public TournamentStats getTournaments() {
        return tournaments;
    }

    public void setTournaments(TournamentStats tournaments) {
        this.tournaments = tournaments;
    }

    public String getMail() {
        return mail;
    }

    public void setMail(String mail) {
        this.mail = mail;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public static  UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setFollowers(user.getFollowers());
        dto.setCountry(user.getCountry());
        dto.setLastOnline(user.getLastOnline());
        dto.setJoined(user.getJoined());
        dto.setStreamer(user.isStreamer());
        dto.setStreamingPlatforms(user.getStreamingPlatforms());
        dto.setClub(user.getClub());
        dto.setGames(user.getGames());
        dto.setStats(user.getStats());
        dto.setTournaments(user.getTournaments());
        dto.setMail(user.getMail());
        dto.setPassword(user.getPassword());
        dto.setRole(user.getRole());
        return dto;
    }
}
