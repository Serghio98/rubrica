package rubrica;

import java.io.File;
import java.io.IOException;
import java.sql.SQLException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rubrica.dominio.Utente;
import rubrica.logica.Autenticazione;
import rubrica.logica.Rubrica;
import rubrica.persistenza.ConfigurazioneDatabase;
import rubrica.persistenza.GestorePersone;
import rubrica.persistenza.GestoreUtenti;
import rubrica.ui.FinestraLogin;
import rubrica.ui.FinestraPrincipale;

/**
 * Avvio del programma: legge le credenziali del database, mostra il login e,
 * se riesce, carica i contatti dell'utente e apre la finestra principale.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::avvia);
    }

    private static void avvia() {
        impostaAspetto();

        ConfigurazioneDatabase database = leggiCredenziali();
        if (database == null) {
            return;
        }
        Autenticazione autenticazione = new Autenticazione(new GestoreUtenti(database));
        boolean primoAvvio;
        try {
            // è anche la prima connessione: se il database non è raggiungibile ci si ferma qui
            primoAvvio = !autenticazione.ciSonoUtenti();
        } catch (SQLException e) {
            terminaConErrore(database, e);
            return;
        }

        mostraLogin(database, autenticazione, primoAvvio);
    }

    /** La finestra di login: all'avvio e dopo ogni logout. */
    private static void mostraLogin(ConfigurazioneDatabase database, Autenticazione autenticazione, boolean primoAvvio) {
        new FinestraLogin(autenticazione, primoAvvio,
                utente -> apriRubrica(database, autenticazione, utente)).setVisible(true);
    }

    /** Le credenziali da credenziali_database.properties, oppure null (dopo aver avvisato) se il file non va. */
    private static ConfigurazioneDatabase leggiCredenziali() {
        File file = ConfigurazioneDatabase.filePredefinito();
        try {
            return ConfigurazioneDatabase.leggi(file);
        } catch (IOException e) {
            termina("Impossibile leggere le credenziali del database dal file\n"
                    + file.getAbsolutePath() + "\n\n" + e.getMessage());
            return null;
        }
    }

    /** Dopo il login: carica i contatti dell'utente e apre la finestra principale. */
    private static void apriRubrica(ConfigurazioneDatabase database, Autenticazione autenticazione, Utente utente) {
        Rubrica rubrica = new Rubrica(new GestorePersone(database), utente);
        try {
            rubrica.carica();
        } catch (SQLException e) {
            terminaConErrore(database, e);
            return;
        }

        // il logout riporta al login; esiste già almeno un utente, quindi non è un primo avvio
        new FinestraPrincipale(rubrica, () -> mostraLogin(database, autenticazione, false)).setVisible(true);
    }

    private static void terminaConErrore(ConfigurazioneDatabase database, SQLException e) {
        termina("Impossibile usare il database " + database.descrizione() + ".\n"
                + ConfigurazioneDatabase.descriviErrore(e));
    }

    private static void termina(String messaggio) {
        JOptionPane.showMessageDialog(null, messaggio, "Rubrica", JOptionPane.ERROR_MESSAGE);
        System.exit(1);
    }

    /** Aspetto del sistema operativo e testi dei bottoni dei messaggi in italiano. */
    private static void impostaAspetto() {
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // si resta con l'aspetto predefinito di Swing
        }
        UIManager.put("OptionPane.yesButtonText", "Sì");
        UIManager.put("OptionPane.noButtonText", "No");
        UIManager.put("OptionPane.okButtonText", "OK");
        UIManager.put("OptionPane.cancelButtonText", "Annulla");
    }
}
