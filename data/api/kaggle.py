import os
import shutil
import csv
# --- CONFIGURAZIONE CREDENZIALI ---
# Incolla qui il token che hai ricevuto (assicurati di non condividerlo pubblicamente!)
os.environ['KAGGLE_KEY'] = '4f1e3979430dfe8b70c03f0514fd18df'
os.environ['KAGGLE_USERNAME'] = 'andreagiacomazzi' # Lo trovi dentro il file kaggle.json o nel profilo
import zipfile
from kaggle.api.kaggle_api_extended import KaggleApi
from datetime import datetime

class KaggleInterface:
    _eco_map = None  # cache: ECO -> opening name

    @staticmethod
    def _load_eco_codes():
        """
        Carica eco_codes.csv una sola volta e crea il dizionario.
        """
        if KaggleInterface._eco_map is not None:
            return

        eco_path = "./kaggle_chess_data/csv/csv/eco_codes.csv"
        eco_map = {}
        print("Reading eco codes...")
        with open(eco_path, newline="", encoding="utf-8") as f:
            reader = csv.DictReader(f)
            for row in reader:
                # supponiamo colonne: eco, name
                eco_map[row["eco"].strip()] = row["eco_name"].strip()

        KaggleInterface._eco_map = eco_map
    
    @staticmethod
    def eco_to_opening(eco_code: str) -> str | None:
        """
        Restituisce il nome dell'apertura dato un ECO code.
        """
        if not eco_code:
            return None

        KaggleInterface._load_eco_codes()
        return KaggleInterface._eco_map.get(eco_code)
    
    @staticmethod
    def download_chess_pgns(start_year: int =1970, end_year: int=2021):
        dataset = "zq1200/world-chess-championships-1866-to-2021"
        download_path = "./kaggle_chess_data"
        
        # Inizializza e autentica le API di Kaggle
        api = KaggleApi()
        api.authenticate()

        # Crea la cartella di destinazione se non esiste
        if not os.path.exists(download_path):
            os.makedirs(download_path)

        print(f"Scarico il dataset {dataset}...")
        # Scarichiamo l'intero zip (Kaggle non permette il download selettivo di file singoli via API)
        api.dataset_download_files(dataset, path=download_path, unzip=True)

        
        filtered_dir = os.path.join(download_path, "filtered_pgns")

        if not os.path.exists(filtered_dir):
            os.makedirs(filtered_dir)

        print("Filtraggio file in corso...")
        
        count = 0
        # Usiamo os.walk per cercare in TUTTE le sottocartelle create dall'unzip
        for root, dirs, files in os.walk(download_path):
            # Evitiamo di scansionare la cartella dove stiamo spostando i file
            if "filtered_pgns" in root:
                continue
                
            for filename in files:
                if filename.endswith(".pgn"):
                    # Estrazione anno: ultimi 4 caratteri prima di .pgn
                    name_without_ext = filename[:-4]
                    year_str = name_without_ext[-4:]
                    
                    try:
                        year = int(year_str)
                        if start_year <= year <= end_year:
                            source_file = os.path.join(root, filename)
                            destination_file = os.path.join(filtered_dir, filename)
                            
                            # Usiamo copy2 invece di rename per evitare errori tra file system diversi
                            shutil.copy2(source_file, destination_file)
                            count += 1
                    except ValueError:
                        continue
        print(f"--- Operazione Completata ---")
        print(f"File totali trovati e copiati: {count}")
        print(f"Percorso: {os.path.abspath(filtered_dir)}")



    @staticmethod
    def parse_pgn_to_dict(game):
        """
        Riceve un oggetto partita dalla libreria chess e lo trasforma in 
        un dizionario pronto per MongoDB.
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