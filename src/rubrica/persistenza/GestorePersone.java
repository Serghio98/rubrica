package rubrica.persistenza;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.Vector;

import rubrica.dominio.Persona;

/**
 * Lettura e scrittura delle persone nella tabella {@code persone} tramite JDBC.
 * <p>
 * Ogni operazione riceve l'id dell'utente e lo usa nella clausola WHERE:
 * un utente non può leggere, modificare o eliminare i contatti di un altro.
 */
public class GestorePersone {

    private final ConfigurazioneDatabase database;

    public GestorePersone(ConfigurazioneDatabase database) {
        this.database = database;
    }

    /** Le persone dell'utente, nell'ordine in cui sono state inserite. */
    public Vector<Persona> carica(int idUtente) throws SQLException {
        String sql = "SELECT id, nome, cognome, indirizzo, telefono, eta FROM persone WHERE id_utente = ? ORDER BY id";
        Vector<Persona> persone = new Vector<>();
        try (Connection connessione = database.apriConnessione();
                PreparedStatement query = connessione.prepareStatement(sql)) {
            query.setInt(1, idUtente);
            try (ResultSet risultato = query.executeQuery()) {
                while (risultato.next()) {
                    int eta = risultato.getInt("eta");
                    if (risultato.wasNull()) {
                        eta = Persona.ETA_NON_INDICATA;
                    }
                    Persona persona = new Persona(risultato.getString("nome"), risultato.getString("cognome"),
                            risultato.getString("indirizzo"), risultato.getString("telefono"), eta);
                    persona.setId(risultato.getInt("id"));
                    persone.add(persona);
                }
            }
        }
        return persone;
    }

    /** Salva una nuova persona dell'utente e le assegna l'id generato dal database. */
    public void inserisci(int idUtente, Persona persona) throws SQLException {
        String sql = "INSERT INTO persone (nome, cognome, indirizzo, telefono, eta, id_utente) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connessione = database.apriConnessione();
                PreparedStatement insert = connessione.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            impostaDati(insert, persona);
            insert.setInt(6, idUtente);
            insert.executeUpdate();
            persona.setId(GestoreUtenti.chiaveGenerata(insert));
        }
    }

    /** Aggiorna i dati di una persona dell'utente (riconosciuta dal suo id). */
    public void aggiorna(int idUtente, Persona persona) throws SQLException {
        String sql = "UPDATE persone SET nome = ?, cognome = ?, indirizzo = ?, telefono = ?, eta = ?"
                + " WHERE id = ? AND id_utente = ?";
        try (Connection connessione = database.apriConnessione();
                PreparedStatement update = connessione.prepareStatement(sql)) {
            impostaDati(update, persona);
            update.setInt(6, persona.getId());
            update.setInt(7, idUtente);
            if (update.executeUpdate() != 1) {
                throw new SQLException("Persona non trovata nel database: forse è stata eliminata");
            }
        }
    }

    /** Elimina una persona dell'utente; se non esiste (o è di un altro utente) non succede nulla. */
    public void elimina(int idUtente, Persona persona) throws SQLException {
        String sql = "DELETE FROM persone WHERE id = ? AND id_utente = ?";
        try (Connection connessione = database.apriConnessione();
                PreparedStatement delete = connessione.prepareStatement(sql)) {
            delete.setInt(1, persona.getId());
            delete.setInt(2, idUtente);
            delete.executeUpdate();
        }
    }

    /** Parametri 1-5: nome, cognome, indirizzo, telefono, età (NULL se non indicata). */
    private static void impostaDati(PreparedStatement statement, Persona persona) throws SQLException {
        statement.setString(1, persona.getNome());
        statement.setString(2, persona.getCognome());
        statement.setString(3, persona.getIndirizzo());
        statement.setString(4, persona.getTelefono());
        if (persona.isEtaIndicata()) {
            statement.setInt(5, persona.getEta());
        } else {
            statement.setNull(5, Types.TINYINT);
        }
    }
}
