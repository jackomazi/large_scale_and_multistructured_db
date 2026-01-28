package it.unipi.chessApp.service;

import it.unipi.chessApp.model.ChessOpening;

public interface OpeningService {
    
    /**
     * Find an opening by FEN position.
     * @param fen The FEN string representing the board position
     * @return The matching ChessOpening, or null if not found
     */
    ChessOpening findOpeningByFen(String fen);
}
