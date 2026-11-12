package rubrica.ui;

import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Bottone per le toolbar: icona sopra, testo sotto.
 */
final class BottoneToolbar extends JButton {

    BottoneToolbar(String testo, Icona.Tipo icona, ActionListener azione) {
        super(testo, new Icona(icona));
        // Il fuoco resta sulla tabella o sui campi di testo.
        setFocusable(false);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        addActionListener(azione);
    }
}
