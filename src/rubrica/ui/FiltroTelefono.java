package rubrica.ui;

import java.awt.Toolkit;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;

import rubrica.dominio.Persona;

/**
 * Nel campo telefono lascia scrivere (o incollare) solo i caratteri ammessi
 * da {@link Persona#caratteriTelefonoAmmessi(String)}; per gli altri emette un beep.
 */
final class FiltroTelefono extends DocumentFilter {

    @Override
    public void insertString(FilterBypass fb, int offset, String testo, AttributeSet attributi)
            throws BadLocationException {
        replace(fb, offset, 0, testo, attributi);
    }

    @Override
    public void replace(FilterBypass fb, int offset, int lunghezza, String testo, AttributeSet attributi)
            throws BadLocationException {
        Document documento = fb.getDocument();
        String attuale = documento.getText(0, documento.getLength());
        String risultato = attuale.substring(0, offset)
                + (testo == null ? "" : testo)
                + attuale.substring(offset + lunghezza);
        if (Persona.caratteriTelefonoAmmessi(risultato)) {
            fb.replace(offset, lunghezza, testo, attributi);
        } else {
            Toolkit.getDefaultToolkit().beep();
        }
    }
}
