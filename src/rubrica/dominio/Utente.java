package rubrica.dominio;

/**
 * Un utente che può accedere alla rubrica; ogni utente vede solo i propri contatti.
 * <p>
 * La password non viene mai memorizzata: si conservano solo il suo hash
 * PBKDF2, il salt casuale e il numero di iterazioni usati per calcolarlo.
 */
public class Utente {

    /** Valore di {@code id} per un utente non ancora salvato nel database. */
    public static final int NUOVO = 0;
    public static final int LUNGHEZZA_MASSIMA_USERNAME = 50;

    private final int id;
    private final String username;
    private final int iterazioni;
    private final byte[] salt;
    private final byte[] hashPassword;

    public Utente(int id, String username, int iterazioni, byte[] salt, byte[] hashPassword) {
        this.id = id;
        this.username = username;
        this.iterazioni = iterazioni;
        this.salt = salt.clone();
        this.hashPassword = hashPassword.clone();
    }

    /** Lo stesso utente con l'id assegnato dal database. */
    public Utente conId(int nuovoId) {
        return new Utente(nuovoId, username, iterazioni, salt, hashPassword);
    }

    public int getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public int getIterazioni() {
        return iterazioni;
    }

    public byte[] getSalt() {
        return salt.clone();
    }

    public byte[] getHashPassword() {
        return hashPassword.clone();
    }

    @Override
    public String toString() {
        return username;
    }
}
