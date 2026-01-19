package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.model.neo4j.ClubNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClubNodeRepository extends Neo4jRepository<ClubNode, String> {
}
