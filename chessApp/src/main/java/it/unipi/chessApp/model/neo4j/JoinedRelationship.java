package it.unipi.chessApp.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;
import org.springframework.data.neo4j.core.schema.Property;

@RelationshipProperties
@Data
@NoArgsConstructor
public class JoinedRelationship {
    @RelationshipId
    private Long id;

    private String country;

    @Property("butter")
    private int bulletRating;

    @Property("blitz")
    private int blitzRating;

    @Property("rapid")
    private int rapidRating;

    @TargetNode
    private ClubNode club;
}
