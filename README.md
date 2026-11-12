# Rubrica

Rubrica telefonica in Java Swing: elenco di persone (nome, cognome, indirizzo,
telefono, età) con inserimento, modifica ed eliminazione. I dati sono salvati in un
database **MySQL** tramite JDBC; l'accesso è protetto da login e ogni utente vede e
modifica solo i propri contatti.

## Installazione

Servono Java 8 o successivo e un server MySQL 8 o successivo. Il pacchetto `Rubrica.zip`
contiene `Rubrica.jar`, `schema_database.sql` e `credenziali_database.properties`.

1. Decomprimere `Rubrica.zip`.
2. Aprire `credenziali_database.properties` e inserire i dati del proprio MySQL:
   `username`, `password`, `ip-server-mysql` e `porta`. Il file deve restare nella
   stessa cartella di `Rubrica.jar`.
3. Eseguire `schema_database.sql` sul proprio MySQL, ad esempio
   `mysql -u root -p < schema_database.sql` oppure aprendolo in MySQL Workbench.
   Crea il database `rubrica` con le tabelle `utenti` e `persone`; rieseguirlo non
   cancella nulla.
4. Avviare `Rubrica.jar` con doppio clic, oppure da terminale: `java -jar Rubrica.jar`

Se qualcosa non va, all'avvio un messaggio spiega il problema: credenziali errate,
database non creato, server MySQL non raggiungibile.

## Uso

- All'avvio compare solo la finestra di login. Con il bottone **REGISTRA** si crea un
  nuovo utente (nome utente, password di almeno 6 caratteri e conferma); dopo la
  registrazione si entra direttamente nella propria rubrica, inizialmente vuota.
- **Al primo avvio** (nessun utente nel database) la registrazione si apre da sola.
- Il titolo della finestra principale mostra l'utente che ha fatto il login.
- **Logout**, a destra nella toolbar, chiude la rubrica e riapre il login, così si può
  entrare con un altro utente senza riavviare il programma.
- Il doppio clic su una riga apre la persona in modifica. Nelle finestre di login,
  registrazione ed editor, Invio conferma ed Esc annulla.

Regole dell'editor:

- è obbligatorio almeno uno tra nome e cognome;
- il telefono è obbligatorio e può contenere solo cifre, spazi, i simboli `( ) - . /`
  e un `+` iniziale (es. `+39 348 000 0000`, `(212) 555-1234`, `030/123456`), con
  almeno 3 cifre. Gli altri caratteri non si possono nemmeno digitare;
- indirizzo ed età sono facoltativi; l'età, se indicata, deve essere un numero intero tra 1 e 150;
- lunghezze massime, come le colonne del database: nome e cognome 100 caratteri,
  indirizzo 255, telefono 50.

Se il database non risponde durante un salvataggio, compare l'errore e l'editor resta
aperto con i dati inseriti, così si può riprovare.

## Database

| Tabella | Colonne |
|---|---|
| `utenti` | `id`, `username` (unico, distingue maiuscole e minuscole), `iterazioni`, `salt`, `hash` |
| `persone` | `id`, `id_utente` (→ `utenti.id`), `nome`, `cognome`, `indirizzo`, `telefono`, `eta` (`NULL` se non indicata) |

**Password.** Non viene mai salvata in chiaro: la tabella `utenti` contiene solo il suo
hash, calcolato con **PBKDF2-HMAC-SHA256** (standard PKCS #5 / RFC 8018, incluso in
Java) con 600.000 iterazioni e un salt casuale di 16 byte diverso per ogni utente.
Con dati errati compare il messaggio "Login errato." e la rubrica non si apre.

**Contatti separati per utente.** Ogni contatto ha l'`id_utente` del suo proprietario e
tutte le query (lettura, modifica, eliminazione) filtrano per l'utente che ha fatto il
login: un utente non può vedere né cambiare i contatti di un altro, nemmeno
conoscendone l'id. Chiunque apra il programma può registrarsi, ma parte con una rubrica
vuota.

`credenziali_database.properties` contiene la password di MySQL in chiaro, come
richiesto dalla traccia: chi può leggere quel file può accedere al database.

## Compilazione

Il driver JDBC è in `lib/mysql-connector-j-26.10.0.jar` (MySQL Connector/J, licenza
GPLv2 con Universal FOSS Exception); in Eclipse va aggiunto al Build Path del progetto.
Con un JDK 9 o successivo, da PowerShell nella cartella del progetto:

```powershell
.\build.ps1
```

Lo script compila i sorgenti per Java 8, esegue le verifiche automatiche che non
richiedono il database (regole su età e telefono, hash delle password) e crea in `dist/`:

| File | Contenuto |
|---|---|
| `Rubrica.jar` | Il programma, con il driver MySQL incluso: basta il doppio clic |
| `schema_database.sql` | Copia di `database/schema_database.sql` |
| `credenziali_database.properties` | Copiato dal modello `database/credenziali_database.properties` solo se manca: qui si mettono le proprie credenziali per provare il programma (`dist/` è esclusa da git) |
| `Rubrica.zip` | Il pacchetto da consegnare: jar, script e il **modello** delle credenziali, mai quelle di `dist/` |

Per verificare anche il codice del database su un MySQL vero:

```powershell
.\build.ps1 -VerificaDatabase dist\credenziali_database.properties
```

La verifica crea un database di prova `rubrica_verifica` con lo stesso script, controlla
registrazione, login, contatti e separazione tra utenti, e alla fine lo elimina.

Per provare senza installare MySQL si può usare Docker, scegliendo una password e
scrivendola anche in `dist/credenziali_database.properties`:

```powershell
docker run -d --name rubrica-mysql -p 127.0.0.1:3306:3306 -e MYSQL_ROOT_PASSWORD=<password> mysql:8.4
```

## Struttura

| Package | Classi | Ruolo |
|---|---|---|
| `rubrica` | `Main` | Avvio: credenziali del database, login, poi finestra principale |
| `rubrica.dominio` | `Persona`, `Utente` | Dati di un contatto (con le regole su età e telefono) e di un utente |
| `rubrica.logica` | `Rubrica`, `Autenticazione`, `HashPassword` | Contatti dell'utente in un `Vector`, salvati nel database a ogni modifica; login e registrazione; hash PBKDF2 delle password |
| `rubrica.persistenza` | `ConfigurazioneDatabase`, `GestorePersone`, `GestoreUtenti` | Lettura delle credenziali e accesso alle tabelle `persone` e `utenti` con JDBC |
| `rubrica.ui` | `FinestraLogin`, `FinestraRegistrazione`, `FinestraPrincipale`, `EditorPersona`, `PersonaTableModel`, `FiltroTelefono`, `Modulo`, `Messaggi`, `BottoneToolbar`, `Icona`, `IconaApplicazione` | Interfaccia Swing con `JToolBar`; le icone dei bottoni e delle finestre sono disegnate via codice |
