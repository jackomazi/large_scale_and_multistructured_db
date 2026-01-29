package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.GameSummary;
import it.unipi.chessApp.utils.Constants;

public class GameSummaryDTO {
    private String id;

    private String white;
    private String black;
    private String opening;
    private String winner;
    private String date;

    public GameSummaryDTO(){
        //Initializates as a placeholder
        this.id = null;
        this.white = "name";
        this.black = "name";
        this.date = Constants.DEFAULT_PLACEHOLDER_DATE;
        this.opening = "name";
        this.winner = "name";
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWhite() {
        return white;
    }

    public void setWhite(String white) {
        this.white = white;
    }

    public String getBlack() {
        return black;
    }

    public void setBlack(String black) {
        this.black = black;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public String getWinner() {
        return winner;
    }

    public void setWinner(String winner) {
        this.winner = winner;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public static GameSummaryDTO summarize(GameDTO game){
        GameSummaryDTO dto = new GameSummaryDTO();
        dto.setId(game.getId());
        dto.setWhite(game.getWhitePlayer());
        dto.setBlack(game.getBlackPlayer());
        dto.setOpening(game.getOpening());
        dto.setDate(game.getEndTime());
        if(game.getResultWhite().equals("win"))
            dto.setWinner(game.getWhitePlayer());
        else
            dto.setWinner(game.getBlackPlayer());
        return dto;
    }

    public static GameSummaryDTO convertToDTO(GameSummary summary){
        GameSummaryDTO dto = new GameSummaryDTO();
        dto.setWinner(summary.getWinner());
        dto.setWhite(summary.getWhite());
        dto.setBlack(summary.getBlack());
        dto.setDate(summary.getDate());
        dto.setId(summary.getId());
        dto.setOpening(summary.getOpening());
        return dto;
    }
}
