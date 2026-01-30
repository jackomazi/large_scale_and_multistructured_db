package it.unipi.chessApp.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;

@Data
@NoArgsConstructor
public class ClubMember {

    @Id
    @GeneratedValue
    private Long id;

    private String name;

    private String country;

    @Property("bullet")
    private int bulletRating;

    @Property("blitz")
    private int blitzRating;

    @Property("rapid")
    private int rapidRating;

}
