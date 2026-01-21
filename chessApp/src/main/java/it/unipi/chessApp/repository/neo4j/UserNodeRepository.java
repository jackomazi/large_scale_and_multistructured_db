package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.model.neo4j.ClubMember;
import it.unipi.chessApp.model.neo4j.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserNodeRepository extends Neo4jRepository<UserNode, String> {

    @Query("""
            MATCH (p)-[r:JOINED]->(c)
            WHERE p.name STARTS WITH $name
            RETURN
                p.mongo_id AS id,
                p.name AS name, 
                r.country AS country,
                r.bullet AS bulletRating,
                r.blitz AS blitzRating,
                r.rapid AS rapidRating
            """)
    Optional<ClubMember> findJoinedClub(String name);

    @Query("""
            MATCH (p:USER {name: $userName})
            MATCH (c:CLUB {name: $newClubName})
            OPTIONAL MATCH (p)-[old:JOINED]->(:CLUB)
            DELETE old
            MERGE (p)-[r:JOINED]->(c)
            ON CREATE SET
                r.join_date = date(),
                r.country = $country,
                r.bullet = $bullet,
                r.blitz = $blitz,
                r.rapid = $rapid
            """)
    Optional<Void> createJoinedRelation(String userName,
                                        String newClubName,
                                        String country,
                                        int bullet,
                                        int blitz,
                                        int rapid);

    @Query("MATCH (u:USER {name: $name}) RETURN u")
    Optional<UserNode> findByName(String name);

    @Query("""
        MATCH (a:USER {mongo_id: $SourceId})
        MATCH (b:USER {mongo_id: $TargetId})
        MERGE (a)-[:FOLLOWS]->(b)
        """)
    void follow(String SourceId, String TargetId);

    @Query("""
            MATCH (follower:USER {mongo_id: $SourceId})-[r:FOLLOWS]->(followed:USER {mongo_id: $TargetId})
            DELETE r
        """)
    void deleteFollowsRelationship(String SourceId, String TargetId);
}
