package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

/**
 * Model class for tournament metadata stored in Redis as JSON string.
 * Used for quick validation during subscription and matchmaking operations.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LiveTournamentData {
    private String status;
    private int minRating;
    private int maxRating;
    private int maxParticipants;
    private String finishTime;

    /**
     * Create a LiveTournamentData from tournament entity fields.
     */
    public static LiveTournamentData fromTournament(String status, int minRating, int maxRating, 
                                                     int maxParticipants, String finishTime) {
        return new LiveTournamentData(status, minRating, maxRating, maxParticipants, finishTime);
    }
}
