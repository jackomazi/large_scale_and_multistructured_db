# large_scale_and_multistructured_db
    Progetto di large scale and multistructured databases @ Unipi. 
    Di seguito descriviamo come procedere all'implementazione del progetto dall'inizio alla consegna

## Data collection:
    Per la parte di data collection fare riferimento al README nella directory "data", tutte le cose descritte di seguito andranno scritte li. Utilizzamo una struttura del testo che rispeccha quanto descritto di seguito.

### Sources:
    Per la parte di data collection è importante avere più di una sorgente di dati per il database, vogliamo utilizzare i dati di chess.com e lichess ed integrarli, inoltre potrebbe essere utile un qualche tipo di database di mosse comuni, di modo da poter poi fare delle query su graphdb.
    ad esempio:
    Sicilian Defense: 1.e4 c5.
    Queen's Gambit: 1.d4 d5 2.c4.
    London System: 1.d4 d5 2.Nf3 Nf6 3.Bf4.
    Inoltre potrebbe essere utile aggiungere funzinalità "social" anche per arricchire un po' la parte di graphdb, possiamo inventarci informazioni sugli utenti usando faker.
    Da valutare se queste sorgenti di dati sono sufficienti

### Modeling:
    Per procedere alla parte di modellazione dei dati da mettere su db dobbiamo esplicitare gli step che abbiamo fatto a partire dai dati raw, per ogni api da cui abbiamo preso dei dati mettiamo un dump generico (direttamente nel readme) dei dati di quella api e diciamo quali dati ci interessano e perchè.

### Join:
    A questo punto dobbiamo fare delle ipotesi su come integrare le fonti di dati, alcuni campi dovranno essere rimossi/modificati per renderli conformi, per ognuno di questi si deve dire cosa si è fatto e perchè

### Final dataset and queries:
    A questo punto abbiamo un insieme possibile di collection da usare nel db, queste vanno descritte accuratamente, e vanno descritte anche le possibili query da fare su queste

## Project presentation:
    Non appena abbiamo chiarito come vogliamo strutturare il db possiamo iniziare a preparare la presentazione dell'idea, non è necessario che il codice per la parte di data collection sia completo. Questa direi che possiamo farla usando il template del prof, non importa usare typst

## Java api implementation:
    Coming soon
