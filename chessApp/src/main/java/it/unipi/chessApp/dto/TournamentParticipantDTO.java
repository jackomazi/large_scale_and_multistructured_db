package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TournamentParticipantDTO {
    private String name;
    private int wins;
    private int draws;
    private int losses;
    private int placement;
    private String tournamentName;
    private String tournamentId;

    public String getTournamentName() {
        return tournamentName;
    }

    public void setTournamentName(String tournamentName) {
        this.tournamentName = tournamentName;
    }

    public String getTournamentId() {
        return tournamentId;
    }

    public void setTournamentId(String tournamentId) {
        this.tournamentId = tournamentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getWins() {
        return wins;
    }

    public void setWins(int wins) {
        this.wins = wins;
    }

    public int getDraws() {
        return draws;
    }

    public void setDraws(int draws) {
        this.draws = draws;
    }

    public int getLosses() {
        return losses;
    }

    public void setLosses(int losses) {
        this.losses = losses;
    }

    public int getPlacement() {
        return placement;
    }

    public void setPlacement(int placement) {
        this.placement = placement;
    }

    public static TournamentParticipantDTO convertToDTO(TournamentParticipant model){
        TournamentParticipantDTO dto = new TournamentParticipantDTO();
        dto.setPlacement(model.getPlacement());
        dto.setName(model.getName());
        dto.setLosses(model.getLosses());
        dto.setWins(model.getWins());
        dto.setDraws(model.getDraws());
        dto.setTournamentId(model.getTournament().getId());
        dto.setTournamentName(model.getTournament().getName());
        return dto;
    }
}
