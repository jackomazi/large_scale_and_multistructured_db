package it.unipi.chessApp.model;

import it.unipi.chessApp.dto.GameSummaryDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class GameSummary {

  @Field("_id")
  private String id;

  private String white;
  private String black;
  private String opening;
  private String winner;
  private String date;

  public static GameSummary convertToEntity(GameSummaryDTO dto){
      GameSummary summary = new GameSummary();
      summary.setWinner(dto.getWinner());
      summary.setWhite(dto.getWhite());
      summary.setBlack(dto.getBlack());
      summary.setDate(dto.getDate());
      summary.setId(dto.getId());
      summary.setOpening(dto.getOpening());
      return summary;
  }
}
