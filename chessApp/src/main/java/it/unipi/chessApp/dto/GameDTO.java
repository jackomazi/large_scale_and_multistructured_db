package it.unipi.chessApp.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameDTO {
    private String id;
    private String whitePlayer;
    private String blackPlayer;
    private int whiteRating;
    private int blackRating;
    private String resultWhite;
    private String resultBlack;
    private String opening;
    private String moves;
    private String timeClass;
    private boolean rated;
    private String endTime;
    private boolean historical;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getWhitePlayer() {
        return whitePlayer;
    }

    public void setWhitePlayer(String whitePlayer) {
        this.whitePlayer = whitePlayer;
    }

    public String getBlackPlayer() {
        return blackPlayer;
    }

    public void setBlackPlayer(String blackPlayer) {
        this.blackPlayer = blackPlayer;
    }

    public int getWhiteRating() {
        return whiteRating;
    }

    public void setWhiteRating(int whiteRating) {
        this.whiteRating = whiteRating;
    }

    public int getBlackRating() {
        return blackRating;
    }

    public void setBlackRating(int blackRating) {
        this.blackRating = blackRating;
    }

    public String getResultWhite() {
        return resultWhite;
    }

    public void setResultWhite(String resultWhite) {
        this.resultWhite = resultWhite;
    }

    public String getResultBlack() {
        return resultBlack;
    }

    public void setResultBlack(String resultBlack) {
        this.resultBlack = resultBlack;
    }

    public String getOpening() {
        return opening;
    }

    public void setOpening(String opening) {
        this.opening = opening;
    }

    public String getMoves() {
        return moves;
    }

    public void setMoves(String moves) {
        this.moves = moves;
    }

    public String getTimeClass() {
        return timeClass;
    }

    public void setTimeClass(String timeClass) {
        this.timeClass = timeClass;
    }

    public boolean isRated() {
        return rated;
    }

    public void setRated(boolean rated) {
        this.rated = rated;
    }

    public String  getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public boolean isHistorical() {
        return historical;
    }

    public void setHistorical(boolean historical) {
        this.historical = historical;
    }
}
