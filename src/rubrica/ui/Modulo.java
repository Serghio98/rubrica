package rubrica.ui;

import java.awt.GridBagConstraints;
import java.awt.Insets;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;

/**
 * Costruzione dei moduli "nomeCampo - valoreCampo" su griglia, usati
 * dall'editor persona e dalla finestra di login.
 */
final class Modulo {

    private Modulo() {
    }

    /** Una riga con l'etichetta a sinistra e il campo, che si allarga, a destra. */
    static void aggiungiRiga(JPanel modulo, int riga, String etichetta, JComponent campo) {
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = riga;
        c.insets = new Insets(4, 4, 4, 4);
        c.anchor = GridBagConstraints.LINE_START;

        JLabel label = new JLabel(etichetta);
        label.setLabelFor(campo);
        c.gridx = 0;
        modulo.add(label, c);

        c.gridx = 1;
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        modulo.add(campo, c);
    }

    /** Un testo su tutta la larghezza del modulo, in grigio se è una nota a piè di modulo. */
    static void aggiungiTesto(JPanel modulo, int riga, String testo, boolean nota) {
        JLabel label = new JLabel(testo);
        if (nota) {
            label.setForeground(UIManager.getColor("Label.disabledForeground"));
        }
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = riga;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = nota ? new Insets(8, 4, 0, 4) : new Insets(0, 4, 8, 4);
        c.anchor = GridBagConstraints.LINE_START;
        modulo.add(label, c);
    }
}
