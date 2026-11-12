package rubrica.dominio;

import java.util.regex.Pattern;

/**
 * Un contatto della rubrica.
 */
public class Persona {

    /** Età ammesse, estremi inclusi. */
    public static final int ETA_MINIMA = 1;
    public static final int ETA_MASSIMA = 150;
    /** Valore di {@code eta} quando l'età non è indicata: l'età è facoltativa. */
    public static final int ETA_NON_INDICATA = 0;

    /** Lunghezze massime dei campi, come le colonne della tabella persone. */
    public static final int LUNGHEZZA_MASSIMA_NOME = 100;
    public static final int LUNGHEZZA_MASSIMA_COGNOME = 100;
    public static final int LUNGHEZZA_MASSIMA_INDIRIZZO = 255;
    public static final int LUNGHEZZA_MASSIMA_TELEFONO = 50;

    /** Valore di {@code id} per una persona non ancora salvata nel database. */
    public static final int NUOVA = 0;

    /**
     * Caratteri ammessi nel telefono: cifre, spazi e i separatori usati nei vari
     * paesi ( ) - . / ; il + solo come primo carattere (prefisso internazionale).
     * Esempi: +39 348 000 0000, (212) 555-1234, 030/123456, 01.23.45.67.89
     */
    private static final Pattern CARATTERI_TELEFONO = Pattern.compile("\\+?[0-9 ()./-]*");
    private static final int CIFRE_MINIME_TELEFONO = 3;

    /** Chiave nel database, {@link #NUOVA} finché la persona non è salvata. */
    private int id = NUOVA;
    private String nome;
    private String cognome;
    private String indirizzo;
    private String telefono;
    private int eta;

    public Persona(String nome, String cognome, String indirizzo, String telefono, int eta) {
        this.nome = nome;
        this.cognome = cognome;
        this.indirizzo = indirizzo;
        this.telefono = telefono;
        this.eta = eta;
    }

    public static boolean etaValida(int eta) {
        return eta >= ETA_MINIMA && eta <= ETA_MASSIMA;
    }

    /** True se il testo usa solo i caratteri ammessi per un telefono, anche se il numero è incompleto. */
    public static boolean caratteriTelefonoAmmessi(String telefono) {
        return CARATTERI_TELEFONO.matcher(telefono).matches();
    }

    /** Il telefono è obbligatorio: deve usare solo i caratteri ammessi e avere almeno 3 cifre. */
    public static boolean telefonoValido(String telefono) {
        int cifre = 0;
        for (char c : telefono.toCharArray()) {
            if (c >= '0' && c <= '9') {
                cifre++;
            }
        }
        return caratteriTelefonoAmmessi(telefono) && cifre >= CIFRE_MINIME_TELEFONO;
    }

    /** Sostituisce i dati di questa persona con quelli di {@code altra}; l'id non cambia. */
    public void copiaDa(Persona altra) {
        this.nome = altra.nome;
        this.cognome = altra.cognome;
        this.indirizzo = altra.indirizzo;
        this.telefono = altra.telefono;
        this.eta = altra.eta;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public String getCognome() {
        return cognome;
    }

    public String getIndirizzo() {
        return indirizzo;
    }

    public String getTelefono() {
        return telefono;
    }

    public int getEta() {
        return eta;
    }

    public boolean isEtaIndicata() {
        return eta != ETA_NON_INDICATA;
    }

    /** Nome e cognome separati da uno spazio; solo uno dei due se l'altro manca. */
    public String getNomeCompleto() {
        return (nome + " " + cognome).trim();
    }
}
