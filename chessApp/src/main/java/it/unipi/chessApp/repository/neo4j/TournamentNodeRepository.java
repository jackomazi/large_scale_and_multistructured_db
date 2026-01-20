package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.model.neo4j.ClubMember;
import it.unipi.chessApp.model.neo4j.TournamentNode;
import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TournamentNodeRepository extends Neo4jRepository<TournamentNode, String> {

    @Query("""
            MATCH (p)-[r:PARTECIPATED]->(t)
            WHERE t.name STARTS WITH $name
               RETURN
               p.mongo_id AS id,
               p.name AS name,
               r.wins AS wins,
               r.losses AS losses,
               r.draws AS draws,
               r.placement AS placement
            """)
    List<TournamentParticipant> findTournamentParticipants(String name);

}
