package rubrica;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import rubrica.dominio.Utente;
import rubrica.logica.Autenticazione;
import rubrica.logica.HashPassword;

/**
 * Verifica automatica dell'hash delle password e delle regole sul nome utente
 * (eseguita da build.ps1, non serve il database).
 */
public class VerificaHashPassword {

    private static int controlli = 0;

    public static void main(String[] args) {
        // Vettore di prova di RFC 7914 (sezione 11) per PBKDF2-HMAC-SHA256: P="passwd", S="salt", c=1
        byte[] atteso = esadecimale("55ac046e56e3089fec1691c22544b605f94185216dde0465e68b9d57c20dacbc");
        byte[] calcolato = HashPassword.hash("passwd".toCharArray(), "salt".getBytes(StandardCharsets.UTF_8), 1);
        verifica(Arrays.equals(atteso, calcolato), "PBKDF2-HMAC-SHA256 coincide con il vettore di prova di RFC 7914");

        Utente a = HashPassword.nuovoUtente("mario", "segreta1".toCharArray());
        Utente b = HashPassword.nuovoUtente("luigi", "segreta1".toCharArray());
        verifica(a.getId() == Utente.NUOVO, "utente non ancora salvato");
        verifica(HashPassword.verifica("segreta1".toCharArray(), a), "password giusta accettata");
        verifica(!HashPassword.verifica("segreta2".toCharArray(), a), "password sbagliata rifiutata");
        verifica(!HashPassword.verifica(new char[0], a), "password vuota rifiutata");
        verifica(!Arrays.equals(a.getSalt(), b.getSalt()), "salt diverso per ogni utente");
        verifica(!Arrays.equals(a.getHashPassword(), b.getHashPassword()), "stessa password, hash diversi");
        verifica(a.getIterazioni() == HashPassword.ITERAZIONI, "numero di iterazioni memorizzato");
        verifica(a.getSalt().length == 16 && a.getHashPassword().length == 32, "salt di 16 byte, hash di 32 byte");

        verifica(Autenticazione.usernameValido("mario"), "username valido");
        verifica(!Autenticazione.usernameValido("   "), "username vuoto");
        verifica(Autenticazione.usernameValido(ripeti('x', Utente.LUNGHEZZA_MASSIMA_USERNAME)), "username di 50 caratteri");
        verifica(!Autenticazione.usernameValido(ripeti('x', Utente.LUNGHEZZA_MASSIMA_USERNAME + 1)), "username troppo lungo");

        System.out.println("Verifica hash password: " + controlli + " controlli superati.");
    }

    public static String ripeti(char c, int volte) {
        char[] caratteri = new char[volte];
        Arrays.fill(caratteri, c);
        return new String(caratteri);
    }

    private static byte[] esadecimale(String testo) {
        byte[] r = new byte[testo.length() / 2];
        for (int i = 0; i < r.length; i++) {
            r[i] = (byte) Integer.parseInt(testo.substring(2 * i, 2 * i + 2), 16);
        }
        return r;
    }

    private static void verifica(boolean condizione, String descrizione) {
        controlli++;
        if (!condizione) {
            throw new AssertionError("FALLITO: " + descrizione);
        }
    }
}
