package rubrica.persistenza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import rubrica.dominio.Utente;

/**
 * Lettura e scrittura degli utenti nella tabella {@code utenti} tramite JDBC.
 */
public class GestoreUtenti {

    private final ConfigurazioneDatabase database;

    public GestoreUtenti(ConfigurazioneDatabase database) {
        this.database = database;
    }

    /** False al primo avvio, quando non è ancora registrato nessun utente. */
    public boolean ciSonoUtenti() throws SQLException {
        try (Connection connessione = database.apriConnessione();
                Statement statement = connessione.createStatement();
                ResultSet risultato = statement.executeQuery("SELECT 1 FROM utenti LIMIT 1")) {
            return risultato.next();
        }
    }

    /** L'utente con questo username (maiuscole e minuscole contano), oppure null. */
    public Utente cerca(String username) throws SQLException {
        String sql = "SELECT id, username, iterazioni, salt, hash FROM utenti WHERE username = ?";
        try (Connection connessione = database.apriConnessione();
                PreparedStatement query = connessione.prepareStatement(sql)) {
            query.setString(1, username);
            try (ResultSet risultato = query.executeQuery()) {
                if (!risultato.next()) {
                    return null;
                }
                return new Utente(risultato.getInt("id"), risultato.getString("username"),
                        risultato.getInt("iterazioni"), risultato.getBytes("salt"), risultato.getBytes("hash"));
            }
        }
    }

    /** Salva un nuovo utente e lo restituisce con l'id assegnato dal database. */
    public Utente inserisci(Utente utente) throws SQLException {
        String sql = "INSERT INTO utenti (username, iterazioni, salt, hash) VALUES (?, ?, ?, ?)";
        try (Connection connessione = database.apriConnessione();
                PreparedStatement insert = connessione.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            insert.setString(1, utente.getUsername());
            insert.setInt(2, utente.getIterazioni());
            insert.setBytes(3, utente.getSalt());
            insert.setBytes(4, utente.getHashPassword());
            insert.executeUpdate();
            return utente.conId(chiaveGenerata(insert));
        }
    }

    static int chiaveGenerata(Statement statement) throws SQLException {
        try (ResultSet chiavi = statement.getGeneratedKeys()) {
            if (!chiavi.next()) {
                throw new SQLException("Il database non ha restituito l'id del nuovo record");
            }
            return chiavi.getInt(1);
        }
    }
}
