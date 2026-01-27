from neo4j import GraphDatabase, RoutingControl
from dotenv import load_dotenv
import os

load_dotenv()

class neo4j_interface:

    URI = os.getenv("NEO4J_URI", "neo4j://localhost:7687")
    AUTH = (os.getenv("NEO4J_USER", "neo4j"), os.getenv("NEO4J_PASSWORD"))

    def __init__(self, database="square64beta"):
        self.driver = GraphDatabase.driver(neo4j_interface.URI, auth=neo4j_interface.AUTH)
        self.database = database

    def insert_user_entity(self, mongo_id, username):

        query = "CREATE (usr:USER { name: $username, mongo_id: $mongo_id} )"

        try:
            self.driver.execute_query(query, username=username, mongo_id=str(mongo_id), database_=self.database)
        except Exception as e:
            print("Inserting username not successfull")
            print(f"Reason: {e}")

    def insert_club_entity(self, mongo_id, club_name):

        query = "CREATE (clb:CLUB { name: $club_name, mongo_id: $mongo_id} )"

        try:
            self.driver.execute_query(query, club_name=club_name, mongo_id=str(mongo_id), database_=self.database)
        except Exception as e:
            print("Inserting club not successfull")
            print(f"Reason: {e}")

    def insert_tournament_entity(self, mongo_id, tournament_name):

        query = "CREATE (trn:TOURNAMENT { name: $tournament_name, mongo_id: $mongo_id} )"

        try:
            self.driver.execute_query(query, tournament_name=tournament_name, mongo_id=str(mongo_id), database_=self.database)
        except Exception as e:
            print("Inserting tournament not successfull")
            print(f"Reason: {e}")

    def connect_user_tournament(self, mongo_id_user, mongo_id_club, tournament_user_stats=None):

        query = "MATCH (usr:USER) WHERE usr.mongo_id = $mongo_id_user " \
                "MATCH (trn:TOURNAMENT) WHERE trn.mongo_id = $mongo_id_club " \
                "CREATE (usr)-[part:PARTECIPATED]->(trn) " \
                "SET part.wins = $wins " \
                "SET part.draws = $draws " \
                "SET part.losses = $losses " \
                "SET part.placement = $placement"

        try:
            self.driver.execute_query(query, mongo_id_club=str(mongo_id_club), mongo_id_user=str(mongo_id_user),
                                       wins = tournament_user_stats.get("wins"),
                                       losses = tournament_user_stats.get("losses"),
                                       draws = tournament_user_stats.get("draws"),
                                       placement = tournament_user_stats.get("placement"),
                                       database_=self.database)
        except Exception as e:
            print("Connection user-tournament not successfull")
            print(f"Reason: {e}")

    def connect_user_club(self, mongo_id_user, mongo_id_club, club_user_infos):

        query = "MATCH (usr:USER) WHERE usr.mongo_id = $mongo_id_user " \
                "MATCH (clb:CLUB) WHERE clb.mongo_id = $mongo_id_club " \
                "CREATE (usr)-[jnd:JOINED]->(clb) " \
                "SET jnd.country = $country " \
                "SET jnd.bullet = $bullet " \
                "SET jnd.blitz = $blitz " \
                "SET jnd.rapid = $rapid "

        try:
            self.driver.execute_query(query, mongo_id_club=str(mongo_id_club), mongo_id_user=str(mongo_id_user),
                                      country = club_user_infos.get("country"),
                                      bullet = club_user_infos.get("stats").get("bullet"),
                                      blitz = club_user_infos.get("stats").get("blitz"),
                                      rapid = club_user_infos.get("stats").get("rapid"),
                                      database_=self.database)
        except Exception as e:
            print("Connection user-club not successfull")
            print(f"Reason: {e}")

    def connect_user_user(self, mongo_id_user1, mongo_id_user2):

        query = "MATCH (usr:USER) WHERE usr.mongo_id = $mongo_id_user1 " \
                "MATCH (usr1:USER) WHERE usr1.mongo_id = $mongo_id_user2 " \
                "MERGE (usr)-[:FOLLOWS]->(usr1)"

        try:
            self.driver.execute_query(query, mongo_id_user2=str(mongo_id_user2), mongo_id_user1=str(mongo_id_user1), database_=self.database)
        except Exception as e:
            print("Connection user-user not successfull")
            print(f"Reason: {e}")

    def get_all_users(self) -> list:

        query = "MATCH (u:USER)" \
                "RETURN" \
                "   u.mongo_id AS mongo_id," \
                "   u.name AS name"
        
        try:
            records, _, _ = self.driver.execute_query(
                query, 
                database_=self.database,
                routing_=RoutingControl.READ # Ottimale per query di sola lettura
            )
            return [record.data() for record in records]
        except Exception as e:
            print(f"Reason: {e}")
            return []
        
    def connect_users_bulk(self, relationships):
        query = (
            "UNWIND $pairs AS pair "
            "MATCH (usr1:USER {mongo_id: pair.id1}) "
            "MATCH (usr2:USER {mongo_id: pair.id2}) "
            "MERGE (usr1)-[:FOLLOWS]->(usr2)"
        )

        try:
            self.driver.execute_query(
                query, 
                pairs=relationships, 
                database_=self.database
            )
        except Exception as e:
            print(f"Errore nel caricamento bulk: {e}")

        
