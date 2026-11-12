package rubrica.persistenza;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Dati di connessione al server MySQL, letti dal file {@value #NOME_FILE}
 * che sta nella stessa cartella di Rubrica.jar:
 * <pre>
 * username=root
 * password=...
 * ip-server-mysql=localhost
 * porta=3306
 * </pre>
 */
public class ConfigurazioneDatabase {

    public static final String NOME_FILE = "credenziali_database.properties";
    public static final String DATABASE_PREDEFINITO = "rubrica";

    private static final String DRIVER = "com.mysql.cj.jdbc.Driver";
    private static final int TIMEOUT_CONNESSIONE_MS = 5000;
    /** Oltre questo tempo senza risposta dal server l'operazione fallisce invece di bloccare il programma. */
    private static final int TIMEOUT_RISPOSTA_MS = 30000;

    private final String host;
    private final int porta;
    private final String database;
    private final String username;
    private final String password;

    public ConfigurazioneDatabase(String host, int porta, String database, String username, String password) {
        this.host = host;
        this.porta = porta;
        this.database = database;
        this.username = username;
        this.password = password;
    }

    /**
     * Il file {@value #NOME_FILE} nella cartella che contiene Rubrica.jar.
     * Se il programma non è avviato da un JAR (ad esempio dall'IDE) si usa la
     * cartella di lavoro corrente.
     */
    public static File filePredefinito() {
        try {
            File posizione = new File(ConfigurazioneDatabase.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (posizione.isFile()) {
                return new File(posizione.getParentFile(), NOME_FILE);
            }
        } catch (Exception e) {
            // posizione del JAR non determinabile: si usa la cartella di lavoro
        }
        return new File(NOME_FILE);
    }

    /** Legge il file delle credenziali; IOException con un messaggio chiaro se manca o non è valido. */
    public static ConfigurazioneDatabase leggi(File file) throws IOException {
        if (!file.isFile()) {
            throw new IOException("File non trovato: " + file.getAbsolutePath());
        }
        Properties valori = new Properties();
        try (Reader reader = new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8)) {
            valori.load(reader);
        }
        String testoPorta = obbligatorio(valori, "porta");
        int porta;
        try {
            porta = Integer.parseInt(testoPorta);
        } catch (NumberFormatException e) {
            throw new IOException("Il valore di \"porta\" non è un numero: " + testoPorta);
        }
        return new ConfigurazioneDatabase(
                obbligatorio(valori, "ip-server-mysql"),
                porta,
                valori.getProperty("database", DATABASE_PREDEFINITO).trim(),
                obbligatorio(valori, "username"),
                valori.getProperty("password", ""));
    }

    private static String obbligatorio(Properties valori, String chiave) throws IOException {
        String valore = valori.getProperty(chiave, "").trim();
        if (valore.isEmpty()) {
            throw new IOException("Manca il valore di \"" + chiave + "\"");
        }
        return valore;
    }

    /** La stessa configurazione, ma per un altro database dello stesso server. */
    public ConfigurazioneDatabase conDatabase(String altroDatabase) {
        return new ConfigurazioneDatabase(host, porta, altroDatabase, username, password);
    }

    /** Apre una nuova connessione, da chiudere dopo l'uso (try-with-resources). */
    public Connection apriConnessione() throws SQLException {
        try {
            Class.forName(DRIVER);
        } catch (ClassNotFoundException e) {
            throw new SQLException("Driver MySQL (" + DRIVER + ") non trovato nel programma", e);
        }
        // allowPublicKeyRetrieval: necessario con MySQL 8 quando la connessione non usa SSL
        String url = "jdbc:mysql://" + host + ":" + porta + "/" + database
                + "?connectTimeout=" + TIMEOUT_CONNESSIONE_MS + "&socketTimeout=" + TIMEOUT_RISPOSTA_MS
                + "&allowPublicKeyRetrieval=true";
        return DriverManager.getConnection(url, username, password);
    }

    public String getDatabase() {
        return database;
    }

    /** Ad esempio {@code root@localhost:3306/rubrica}. */
    public String descrizione() {
        return username + "@" + host + ":" + porta + "/" + database;
    }

    /** Spiega in italiano gli errori più comuni, seguiti dal dettaglio tecnico di MySQL. */
    public static String descriviErrore(SQLException e) {
        String causa;
        String statoSql = e.getSQLState() == null ? "" : e.getSQLState();
        if (e.getErrorCode() == 1045) {
            causa = "Accesso negato: controllare username e password in " + NOME_FILE + ".";
        } else if (e.getErrorCode() == 1049) {
            causa = "Il database non esiste: eseguire lo script schema_database.sql.";
        } else if (e.getErrorCode() == 1146) {
            causa = "Mancano le tabelle del database: eseguire lo script schema_database.sql.";
        } else if (statoSql.startsWith("08")) {
            causa = "Il server MySQL non risponde. Controllare che sia avviato e i valori\n"
                    + "di ip-server-mysql e porta in " + NOME_FILE + ".";
        } else {
            causa = "Errore del database.";
        }
        // del messaggio di MySQL basta la prima riga, mandata a capo per non allargare troppo la finestra
        String dettaglio = e.getMessage() == null ? "" : e.getMessage().trim().split("\\R", 2)[0];
        return causa + "\n\nDettaglio: " + aCapo(dettaglio, 80);
    }

    private static String aCapo(String testo, int larghezza) {
        StringBuilder risultato = new StringBuilder();
        int lunghezzaRiga = 0;
        for (String parola : testo.split(" ")) {
            if (lunghezzaRiga > 0 && lunghezzaRiga + 1 + parola.length() > larghezza) {
                risultato.append('\n');
                lunghezzaRiga = 0;
            } else if (lunghezzaRiga > 0) {
                risultato.append(' ');
                lunghezzaRiga++;
            }
            risultato.append(parola);
            lunghezzaRiga += parola.length();
        }
        return risultato.toString();
    }
}
