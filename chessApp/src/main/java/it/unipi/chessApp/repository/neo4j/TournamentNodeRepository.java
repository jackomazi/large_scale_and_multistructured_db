package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.model.neo4j.TournamentNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TournamentNodeRepository extends Neo4jRepository<TournamentNode, String> {
}
