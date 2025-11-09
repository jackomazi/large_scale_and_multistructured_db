# testAIRFLOW

## Descrizione
Questo repository contiene un progetto di esempio che utilizza Apache Airflow per eseguire lo scraping dei dati dei giochi di scacchi da Chess.com e salvarli in un database MongoDB.

In particolare ho utilizzato Astro, una distribuzione di Airflow basata su Docker, che gestisce automaticamente diversi container per:
- Scheduler
- Webserver
- Triggerer
- DAG processor
- Postgres (database backend di Airflow per i metadata)

Ogni componente di Airflow viene eseguito in container separat, ma comunicano tra loro tramite una rete Docker dedicata.<br>
Ogni DAG definito gira all'interno del container scheduler, che esegue i task definiti nei DAG.<br>
Ogni connessione al database MongoDB avviene tramite la rete Docker, utilizzando l'hostname del servizio MongoDB definito nel file `docker-compose.override.yml`.

Astro utilizza Docker Compose per orchestrare i vari container necessari per eseguire Airflow in un ambiente di sviluppo locale.

Per comunicare con MongoDB, il DAG utilizza l'hostname `mongo`, che corrisponde al nome del servizio definito nel file `docker-compose.override.yml`.

*Spiegazione file docker-compose.override*:
`mongo` è il nome del servizio MongoDB.<br>
`image: mongo:6` specifica l'immagine Docker da utilizzare per il container MongoDB.<br>
`container_name: mongo` imposta il nome del container Docker.<br>
`ports` espone la porta 27017 del container alla porta 27017 della macchina host, permettendo di connettersi a MongoDB dall'esterno del container (da MongoDB Compass, ad esempio).<br>
`volumes` monta un volume Docker per persistere i dati di MongoDB anche se il container viene riavviato o eliminato.<br>
`networks` collega il container MongoDB alla rete Docker di Airflow, permettendo la comunicazione tra i container e MongoDB.

In pratica ora il container mongo è collegato alla rete di Airflow e può essere raggiunto dagli altri container (scheduler, webserver, ecc.) utilizzando l'hostname `mongo` e la porta 27017.


## Istruzioni
Il primo passo quello di installare Astro CLI, un tool che semplifica la gestione di Airflow creando un ambiente di sviluppo locale.

Requisiti:
- Docker o Podman, un servizio di containerizzazione
- WSL (Windows Subsystem for Linux) se usi Windows

Vai al sito https://www.astronomer.io/docs/astro/cli/install-cli
e segui le istruzioni per il tuo sistema operativo.

Io ho seguito l'installazione manuale, scaricando da https://github.com/astronomer/astro-cli/releases la versione 1.36.0 per Windows.

Ho scaricato il file .exe e l'ho rinominato in astro.exe, poi l'ho spostato nella cartella C:\Program Files\Astro\
e ho aggiunto questa cartella al PATH di sistema.

Dopodiché ho aperto un terminale PowerShell e ho verificato l'installazione con il comando:

```powershell
astro version
```
Dovrebbe restituire la versione di Astro CLI installata.

Ora posso creare un nuovo progetto Airflow con il comando:

```powershell
astro dev init
```

**Nota**: clonando questo repository, il comando non è necessario, in quanto il progetto è già configurato.

Prima di continuare assicurati che Docker sia in esecuzione.
Ora posso avviare l'ambiente di sviluppo di Airflow con il comando:

```powershell
astro dev start
```
Questo avvia tutti i container necessari per eseguire Airflow.

Dopo qualche istante, posso accedere all'interfaccia web di Airflow aprendo un browser e andando su http://localhost:8080.

## Contenuto

- [dags/](dags/): contiene i DAG di Airflow per il progetto.<br>
    Al momento l'unico DAG presente è chess_com_scraping_dag.py, che esegue lo scraping dei dati da Chess.com e li salva in MongoDB.
- [plugins/](plugins/): contiene i plugin personalizzati di Airflow.
- [.astro/](.astro/): contiene i file di configurazione per l'ambiente Astro di Airflow.
    - docker-compose.override.yml: file per Docker Compose.
