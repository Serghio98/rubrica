package rubrica;

import java.io.File;
import java.io.IOException;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import rubrica.logica.Rubrica;
import rubrica.persistenza.GestoreFile;
import rubrica.ui.FinestraPrincipale;

/**
 * Avvio del programma: carica le persone salvate e apre la finestra principale.
 */
public class Main {

    public static void main(String[] args) {
        SwingUtilities.invokeLater(Main::avvia);
    }

    private static void avvia() {
        impostaAspetto();

        File file = GestoreFile.filePredefinito();
        Rubrica rubrica = new Rubrica(new GestoreFile(file));
        try {
            rubrica.carica();
        } catch (IOException e) {
            // Meglio fermarsi: partire con la rubrica vuota sovrascriverebbe il file al primo salvataggio.
            JOptionPane.showMessageDialog(null,
                    "Impossibile leggere il file " + file.getAbsolutePath() + ":\n" + e.getMessage(),
                    "Rubrica", JOptionPane.ERROR_MESSAGE);
            System.exit(1);
            return;
        }

        new FinestraPrincipale(rubrica).setVisible(true);
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
