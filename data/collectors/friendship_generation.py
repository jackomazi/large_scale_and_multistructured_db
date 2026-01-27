import json
import sys
import os
import random
from api.chess_com import chess_com_interface
from pymongo import MongoClient
from bson.objectid import ObjectId
#Using neo4j db method to memorize key relationships
from storage.neo4j_interface import neo4j_interface

#Neo4J interface
neo4j_dr = neo4j_interface()

users = neo4j_dr.get_all_users()

to_connect = []

for user1 in users:
    for user2 in users:
        id1 = user1.get("mongo_id")
        id2 = user2.get("mongo_id")
        
        if id1 == id2:
            continue
        
        # Follow chance
        if random.random() < 0.0015:
            print(f"{id1} follows {id2}")
            to_connect.append({'id1': id1, 'id2': id2})

print(f"Trying to memorize {len(to_connect)} in neo4j")
if to_connect:
    neo4j_dr.connect_users_bulk(to_connect)
    print(f"Created {len(to_connect)} follow relationship with success.")