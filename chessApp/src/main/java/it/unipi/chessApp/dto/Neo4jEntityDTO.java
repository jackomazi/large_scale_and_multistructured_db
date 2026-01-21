package it.unipi.chessApp.dto;

import it.unipi.chessApp.model.neo4j.ClubNode;
import it.unipi.chessApp.model.neo4j.TournamentNode;
import it.unipi.chessApp.model.neo4j.UserNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Neo4jEntityDTO {
    private String mongoId;
    private String name;

    public String getMongoId() {
        return mongoId;
    }

    public void setMongoId(String mongoId) {
        this.mongoId = mongoId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public static Neo4jEntityDTO convertToDTO(UserNode node){
        Neo4jEntityDTO dto = new Neo4jEntityDTO();
        dto.setMongoId(node.getId());
        dto.setName(node.getName());
        return dto;
    }
    public static Neo4jEntityDTO convertToDTO(ClubNode node){
        Neo4jEntityDTO dto = new Neo4jEntityDTO();
        dto.setMongoId(node.getId());
        dto.setName(node.getName());
        return dto;
    }
    public static Neo4jEntityDTO convertToDTO(TournamentNode node){
        Neo4jEntityDTO dto = new Neo4jEntityDTO();
        dto.setMongoId(node.getId());
        dto.setName(node.getName());
        return dto;
    }
}
