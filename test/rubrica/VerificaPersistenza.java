package rubrica;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.List;

import rubrica.dominio.Persona;
import rubrica.logica.Rubrica;
import rubrica.persistenza.GestoreFile;

/**
 * Verifica automatica dello strato di persistenza (eseguita da build.ps1).
 * Lavora in una cartella temporanea, senza toccare il vero informazioni.txt.
 * Termina con codice di uscita diverso da 0 se un controllo fallisce.
 */
public class VerificaPersistenza {

    private static int controlli = 0;

    public static void main(String[] args) throws IOException {
        File cartella = Files.createTempDirectory("rubrica-verifica").toFile();
        try {
            fileMancante(cartella);
            salvaERicarica(cartella);
            eliminaEModifica(cartella);
            righeNonValide(cartella);
            System.out.println("Verifica persistenza: " + controlli + " controlli superati.");
        } finally {
            File[] files = cartella.listFiles();
            if (files != null) {
                for (File f : files) {
                    f.delete();
                }
            }
            cartella.delete();
        }
    }

    /** Se il file non esiste la rubrica parte vuota, senza errori e senza creare il file. */
    private static void fileMancante(File cartella) throws IOException {
        File file = new File(cartella, "mancante.txt");
        Rubrica rubrica = apri(file);
        verifica(rubrica.getNumeroPersone() == 0, "file mancante: rubrica vuota");
        verifica(!file.exists(), "file mancante: il caricamento non crea il file");
    }

    /** Ogni aggiunta riscrive il file; riaprendolo si ritrovano gli stessi dati. */
    private static void salvaERicarica(File cartella) throws IOException {
        File file = new File(cartella, "salva.txt");
        Rubrica rubrica = apri(file);
        rubrica.aggiungi(new Persona("Steve", "Jobs", "via Cupertino 13", "0612344", 56));
        verificaRighe(file, "Steve;Jobs;via Cupertino 13;0612344;56");

        rubrica.aggiungi(new Persona("Bill", "Gates", "via Redmond 10", "06688989", 60));
        rubrica.aggiungi(new Persona("Niccolò", "Citrò", "", "333 1234567", Persona.ETA_NON_INDICATA));
        rubrica.aggiungi(new Persona("", "Rossi", "", "+39 06 7654321", 40));
        verificaRighe(file,
                "Steve;Jobs;via Cupertino 13;0612344;56",
                "Bill;Gates;via Redmond 10;06688989;60",
                "Niccolò;Citrò;;333 1234567;",
                ";Rossi;;+39 06 7654321;40");

        verificaUguali(rubrica, apri(file));
    }

    /** Caso di test 3 della traccia: elimino la persona di mezzo e modifico l'ultima. */
    private static void eliminaEModifica(File cartella) throws IOException {
        File file = new File(cartella, "modifica.txt");
        Rubrica rubrica = apri(file);
        rubrica.aggiungi(new Persona("Anna", "Bianchi", "via Roma 1", "111", 30));
        rubrica.aggiungi(new Persona("Bruno", "Verdi", "via Milano 2", "222", 40));
        rubrica.aggiungi(new Persona("Carla", "Neri", "via Napoli 3", "333", 50));

        rubrica.elimina(1);
        rubrica.modifica(1, new Persona("Carla", "Neri", "piazza Duomo 4", "444", 51));

        verificaRighe(file,
                "Anna;Bianchi;via Roma 1;111;30",
                "Carla;Neri;piazza Duomo 4;444;51");
        verificaUguali(rubrica, apri(file));
    }

    /** L'esempio della traccia si carica; righe vuote o malformate vengono ignorate. */
    private static void righeNonValide(File cartella) throws IOException {
        File file = new File(cartella, "righe.txt");
        String contenuto = "﻿"
                + "Steve;Jobs;via Cupertino 13;0612344;56\r\n"
                + "\r\n"
                + "Bill;Gates;via Redmond 10;06688989;60\n"
                + "solo;quattro;campi;123\n"
                + "eta;non;numerica;123;abc\n"
                + "troppi;campi;a;b;1;2\n"
                + "   \n"
                + "Babbo;Natale;via del Polo Nord;00000111;99\n"
                + "Senza;Eta;;123;\n"
                + ";SoloCognome;;456;0\n";
        Files.write(file.toPath(), contenuto.getBytes(StandardCharsets.UTF_8));

        Rubrica rubrica = apri(file);
        verifica(rubrica.getNumeroPersone() == 5, "righe non valide: caricate 5 persone su 10 righe");
        verificaPersona(rubrica.getPersona(0), "Steve", "Jobs", "via Cupertino 13", "0612344", 56);
        verificaPersona(rubrica.getPersona(1), "Bill", "Gates", "via Redmond 10", "06688989", 60);
        verificaPersona(rubrica.getPersona(2), "Babbo", "Natale", "via del Polo Nord", "00000111", 99);
        verificaPersona(rubrica.getPersona(3), "Senza", "Eta", "", "123", Persona.ETA_NON_INDICATA);
        verificaPersona(rubrica.getPersona(4), "", "SoloCognome", "", "456", Persona.ETA_NON_INDICATA);
    }

    private static Rubrica apri(File file) throws IOException {
        Rubrica rubrica = new Rubrica(new GestoreFile(file));
        rubrica.carica();
        return rubrica;
    }

    private static void verificaRighe(File file, String... attese) throws IOException {
        List<String> righe = Files.readAllLines(file.toPath(), StandardCharsets.UTF_8);
        verifica(righe.equals(Arrays.asList(attese)),
                "contenuto di " + file.getName() + ": atteso " + Arrays.asList(attese) + ", trovato " + righe);
    }

    private static void verificaUguali(Rubrica attesa, Rubrica trovata) {
        verifica(attesa.getNumeroPersone() == trovata.getNumeroPersone(), "stesso numero di persone dopo la riapertura");
        for (int i = 0; i < attesa.getNumeroPersone(); i++) {
            Persona p = attesa.getPersona(i);
            verificaPersona(trovata.getPersona(i), p.getNome(), p.getCognome(), p.getIndirizzo(), p.getTelefono(), p.getEta());
        }
    }

    private static void verificaPersona(Persona p, String nome, String cognome, String indirizzo, String telefono, int eta) {
        boolean uguale = p.getNome().equals(nome) && p.getCognome().equals(cognome)
                && p.getIndirizzo().equals(indirizzo) && p.getTelefono().equals(telefono) && p.getEta() == eta;
        verifica(uguale, "persona attesa " + nome + ";" + cognome + ";" + indirizzo + ";" + telefono + ";" + eta
                + ", trovata " + p.getNome() + ";" + p.getCognome() + ";" + p.getIndirizzo() + ";"
                + p.getTelefono() + ";" + p.getEta());
    }

    private static void verifica(boolean condizione, String descrizione) {
        controlli++;
        if (!condizione) {
            throw new AssertionError("FALLITO: " + descrizione);
        }
    }
}
