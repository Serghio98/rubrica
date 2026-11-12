package rubrica.logica;

import java.security.GeneralSecurityException;
import java.security.MessageDigest;
import java.security.SecureRandom;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;

import rubrica.dominio.Utente;

/**
 * Hash delle password con PBKDF2-HMAC-SHA256 (standard PKCS #5, RFC 8018),
 * incluso in Java senza librerie esterne.
 * <p>
 * Ogni utente ha un salt casuale, quindi due password uguali producono hash
 * diversi; le molte iterazioni rendono lenti i tentativi di indovinare la
 * password partendo dall'hash.
 */
public final class HashPassword {

    public static final String ALGORITMO = "PBKDF2WithHmacSHA256";
    /** Valore raccomandato da OWASP per PBKDF2-HMAC-SHA256. */
    public static final int ITERAZIONI = 600_000;

    private static final int BYTE_SALT = 16;
    private static final int BIT_HASH = 256;
    private static final SecureRandom CASUALE = new SecureRandom();

    private HashPassword() {
    }

    /** Crea un utente (non ancora salvato) con un salt nuovo e l'hash della password indicata. */
    public static Utente nuovoUtente(String username, char[] password) {
        byte[] salt = new byte[BYTE_SALT];
        CASUALE.nextBytes(salt);
        return new Utente(Utente.NUOVO, username, ITERAZIONI, salt, hash(password, salt, ITERAZIONI));
    }

    /** True se la password corrisponde all'hash memorizzato per l'utente. */
    public static boolean verifica(char[] password, Utente utente) {
        if (password.length == 0) {
            return false;
        }
        byte[] calcolato = hash(password, utente.getSalt(), utente.getIterazioni());
        // confronto a tempo costante
        return MessageDigest.isEqual(calcolato, utente.getHashPassword());
    }

    public static byte[] hash(char[] password, byte[] salt, int iterazioni) {
        PBEKeySpec spec = new PBEKeySpec(password, salt, iterazioni, BIT_HASH);
        try {
            return SecretKeyFactory.getInstance(ALGORITMO).generateSecret(spec).getEncoded();
        } catch (GeneralSecurityException e) {
            throw new IllegalStateException("Algoritmo " + ALGORITMO + " non disponibile", e);
        } finally {
            spec.clearPassword();
        }
    }
}
