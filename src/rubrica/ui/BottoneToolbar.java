package rubrica.ui;

import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;

import javax.swing.JButton;
import javax.swing.SwingConstants;

/**
 * Bottone per le toolbar: icona sopra, testo sotto, scorciatoia Alt+lettera.
 */
final class BottoneToolbar extends JButton {

    BottoneToolbar(String testo, Icona.Tipo icona, int tastoRapido, ActionListener azione) {
        super(testo, new Icona(icona));
        setMnemonic(tastoRapido);
        setToolTipText(testo + " (Alt+" + KeyEvent.getKeyText(tastoRapido) + ")");
        // Il fuoco resta sulla tabella o sui campi di testo.
        setFocusable(false);
        setHorizontalTextPosition(SwingConstants.CENTER);
        setVerticalTextPosition(SwingConstants.BOTTOM);
        addActionListener(azione);
    }
}
