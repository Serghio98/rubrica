package rubrica.persistenza;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Scanner;
import java.util.Vector;

import rubrica.dominio.Persona;

/**
 * Legge e scrive le persone su file di testo, una persona per riga:
 * <pre>nome;cognome;indirizzo;telefono;eta</pre>
 * Il file è codificato in UTF-8, così i caratteri accentati restano corretti
 * anche spostando il file su un altro computer.
 */
public class GestoreFile {

    public static final String NOME_FILE = "informazioni.txt";
    /** Separatore dei campi: non può comparire nei dati di una persona. */
    public static final String SEPARATORE = ";";

    private static final String CODIFICA = "UTF-8";
    private static final int NUMERO_CAMPI = 5;
    private static final char BOM = '﻿';

    private final File file;

    public GestoreFile(File file) {
        this.file = file;
    }

    public File getFile() {
        return file;
    }

    /**
     * Il file {@value #NOME_FILE} nella cartella che contiene Rubrica.jar.
     * Se il programma non è avviato da un JAR (ad esempio dall'IDE) si usa la
     * cartella di lavoro corrente.
     */
    public static File filePredefinito() {
        try {
            File posizione = new File(GestoreFile.class.getProtectionDomain()
                    .getCodeSource().getLocation().toURI());
            if (posizione.isFile()) {
                return new File(posizione.getParentFile(), NOME_FILE);
            }
        } catch (Exception e) {
            // posizione del JAR non determinabile: si usa la cartella di lavoro
        }
        return new File(NOME_FILE);
    }

    /**
     * Carica le persone dal file. Se il file non esiste restituisce un elenco
     * vuoto; le righe vuote o non valide vengono ignorate.
     */
    public Vector<Persona> carica() throws IOException {
        Vector<Persona> persone = new Vector<>();
        if (!file.exists()) {
            return persone;
        }
        try (Scanner scanner = new Scanner(file, CODIFICA)) {
            boolean primaRiga = true;
            while (scanner.hasNextLine()) {
                String riga = scanner.nextLine();
                if (primaRiga && !riga.isEmpty() && riga.charAt(0) == BOM) {
                    // file salvato come "UTF-8 con BOM", ad esempio dal Blocco note
                    riga = riga.substring(1);
                }
                primaRiga = false;
                Persona persona = daRiga(riga);
                if (persona != null) {
                    persone.add(persona);
                }
            }
            if (scanner.ioException() != null) {
                throw scanner.ioException();
            }
        }
        return persone;
    }

    /** Riscrive il file con tutte le persone dell'elenco. */
    public void salva(List<Persona> persone) throws IOException {
        try (PrintStream out = new PrintStream(file, CODIFICA)) {
            for (Persona persona : persone) {
                out.println(aRiga(persona));
            }
            if (out.checkError()) {
                throw new IOException("Errore durante la scrittura di " + file.getAbsolutePath());
            }
        }
    }

    /** L'età non indicata viene scritta come campo vuoto, ad esempio {@code Mario;;;3480000000;}. */
    private static String aRiga(Persona p) {
        return String.join(SEPARATORE,
                p.getNome(), p.getCognome(), p.getIndirizzo(), p.getTelefono(),
                p.isEtaIndicata() ? String.valueOf(p.getEta()) : "");
    }

    /** Converte una riga del file in una persona, oppure null se la riga non è valida. */
    private static Persona daRiga(String riga) {
        if (riga.trim().isEmpty()) {
            return null;
        }
        String[] campi = riga.split(SEPARATORE, -1);
        if (campi.length != NUMERO_CAMPI) {
            return null;
        }
        try {
            String testoEta = campi[4].trim();
            int eta = testoEta.isEmpty() ? Persona.ETA_NON_INDICATA : Integer.parseInt(testoEta);
            return new Persona(campi[0].trim(), campi[1].trim(), campi[2].trim(), campi[3].trim(), eta);
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
