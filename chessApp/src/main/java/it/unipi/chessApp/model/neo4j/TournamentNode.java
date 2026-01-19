package it.unipi.chessApp.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Node("TOURNAMENT")
@Data
@NoArgsConstructor
public class TournamentNode {
    @Id
    @Property("mongo_id")
    private String id;

    private String name;

    public TournamentNode(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
