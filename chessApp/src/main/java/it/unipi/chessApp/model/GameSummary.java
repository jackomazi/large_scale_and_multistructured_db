package it.unipi.chessApp.model;

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
}
