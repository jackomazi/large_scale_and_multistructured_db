package it.unipi.chessApp.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Member {

  @Field("_id")
  private String id;

  private String name;
  private Stats stats;
  private String country;
}
