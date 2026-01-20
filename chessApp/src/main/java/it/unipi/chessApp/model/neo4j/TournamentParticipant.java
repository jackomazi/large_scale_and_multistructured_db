package it.unipi.chessApp.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.RelationshipId;
import org.springframework.data.neo4j.core.schema.RelationshipProperties;
import org.springframework.data.neo4j.core.schema.TargetNode;

@RelationshipProperties
@Data
@NoArgsConstructor
public class TournamentParticipant {
    @RelationshipId
    private Long id;

    private String name;
    private int wins;
    private int draws;
    private int losses;
    private int placement;

    @TargetNode
    private TournamentNode tournament;
}
