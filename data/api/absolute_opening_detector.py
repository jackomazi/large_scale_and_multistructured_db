import chess
import chess.pgn
import io
import json
import os
import glob

class ChessOpeningDetector:
    def __init__(self, json_folder_path="api/opening_json_data"):
        self.openings_db = {}
        
        # Carica tutti i file .json trovati nella cartella indicata
        # (Modifica il path se i file sono altrove, es: "C:/dati/scacchi")
        self._load_local_json_files(json_folder_path)

    def _load_local_json_files(self, folder_path):
        # Cerca tutti i file che finiscono per .json
        pattern = os.path.join(folder_path, "*.json")
        files = glob.glob(pattern)
        
        if not files:
            print(f"Nessun file JSON trovato in: {folder_path}")
            return

        print(f"Trovati {len(files)} file JSON. Caricamento in corso...")
        
        for file_path in files:
            try:
                with open(file_path, 'r', encoding='utf-8') as f:
                    data = json.load(f)
                    
                    # Il tuo formato è: "FEN_STRING": { "name": "...", ... }
                    for fen_full, info in data.items():
                        # Prendiamo il nome
                        name = info.get("name", "Unknown")
                        
                        # IMPORTANTE: Normalizziamo il FEN (chiave)
                        # Prendiamo solo la parte posizionale (prima parte della stringa)
                        # Esempio: "rnbqkbnr/... 0 3" -> "rnbqkbnr/..."
                        fen_key = fen_full.split(" ")[0]
                        
                        self.openings_db[fen_key] = name
                        
            except Exception as e:
                print(f"Errore caricamento {file_path}: {e}")
                
        print(f"Totale aperture indicizzate: {len(self.openings_db)}")

    def get_opening(self, pgn_string):
        # 1. Parsing PGN
        pgn_io = io.StringIO(pgn_string)
        game = chess.pgn.read_game(pgn_io)
        
        if not game:
            return "Invalid PGN"

        # 2. Se c'è l'URL esplicito, usiamo quello (è il più preciso)
        if "ECOUrl" in game.headers:
             url = game.headers["ECOUrl"]
             return url.split("/")[-1].replace("-", " ")

        # 3. Analisi Posizionale (Match con i tuoi JSON)
        board = game.board() # Carica il FEN iniziale incluso [SetUp "1"]
        
        # Cerchiamo la posizione iniziale
        fen_key = board.fen().split(" ")[0]
        if fen_key in self.openings_db:
            return self.openings_db[fen_key]

        # Se non trovata subito, scorriamo le prime 20 mosse
        for move in game.mainline_moves():
            board.push(move)
            fen_key = board.fen().split(" ")[0]
            
            if fen_key in self.openings_db:
                return self.openings_db[fen_key]
            
            # Limite per evitare analisi inutili nel mediogioco
            if board.fullmove_number > 20:
                break
                
        return "Unknown Opening"

# --- ESEMPIO DI UTILIZZO CON IL TUO PGN ---

pgn_broken = """[Event \"2015 A LAST CHESS COMPETITION - Round 1\"]\n[Site \"Chess.com\"]\n[Date 
\"2016.03.18\"]\n[Round \"-\"]\n[White \"dmarkg\"]\n[Black \"Dulli4Life\"]\n[Result \"1-0\"]\n[SetUp \"1\"]
\n[FEN \"rnbqkb1r/ppp1pppp/5n2/3p4/3P1B2/5N2/PPP1PPPP/RN1QKB1R b KQkq - 4 3\"]\n[Tournament 
\"https://www.chess.com/tournament/2015-a-last-chess-competition\"]\n[CurrentPosition \"r7/P7/8/1N6/1KR5/3k4/1P6/8 b - 
- 0 52\"]\n[Timezone \"UTC\"]\n[UTCDate \"2016.03.18\"]\n[UTCTime \"06:39:27\"]\n[WhiteElo \"1295\"]\n[BlackElo \"1271\"]\n[TimeControl 
\"1/259200\"]\n[Termination \"dmarkg won by resignation\"]\n[StartTime \"06:39:27\"]\n[EndDate \"2016.04.25\"]\n[EndTime \"18:20:07\"]\n
[Link \"https://www.chess.com/game/daily/131478998\"]\n\n3... Bg4 4. h3 Bxf3 5. exf3 g6 6. Bb5+ c6 7. Ba4 Nbd7 8. Nc3 Bg7 9. Qd2 b5 10. 
Bb3 O-O 11. Bh6 Bxh6 12. Qxh6 e5 13. O-O-O b4 14. Na4 Qc7 15. c3 c5 16. dxe5 Qxe5 17. f4 Qh5 18. Qxh5 gxh5 19. Bxd5 Nxd5 20. Rxd5 Rad8 21. 
Rhd1 Rfe8 22. Rxd7 Rc8 23. Rxa7 bxc3 24. Nxc3 Rc6 25. Rdd7 Rce6 26. Rxf7 Re1+ 27. Kc2 Rf1 28. Rxh7 Rxf2+ 29. Kb3 c4+ 30. Ka3 Rb8 31. Rhg7+ Kh8 
32. Rab7 Rxb7 33. Rxb7 Rxg2 34. Rb5 Rf2 35. Rxh5+ Kg7 36. Rg5+ Kh6 37. Rg4 Kh5 38. Rg5+ Kh4 39. Nd5 Rf3+ 40. Kb4 Kxh3 41. f5 Kh4 42. Rg2 Rxf5 43. 
Nc3 Kh3 44. Rd2 Rf4 45. Rd5 Kg4 46. 
Rc5 Kf3 47. Rxc4 Rf8 48. a4 Rb8+ 49. Nb5 Ke3 50. a5 Kd3 51. a6 Ra8 52. a7 1-0\n4"""

# Inizializziamo il detector
detector = ChessOpeningDetector()

# Eseguiamo
opening_name = detector.get_opening(pgn_broken)
print(f"Apertura rilevata: {opening_name}")