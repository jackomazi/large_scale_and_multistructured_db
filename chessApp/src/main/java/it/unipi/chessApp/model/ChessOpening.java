package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.io.Serializable;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChessOpening implements Serializable {
    private static final long serialVersionUID = 1L;

    private String eco;    // ECO code (e.g., "A00", "B20")
    private String name;   // Opening name (e.g., "Sicilian Defense")
    private String moves;  // Move sequence (e.g., "1. e4 c5")
    private String fen;    // FEN position after the moves
}
