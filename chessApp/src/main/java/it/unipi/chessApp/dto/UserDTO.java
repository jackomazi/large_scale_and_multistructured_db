package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.Stats;
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
  private String country;
  private String lastOnline;
  private String joined;
  private boolean isStreamer;
  private boolean verified;
  private List<String> streamingPlatforms;
  private List<GameSummaryDTO> games;
  private Stats stats;
  private String mail;
  private String password;
  private boolean admin;

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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    public List<String> getStreamingPlatforms() {
        return streamingPlatforms;
    }

    public void setStreamingPlatforms(List<String> streamingPlatforms) {
        this.streamingPlatforms = streamingPlatforms;
    }

    public List<GameSummaryDTO> getGames() {
        return games;
    }

    public void setGames(List<GameSummaryDTO> games) {
        this.games = games;
    }

    public Stats getStats() {
        return stats;
    }

    public void setStats(Stats stats) {
        this.stats = stats;
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

    public boolean isAdmin() {
        return admin;
    }

    public void setAdmin(boolean admin) {
        this.admin = admin;
    }

    public static UserDTO convertToDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setUsername(user.getUsername());
        dto.setCountry(user.getCountry());
        dto.setLastOnline(user.getLastOnline());
        dto.setJoined(user.getJoined());
        dto.setStreamer(user.isStreamer());
        dto.setVerified(user.isVerified());
        dto.setStreamingPlatforms(user.getStreamingPlatforms());
        List<GameSummaryDTO> summaries = user.getGames()
                .stream()
                .map(GameSummaryDTO::convertToDTO)
                .toList();
        dto.setGames(summaries);
        dto.setStats(user.getStats());
        dto.setMail(user.getMail());
        dto.setPassword(user.getPassword());
        dto.setAdmin(user.isAdmin());
        return dto;
    }
}
