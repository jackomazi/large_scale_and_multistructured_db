import os
import sys
from api.kaggle import KaggleInterface
from pymongo import MongoClient
from storage.mongo_interface import mongo_db_interface
import chess.pgn

if __name__ == "__main__":
    try:
        client = MongoClient("mongodb://localhost:27017/")
        client.server_info()

        # change the database name if needed
        db = client["chess_db_test_3"]

        # drop games collection if exists
        db.drop_collection("games")


        collection_historical = db["games"]

        print("MongoDB connection successful")
    except:
        print("Could not connect to MongoDB, exiting...")
        sys.exit(1)


    download_path = "./kaggle_chess_data"
    filtered_path = os.path.join(download_path, "filtered_pgns")

    # --- NUOVO CONTROLLO ---
    if not os.path.exists(download_path):
        KaggleInterface.download_chess_pgns()
    else:
        print(f"La cartella '{download_path}' esiste già.")

    if os.path.exists(filtered_path):
        all_games = []

        for filename in os.listdir(filtered_path):
            if filename.endswith(".pgn"):
                file_path = os.path.join(filtered_path, filename)
                print(f"Elaborazione file: {filename}...")
                
                with open(file_path, encoding="utf-8") as pgn_file:
                    # Leggiamo tutte le partite nel file una dopo l'altra
                    while True:
                        game = chess.pgn.read_game(pgn_file)
                        if game is None:
                            break  # Fine del file
                        
                        # Trasformiamo la partita in dizionario
                        game_data = KaggleInterface.parse_pgn_to_dict(game)
                        
                        historical_mongo_id = mongo_db_interface.store_dict_to_MongoDB(game_data, collection_historical)
                        
                        

        print("\nProcesso di parsing completato!")
    
    

