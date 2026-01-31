package it.unipi.chessApp.model.neo4j;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.neo4j.core.schema.Id;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.ArrayList;
import java.util.List;

@Node("USER")
@Data
@NoArgsConstructor
public class UserNode {
    @Id
    @Property("mongo_id")
    private String id;

    private String name;

    @Relationship(type = "FOLLOWS", direction = Relationship.Direction.OUTGOING)
    private List<UserNode> following = new ArrayList<>();

    @Relationship(type = "JOINED", direction = Relationship.Direction.OUTGOING)
    private List<ClubMember> clubs = new ArrayList<>();

    @Relationship(type = "PARTECIPATED", direction = Relationship.Direction.OUTGOING)
    private List<TournamentParticipant> tournaments = new ArrayList<>();

    public UserNode(String id, String name) {
        this.id = id;
        this.name = name;
    }
}
