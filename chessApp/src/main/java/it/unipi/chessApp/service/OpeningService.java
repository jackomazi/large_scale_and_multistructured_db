package it.unipi.chessApp.service;

import it.unipi.chessApp.model.ChessOpening;

public interface OpeningService {
    
    /**
     * Load all chess openings from JSON files into Redis.
     * Called at application startup.
     */
    void loadOpeningsToRedis();
    
    /**
     * Find an opening by FEN position.
     * @param fen The FEN string representing the board position
     * @return The matching ChessOpening, or null if not found
     */
    ChessOpening findOpeningByFen(String fen);
    
    /**
     * Check if openings have been loaded into Redis.
     * @return true if openings are loaded
     */
    boolean isOpeningsLoaded();
    
    /**
     * Get the total number of openings loaded.
     * @return count of openings in Redis
     */
    long getOpeningsCount();
}
