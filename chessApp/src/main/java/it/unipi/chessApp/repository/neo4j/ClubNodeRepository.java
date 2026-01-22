package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.model.neo4j.ClubNode;
import it.unipi.chessApp.model.neo4j.ClubMember;
import it.unipi.chessApp.model.neo4j.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface ClubNodeRepository extends Neo4jRepository<ClubNode, String> {

    @Query("""
            MATCH (p)-[r:JOINED]->(c)
            WHERE c.name STARTS WITH $name
            RETURN
                p.mongo_id AS id,
                p.name AS name, 
                r.country AS country,
                r.bullet AS bulletRating,
                r.blitz AS blitzRating,
                r.rapid AS rapidRating
            """)
    List<ClubMember> findClubMembers(String name);

    Optional<ClubNode> findByName(String name);
}
