package it.unipi.chessApp.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.GeneratedValue;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;

@RelationshipProperties
@Data
@NoArgsConstructor
public class FriendRecommendation {

    @Id
    @GeneratedValue
    private String id;

    private String mongoID;

    private String name;

    private String connectionType;

}
