package rubrica.logica;

import java.sql.SQLException;

import rubrica.dominio.Utente;
import rubrica.persistenza.GestoreUtenti;

/**
 * Login e registrazione degli utenti, salvati nella tabella {@code utenti}.
 */
public class Autenticazione {

    public static final int LUNGHEZZA_MINIMA_PASSWORD = 6;

    private final GestoreUtenti gestore;

    public Autenticazione(GestoreUtenti gestore) {
        this.gestore = gestore;
    }

    /** False al primo avvio, quando non è ancora registrato nessun utente. */
    public boolean ciSonoUtenti() throws SQLException {
        return gestore.ciSonoUtenti();
    }

    /** L'utente, se username e password sono corretti; altrimenti null. */
    public Utente accedi(String username, char[] password) throws SQLException {
        Utente utente = gestore.cerca(username.trim());
        return utente != null && HashPassword.verifica(password, utente) ? utente : null;
    }

    public boolean esiste(String username) throws SQLException {
        return gestore.cerca(username.trim()) != null;
    }

    /** Il nome utente non può essere vuoto né più lungo della colonna del database. */
    public static boolean usernameValido(String username) {
        String nome = username.trim();
        return !nome.isEmpty() && nome.length() <= Utente.LUNGHEZZA_MASSIMA_USERNAME;
    }

    /**
     * Registra un nuovo utente e lo restituisce con l'id assegnato dal database.
     *
     * @throws IllegalArgumentException se lo username non è valido o è già usato,
     *         oppure se la password è troppo corta
     */
    public Utente registra(String username, char[] password) throws SQLException {
        String nome = username.trim();
        if (!usernameValido(nome) || password.length < LUNGHEZZA_MINIMA_PASSWORD || esiste(nome)) {
            throw new IllegalArgumentException("Username o password non validi");
        }
        return gestore.inserisci(HashPassword.nuovoUtente(nome, password));
    }
}
