package rubrica.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.AbstractDocument;

import rubrica.dominio.Persona;
import rubrica.persistenza.GestoreFile;

/**
 * Finestra "editor-persona": modulo per inserire o modificare i dati di una persona.
 * È modale, quindi resta sopra la finestra principale finché non viene chiusa.
 */
public class EditorPersona extends JDialog {

    private final JTextField campoNome = new JTextField(22);
    private final JTextField campoCognome = new JTextField(22);
    private final JTextField campoIndirizzo = new JTextField(22);
    private final JTextField campoTelefono = new JTextField(22);
    private final JTextField campoEta = new JTextField(22);

    /** I dati inseriti, oppure null finché l'utente non preme Salva. */
    private Persona risultato;

    /**
     * Apre l'editor sopra la finestra indicata e attende che venga chiuso.
     *
     * @param persona la persona da modificare, oppure null per inserirne una nuova
     * @return i dati inseriti se l'utente ha premuto Salva, null se ha annullato
     */
    public static Persona apri(Window proprietario, Persona persona) {
        EditorPersona editor = new EditorPersona(proprietario, persona);
        editor.setVisible(true);
        return editor.risultato;
    }

    private EditorPersona(Window proprietario, Persona persona) {
        super(proprietario,
                persona == null ? "Editor persona - Nuova persona" : "Editor persona - Modifica",
                ModalityType.APPLICATION_MODAL);

        if (persona != null) {
            campoNome.setText(persona.getNome());
            campoCognome.setText(persona.getCognome());
            campoIndirizzo.setText(persona.getIndirizzo());
            campoTelefono.setText(persona.getTelefono());
            campoEta.setText(persona.isEtaIndicata() ? String.valueOf(persona.getEta()) : "");
        }
        // Il filtro va messo dopo aver riempito i campi, così un telefono non valido
        // letto dal file resta visibile (e va corretto) invece di sparire.
        ((AbstractDocument) campoTelefono.getDocument()).setDocumentFilter(new FiltroTelefono());
        campoTelefono.setToolTipText("Cifre, spazi, ( ) - . / e un + iniziale, ad esempio +39 348 000 0000");

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new BottoneToolbar("Salva", Icona.Tipo.SALVA, KeyEvent.VK_S, e -> salva()));
        toolbar.add(new BottoneToolbar("Annulla", Icona.Tipo.ANNULLA, KeyEvent.VK_A, e -> dispose()));

        JPanel modulo = new JPanel(new GridBagLayout());
        modulo.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        aggiungiRiga(modulo, 0, "Nome", campoNome);
        aggiungiRiga(modulo, 1, "Cognome", campoCognome);
        aggiungiRiga(modulo, 2, "Indirizzo", campoIndirizzo);
        aggiungiRiga(modulo, 3, "Telefono", campoTelefono);
        aggiungiRiga(modulo, 4, "Età", campoEta);
        aggiungiNota(modulo, 5, "Obbligatori: telefono e almeno uno tra nome e cognome.");

        add(toolbar, BorderLayout.NORTH);
        add(modulo, BorderLayout.CENTER);

        // Invio = Salva, Esc = Annulla
        collegaTasto(KeyEvent.VK_ENTER, "salva", this::salva);
        collegaTasto(KeyEvent.VK_ESCAPE, "annulla", this::dispose);

        setIconImages(IconaApplicazione.immagini());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        pack();
        setMinimumSize(getSize());
        setLocationRelativeTo(proprietario);
    }

    private static void aggiungiRiga(JPanel modulo, int riga, String etichetta, JTextField campo) {
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

    private static void aggiungiNota(JPanel modulo, int riga, String testo) {
        JLabel nota = new JLabel(testo);
        nota.setForeground(UIManager.getColor("Label.disabledForeground"));
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = riga;
        c.gridx = 0;
        c.gridwidth = 2;
        c.insets = new Insets(8, 4, 0, 4);
        c.anchor = GridBagConstraints.LINE_START;
        modulo.add(nota, c);
    }

    private void collegaTasto(int tasto, String nome, Runnable azione) {
        JRootPane radice = getRootPane();
        radice.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(tasto, 0), nome);
        radice.getActionMap().put(nome, new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                azione.run();
            }
        });
    }

    /** Controlla i dati inseriti; se sono validi li memorizza e chiude la finestra. */
    private void salva() {
        String nome = campoNome.getText().trim();
        String cognome = campoCognome.getText().trim();
        String indirizzo = campoIndirizzo.getText().trim();
        String telefono = campoTelefono.getText().trim();
        String testoEta = campoEta.getText().trim();

        if (nome.isEmpty() && cognome.isEmpty()) {
            mostraErrore("Inserire almeno il nome o il cognome.", campoNome);
            return;
        }
        if (!controllaSeparatore("Nome", campoNome)
                || !controllaSeparatore("Cognome", campoCognome)
                || !controllaSeparatore("Indirizzo", campoIndirizzo)) {
            return;
        }
        if (telefono.isEmpty()) {
            mostraErrore("Il telefono è obbligatorio.", campoTelefono);
            return;
        }
        if (!Persona.telefonoValido(telefono)) {
            mostraErrore("Il telefono può contenere solo cifre, spazi, i simboli ( ) - . / e un + iniziale,\n"
                    + "e deve avere almeno 3 cifre. Esempio: +39 348 000 0000", campoTelefono);
            return;
        }
        int eta = Persona.ETA_NON_INDICATA;
        if (!testoEta.isEmpty()) {
            try {
                eta = Integer.parseInt(testoEta);
            } catch (NumberFormatException e) {
                eta = -1;
            }
            if (!Persona.etaValida(eta)) {
                mostraErrore("L'età deve essere un numero intero tra " + Persona.ETA_MINIMA
                        + " e " + Persona.ETA_MASSIMA + ", oppure vuota.", campoEta);
                return;
            }
        }

        risultato = new Persona(nome, cognome, indirizzo, telefono, eta);
        dispose();
    }

    /** Il separatore del file non può comparire nei dati, altrimenti la riga salvata sarebbe illeggibile. */
    private boolean controllaSeparatore(String etichetta, JTextField campo) {
        if (campo.getText().contains(GestoreFile.SEPARATORE)) {
            mostraErrore("Il carattere \"" + GestoreFile.SEPARATORE + "\" non è consentito nel campo "
                    + etichetta + ".", campo);
            return false;
        }
        return true;
    }

    private void mostraErrore(String messaggio, JTextField campo) {
        JOptionPane.showMessageDialog(this, messaggio, "Dati non validi", JOptionPane.ERROR_MESSAGE);
        campo.requestFocusInWindow();
        campo.selectAll();
    }
}
