package rubrica.ui;

import java.awt.Component;
import java.sql.SQLException;

import javax.swing.JOptionPane;

import rubrica.persistenza.ConfigurazioneDatabase;

/** Messaggi di errore comuni alle finestre. */
final class Messaggi {

    private Messaggi() {
    }

    static void erroreDatabase(Component genitore, SQLException e) {
        JOptionPane.showMessageDialog(genitore, ConfigurazioneDatabase.descriviErrore(e),
                "Errore del database", JOptionPane.ERROR_MESSAGE);
    }
}
