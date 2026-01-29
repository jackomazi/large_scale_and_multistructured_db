package it.unipi.chessApp.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "clubs")
public class Club {
    @Id
    private String id;
    private String name;
    private String description;
    private String country;
    @Field("creation_date")
    private String creationDate;
    private String admin;
}
