import os
import shutil
import csv

os.environ['KAGGLE_KEY'] = 'ask_andrea'
os.environ['KAGGLE_USERNAME'] = 'andreagiacomazzi' # necessary for kaggle API to work
import zipfile
from kaggle.api.kaggle_api_extended import KaggleApi
from datetime import datetime

class KaggleInterface:
    _eco_map = None  # cache: ECO -> opening name

    @staticmethod
    def _load_eco_codes():
        """
        Loads the ECO codes from the CSV file into a dictionary.
        """
        if KaggleInterface._eco_map is not None:
            return

        eco_path = "./kaggle_chess_data/csv/csv/eco_codes.csv"
        eco_map = {}
        print("Reading eco codes...")
        with open(eco_path, newline="", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                # eco and eco_name columns
                eco_map[row["eco"].strip()] = row["eco_name"].strip()

        KaggleInterface._eco_map = eco_map
    
    @staticmethod
    def eco_to_opening(eco_code: str) -> str | None:
        """
        Converts an ECO code to its corresponding opening name.
        Returns None if the code is not found.
        """
        if not eco_code:
            return None

        KaggleInterface._load_eco_codes()
        return KaggleInterface._eco_map.get(eco_code)
    
    @staticmethod
    def download_chess_pgns(start_year: int =1970, end_year: int=2021):
        dataset = "zq1200/world-chess-championships-1866-to-2021"
        download_path = "./kaggle_chess_data"
        
        # Kaggle API authentication
        api = KaggleApi()
        api.authenticate()

        # Create download directory if it doesn't exist
        if not os.path.exists(download_path):
            os.makedirs(download_path)

        print(f"Scarico il dataset {dataset}...")
        # Download and unzip the dataset
        api.dataset_download_files(dataset, path=download_path, unzip=True)

        
        filtered_dir = os.path.join(download_path, "filtered_pgns")

        if not os.path.exists(filtered_dir):
            os.makedirs(filtered_dir)

        print(f"Filter files from {start_year} to {end_year}...")
        
        count = 0
        
        for root, dirs, files in os.walk(download_path):
            
            if "filtered_pgns" in root:
                continue
                
            for filename in files:
                if filename.endswith(".pgn"):
                    # extract year from filename
                    name_without_ext = filename[:-4]
                    year_str = name_without_ext[-4:]
                    
                    try:
                        year = int(year_str)
                        if start_year <= year <= end_year:
                            source_file = os.path.join(root, filename)
                            destination_file = os.path.join(filtered_dir, filename)
                            
                            # copy file to filtered directory
                            shutil.copy2(source_file, destination_file)
                            count += 1
                    except ValueError:
                        continue
        print(f"--- Operation completed! {count} files copied to '{filtered_dir}' ---")



    @staticmethod
    def parse_pgn_to_dict(game):
        """
        Parses a chess.pgn.Game object into a dictionary with cleaned data.
        """
        # headers → lowercase
        game_dict = {k.lower(): v for k, v in game.headers.items()}
        
        # moves
        game_dict["moves"] = str(game.mainline_moves())
        
        # rating
        try:
            game_dict["white_rating"] = int(game_dict.pop("whiteelo"))
        except (KeyError, ValueError):
            game_dict["white_rating"] = None

        try:
            game_dict["black_rating"] = int(game_dict.pop("blackelo"))
        except (KeyError, ValueError):
            game_dict["black_rating"] = None

        # result → result_white / result_black
        result = game_dict.pop("result", None)

        if result == "1-0":
            game_dict["result_white"] = "win"
            game_dict["result_black"] = "loss"
        elif result == "0-1":
            game_dict["result_white"] = "loss"
            game_dict["result_black"] = "win"
        elif result == "1/2-1/2":
            game_dict["result_white"] = "draw"
            game_dict["result_black"] = "draw"
        else:
            game_dict["result_white"] = None
            game_dict["result_black"] = None
        
        game_dict.pop("eventdate", None)

        # date
        pgn_date_str = game_dict.pop("date", None)

        if not pgn_date_str or "????" in pgn_date_str:
            game_dict["date"] = None
        else:
            clean_date = pgn_date_str.replace("??", "01").replace(".", "-")
            try:
                dt = datetime.strptime(clean_date, "%Y-%m-%d")
                game_dict["date"] = dt.strftime("%Y-%m-%d %H:%M:%S")
            except ValueError:
                game_dict["date"] = None

        # rename white/black
        game_dict["white_player"] = game_dict.pop("white")
        game_dict["black_player"] = game_dict.pop("black")

        game_dict["time_class"] = "classical"
        try:
            eco_code = game_dict.pop("eco")
        except KeyError:
            eco_code = None
        game_dict["opening"] = KaggleInterface.eco_to_opening(eco_code)
        
        game_dict["historical"] = True

        game_dict.pop("round")
        return game_dict