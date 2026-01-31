package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.dto.TournamentPlayerResultDTO;
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
    MATCH (t:TOURNAMENT {mongo_id: $id})
    MATCH (p:USER)-[r:PARTECIPATED]->(t)
    RETURN
       p.mongo_id AS participantId,
       p.name AS name,
       r.wins AS wins,
       r.losses AS losses,
       r.draws AS draws,
       r.placement AS placement
    """)
    List<TournamentPlayerResultDTO> findTournamentParticipants(String id);

    @Query("""
            MATCH (p: USER {mongo_id: $userID})
            MATCH (t: TOURNAMENT {mongo_id: $tournamentID})
            MERGE (p)-[r:PARTECIPATED]->(t)
            ON CREATE SET
                r.wins = 0,
                r.draws = 0,
                r.losses = 0,
                r.placement = -1
            """)
    Void createParticipatedRelation(String userID, String tournamentID);

    @Query("""
            MATCH (t:TOURNAMENT {mongo_id: $tournamentId})
            DETACH DELETE t
            """)
    void deleteByMongoId(String tournamentId);

}
