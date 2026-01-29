package it.unipi.chessApp.repository.neo4j;

import it.unipi.chessApp.model.neo4j.ClubMember;
import it.unipi.chessApp.model.neo4j.FriendRecommendation;
import it.unipi.chessApp.model.neo4j.TournamentParticipant;
import it.unipi.chessApp.model.neo4j.UserNode;
import org.springframework.data.neo4j.repository.Neo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
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
    void createJoinedRelation(String userName,
                                        String newClubName,
                                        String country,
                                        int bullet,
                                        int blitz,
                                        int rapid);

    @Query("""
            MATCH (p:USER {mongo_id: $userId})
            MATCH (p)-[r:PARTECIPATED]->(t:TOURNAMENT)
            RETURN
               p.mongo_id AS id,
               p.name AS name,
               r.wins AS wins,
               r.losses AS losses,
               r.draws AS draws,
               r.placement AS placement,
               t AS tournament
            """)
    List<TournamentParticipant> findUserTournaments(String userId);

    @Query("""
            MATCH (p:USER {mongo_id: $userId})
            MATCH (p)-[r:JOINED]->(c:CLUB)
            SET
                r.bullet = $bullet,
                r.blitz = $blitz,
                r.rapid = $rapid
            """)
    void updateJoinedRelation(String userId,
                                        int bullet,
                                        int blitz,
                                        int rapid);

    @Query("""
            MATCH (p:USER {mongo_id: $userId})
            MATCH (t:TOURNAMENT {mongo_id: $tournamentId})
            MATCH (p)-[r:PARTECIPATED]->(t)
            SET
               r.wins = $wins,
               r.losses = $losses,
               r.draws = $draws,
               r.placement = $placement
            """)
    Optional<Void> updateUserTournamentStats(String userId,
                                             String tournamentId,
                                             int wins,
                                             int losses,
                                             int draws,
                                             int placement);

    @Query("""
            MATCH (p:USER {mongo_id: $userId})
            MATCH (t:TOURNAMENT {mongo_id: $tournamentId})
            MATCH (p)-[r:PARTECIPATED]->(t)
               RETURN
               p.mongo_id AS id,
               p.name AS name,
               r.wins AS wins,
               r.losses AS losses,
               r.draws AS draws,
               r.placement AS placement,
               t AS tournament
            """)
    TournamentParticipant findUserTournamentStats(String tournamentId, String userId);

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

    @Query("""
            MATCH (me:USER {mongo_id: "6970a8eafdb64c9b443d55de"})
            
            MATCH (me)-[:FOLLOWS|JOINED|PARTECIPATED*2]-(consigliato:USER)
            
            WHERE consigliato <> me
              AND NOT (me)-[:FOLLOWS]->(consigliato)
            
            WITH me, consigliato
            OPTIONAL MATCH (me)-[:FOLLOWS]->(amico:USER)-[:FOLLOWS]->(consigliato)
            WITH me, consigliato, collect(DISTINCT amico.name) AS amiciInComune
            
            OPTIONAL MATCH (me)-[:JOINED|PARTECIPATED]->(comune)<-[:JOINED|PARTECIPATED]-(consigliato)
            WITH me, consigliato, amiciInComune, collect(DISTINCT labels(comune)[0]) AS interessiLabels
            
            RETURN
                consigliato.mongo_id AS mongoID,
                consigliato.name AS name,
                CASE
                    WHEN size(amiciInComune) > 0 THEN "Seguito da " + amiciInComune[0] + (CASE WHEN size(amiciInComune) > 1 THEN " + altri" ELSE "" END)
                    WHEN size(interessiLabels) > 0 THEN "Insieme in " + interessiLabels[0]
                    ELSE "Suggerito per te"
                END AS connectionType
            ORDER BY size(amiciInComune) DESC, size(interessiLabels) DESC
            LIMIT 10
            
            """)
    List<FriendRecommendation> suggestFriends(String userID);

    @Query("""
            MATCH (me:USER {mongo_id: $userID})-[:FOLLOWS]->(friend:USER)
            RETURN
                friend.mongo_id AS mongo_id,
                friend.name AS name
            """)
    List<UserNode> findUserFollows(String userID);

    @Query("""
            MATCH (me:USER {mongo_id: $userID})<-[:FOLLOWS]-(friend:USER)
            RETURN friend
            """)
    List<UserNode> findUserFollowers(String userID);
}
