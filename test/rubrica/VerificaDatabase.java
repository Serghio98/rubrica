package rubrica;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rubrica.dominio.Persona;
import rubrica.dominio.Utente;
import rubrica.logica.Autenticazione;
import rubrica.logica.HashPassword;
import rubrica.logica.Rubrica;
import rubrica.persistenza.ConfigurazioneDatabase;
import rubrica.persistenza.GestorePersone;
import rubrica.persistenza.GestoreUtenti;

/**
 * Verifica del codice del database su un server MySQL vero
 * (eseguita da {@code build.ps1 -VerificaDatabase <credenziali>}).
 * <p>
 * Crea il database di prova {@value #DATABASE_PROVA} con lo script
 * schema_database.sql (cambiando solo il nome del database), lo usa e alla
 * fine lo elimina. Argomenti: file delle credenziali, file dello script.
 */
public class VerificaDatabase {

    private static final String DATABASE_PROVA = "rubrica_verifica";

    private static int controlli = 0;

    public static void main(String[] args) throws Exception {
        ConfigurazioneDatabase server = ConfigurazioneDatabase.leggi(new File(args[0])).conDatabase("");
        ConfigurazioneDatabase database = server.conDatabase(DATABASE_PROVA);
        creaDatabase(server, new File(args[1]), DATABASE_PROVA);
        try {
            utenti(database);
            contatti(database);
            separazioneTraUtenti(database);
            System.out.println("Verifica database (" + database.descrizione() + "): " + controlli + " controlli superati.");
        } finally {
            esegui(server, "DROP DATABASE IF EXISTS " + DATABASE_PROVA);
        }
    }

    private static void utenti(ConfigurazioneDatabase database) throws SQLException {
        Autenticazione autenticazione = new Autenticazione(new GestoreUtenti(database));
        verifica(!autenticazione.ciSonoUtenti(), "database nuovo: nessun utente (primo avvio)");

        Utente mario = autenticazione.registra("  mario  ", "segreta1".toCharArray());
        verifica(mario.getId() > 0 && mario.getUsername().equals("mario"), "registrazione: id assegnato dal database");
        verifica(autenticazione.ciSonoUtenti(), "dopo la registrazione ci sono utenti");

        Utente entrato = autenticazione.accedi("mario", "segreta1".toCharArray());
        verifica(entrato != null && entrato.getId() == mario.getId(), "login: restituisce l'utente registrato");
        verifica(autenticazione.accedi(" mario ", "segreta1".toCharArray()) != null, "spazi attorno allo username ignorati");
        verifica(autenticazione.accedi("mario", "Segreta1".toCharArray()) == null, "password con maiuscole diverse rifiutata");
        verifica(autenticazione.accedi("Mario", "segreta1".toCharArray()) == null, "username con maiuscole diverse rifiutato");
        verifica(autenticazione.accedi("nessuno", "segreta1".toCharArray()) == null, "utente inesistente rifiutato");
        verifica(autenticazione.esiste("mario") && !autenticazione.esiste("Mario"), "esiste: maiuscole e minuscole contano");

        registrazioneRifiutata(autenticazione, "mario", "altrapassword", "username già usato");
        registrazioneRifiutata(autenticazione, "   ", "segreta1", "username vuoto");
        registrazioneRifiutata(autenticazione, VerificaHashPassword.ripeti('x', 51), "segreta1", "username troppo lungo");
        registrazioneRifiutata(autenticazione, "luigi", "corta", "password troppo corta");
        Utente marioMaiuscolo = autenticazione.registra("Mario", "password3".toCharArray());
        verifica(marioMaiuscolo.getId() != mario.getId(), "Mario e mario sono utenti diversi");

        try (Connection connessione = database.apriConnessione();
                Statement statement = connessione.createStatement();
                ResultSet riga = statement.executeQuery(
                        "SELECT iterazioni, LENGTH(salt), LENGTH(hash) FROM utenti WHERE username = 'mario'")) {
            verifica(riga.next() && riga.getInt(1) == HashPassword.ITERAZIONI && riga.getInt(2) == 16 && riga.getInt(3) == 32,
                    "tabella utenti: iterazioni, salt di 16 byte e hash di 32 byte");
        }
    }

    private static void contatti(ConfigurazioneDatabase database) throws SQLException {
        Utente anna = new Autenticazione(new GestoreUtenti(database)).registra("anna", "password1".toCharArray());
        GestorePersone gestore = new GestorePersone(database);
        Rubrica rubrica = apri(gestore, anna);
        verifica(rubrica.getNumeroPersone() == 0, "nuovo utente: rubrica vuota");

        rubrica.aggiungi(new Persona("Steve", "Jobs", "via Cupertino 13", "0612344", 56));
        rubrica.aggiungi(new Persona("Niccolò", "Citrò", "", "333 1234567", Persona.ETA_NON_INDICATA));
        rubrica.aggiungi(new Persona("", "Rossi", "via; col punto e virgola", "+39 06 7654321", 40));
        verifica(rubrica.getPersona(0).getId() > 0 && rubrica.getPersona(2).getId() > rubrica.getPersona(0).getId(),
                "id assegnati dal database in ordine di inserimento");
        verificaPersone(apri(gestore, anna),
                "Steve;Jobs;via Cupertino 13;0612344;56",
                "Niccolò;Citrò;;333 1234567;",
                ";Rossi;via; col punto e virgola;+39 06 7654321;40");
        verifica(conta(database, "SELECT COUNT(*) FROM persone WHERE eta IS NULL AND id_utente = " + anna.getId()) == 1,
                "età non indicata salvata come NULL");

        // Caso di test 3 della traccia: elimino la persona di mezzo e modifico l'ultima
        int idUltima = rubrica.getPersona(2).getId();
        rubrica.elimina(1);
        rubrica.modifica(1, new Persona("", "Rossi", "piazza Duomo 4", "444", 41));
        Rubrica riaperta = apri(gestore, anna);
        verificaPersone(riaperta, "Steve;Jobs;via Cupertino 13;0612344;56", ";Rossi;piazza Duomo 4;444;41");
        verifica(riaperta.getPersona(1).getId() == idUltima, "la modifica mantiene lo stesso record");
    }

    private static void separazioneTraUtenti(ConfigurazioneDatabase database) throws SQLException {
        Autenticazione autenticazione = new Autenticazione(new GestoreUtenti(database));
        Utente paolo = autenticazione.registra("paolo", "password1".toCharArray());
        Utente sara = autenticazione.registra("sara", "password2".toCharArray());
        GestorePersone gestore = new GestorePersone(database);

        Rubrica diPaolo = apri(gestore, paolo);
        diPaolo.aggiungi(new Persona("Contatto", "DiPaolo", "", "111", 30));
        Rubrica diSara = apri(gestore, sara);
        verifica(diSara.getNumeroPersone() == 0, "sara non vede i contatti di paolo");
        diSara.aggiungi(new Persona("Contatto", "DiSara", "", "222", 40));
        verificaPersone(apri(gestore, paolo), "Contatto;DiPaolo;;111;30");
        verificaPersone(apri(gestore, sara), "Contatto;DiSara;;222;40");

        // Anche conoscendo l'id, un utente non può modificare né eliminare i contatti di un altro
        Persona contattoDiPaolo = new Persona("Modificato", "DaSara", "", "999", 1);
        contattoDiPaolo.setId(diPaolo.getPersona(0).getId());
        try {
            gestore.aggiorna(sara.getId(), contattoDiPaolo);
            verifica(false, "sara ha modificato un contatto di paolo");
        } catch (SQLException e) {
            verifica(true, "modifica di un contatto altrui rifiutata");
        }
        gestore.elimina(sara.getId(), contattoDiPaolo);
        verificaPersone(apri(gestore, paolo), "Contatto;DiPaolo;;111;30");
    }

    // ---- creazione del database di prova ----

    /** Esegue lo script di creazione cambiando solo il nome del database. */
    public static void creaDatabase(ConfigurazioneDatabase server, File schema, String nome) throws Exception {
        esegui(server, "DROP DATABASE IF EXISTS " + nome);
        String script = new String(Files.readAllBytes(schema.toPath()), StandardCharsets.UTF_8);
        try (Connection connessione = server.apriConnessione(); Statement statement = connessione.createStatement()) {
            for (String istruzione : istruzioni(script)) {
                if (istruzione.startsWith("CREATE DATABASE") || istruzione.startsWith("USE")) {
                    istruzione = istruzione.replaceAll("\\brubrica\\b", nome);
                }
                statement.execute(istruzione);
            }
        }
    }

    /** Le istruzioni dello script separate da ";", senza i commenti "--". */
    static List<String> istruzioni(String script) {
        StringBuilder senzaCommenti = new StringBuilder();
        for (String riga : script.split("\\R")) {
            int commento = riga.indexOf("--");
            senzaCommenti.append(commento >= 0 ? riga.substring(0, commento) : riga).append('\n');
        }
        List<String> istruzioni = new ArrayList<>();
        for (String istruzione : senzaCommenti.toString().split(";")) {
            if (!istruzione.trim().isEmpty()) {
                istruzioni.add(istruzione.trim());
            }
        }
        return istruzioni;
    }

    public static void esegui(ConfigurazioneDatabase database, String sql) throws SQLException {
        try (Connection connessione = database.apriConnessione(); Statement statement = connessione.createStatement()) {
            statement.execute(sql);
        }
    }

    // ---- aiuti ----

    private static Rubrica apri(GestorePersone gestore, Utente utente) throws SQLException {
        Rubrica rubrica = new Rubrica(gestore, utente);
        rubrica.carica();
        return rubrica;
    }

    private static int conta(ConfigurazioneDatabase database, String sql) throws SQLException {
        try (Connection connessione = database.apriConnessione();
                Statement statement = connessione.createStatement();
                ResultSet risultato = statement.executeQuery(sql)) {
            risultato.next();
            return risultato.getInt(1);
        }
    }

    /** Confronta le persone con righe "nome;cognome;indirizzo;telefono;eta" (età vuota se non indicata). */
    private static void verificaPersone(Rubrica rubrica, String... attese) {
        List<String> trovate = new ArrayList<>();
        for (int i = 0; i < rubrica.getNumeroPersone(); i++) {
            Persona p = rubrica.getPersona(i);
            trovate.add(p.getNome() + ";" + p.getCognome() + ";" + p.getIndirizzo() + ";" + p.getTelefono() + ";"
                    + (p.isEtaIndicata() ? String.valueOf(p.getEta()) : ""));
        }
        verifica(trovate.equals(Arrays.asList(attese)), "attese " + Arrays.asList(attese) + ", trovate " + trovate);
    }

    private static void registrazioneRifiutata(Autenticazione a, String username, String password, String caso)
            throws SQLException {
        try {
            a.registra(username, password.toCharArray());
            verifica(false, "registrazione accettata: " + caso);
        } catch (IllegalArgumentException e) {
            verifica(true, caso);
        }
    }

    private static void verifica(boolean condizione, String descrizione) {
        controlli++;
        if (!condizione) {
            throw new AssertionError("FALLITO: " + descrizione);
        }
    }
}
