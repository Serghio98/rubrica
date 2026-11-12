package rubrica.ui;

import java.awt.BorderLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.sql.SQLException;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.KeyStroke;
import javax.swing.text.AbstractDocument;

import rubrica.dominio.Persona;

/**
 * Finestra "editor-persona": modulo per inserire o modificare i dati di una persona.
 * È modale, quindi resta sopra la finestra principale finché non viene chiusa.
 */
public class EditorPersona extends JDialog {

    /** Salva i dati validati, ad esempio nel database. */
    public interface Salvataggio {
        void salva(Persona dati) throws SQLException;
    }

    private final Salvataggio salvataggio;

    private final JTextField campoNome = new JTextField(22);
    private final JTextField campoCognome = new JTextField(22);
    private final JTextField campoIndirizzo = new JTextField(22);
    private final JTextField campoTelefono = new JTextField(22);
    private final JTextField campoEta = new JTextField(22);

    /** I dati salvati, oppure null finché il salvataggio non riesce. */
    private Persona risultato;

    /**
     * Apre l'editor sopra la finestra indicata e attende che venga chiuso.
     * Con Salva i dati vengono controllati e passati a {@code salvataggio}: se il
     * salvataggio non riesce l'errore viene mostrato e l'editor resta aperto.
     *
     * @param persona la persona da modificare, oppure null per inserirne una nuova
     * @return i dati salvati, oppure null se l'utente ha annullato
     */
    public static Persona apri(Window proprietario, Persona persona, Salvataggio salvataggio) {
        EditorPersona editor = new EditorPersona(proprietario, persona, salvataggio);
        editor.setVisible(true);
        return editor.risultato;
    }

    private EditorPersona(Window proprietario, Persona persona, Salvataggio salvataggio) {
        super(proprietario,
                persona == null ? "Editor persona - Nuova persona" : "Editor persona - Modifica",
                ModalityType.APPLICATION_MODAL);
        this.salvataggio = salvataggio;

        if (persona != null) {
            campoNome.setText(persona.getNome());
            campoCognome.setText(persona.getCognome());
            campoIndirizzo.setText(persona.getIndirizzo());
            campoTelefono.setText(persona.getTelefono());
            campoEta.setText(persona.isEtaIndicata() ? String.valueOf(persona.getEta()) : "");
        }
        // Il filtro va messo dopo aver riempito i campi, così un telefono non valido
        // già salvato resta visibile (e va corretto) invece di sparire.
        ((AbstractDocument) campoTelefono.getDocument()).setDocumentFilter(new FiltroTelefono());
        campoTelefono.setToolTipText("Cifre, spazi, ( ) - . / e un + iniziale, ad esempio +39 348 000 0000");

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new BottoneToolbar("Salva", Icona.Tipo.SALVA, KeyEvent.VK_S, e -> salva()));
        toolbar.add(new BottoneToolbar("Annulla", Icona.Tipo.ANNULLA, KeyEvent.VK_A, e -> dispose()));

        JPanel modulo = new JPanel(new GridBagLayout());
        modulo.setBorder(BorderFactory.createEmptyBorder(10, 12, 12, 12));
        Modulo.aggiungiRiga(modulo, 0, "Nome", campoNome);
        Modulo.aggiungiRiga(modulo, 1, "Cognome", campoCognome);
        Modulo.aggiungiRiga(modulo, 2, "Indirizzo", campoIndirizzo);
        Modulo.aggiungiRiga(modulo, 3, "Telefono", campoTelefono);
        Modulo.aggiungiRiga(modulo, 4, "Età", campoEta);
        Modulo.aggiungiTesto(modulo, 5, "Obbligatori: telefono e almeno uno tra nome e cognome.", true);

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

    /** Controlla i dati inseriti; se sono validi li salva e chiude la finestra. */
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
        if (!controllaLunghezza("Nome", campoNome, nome, Persona.LUNGHEZZA_MASSIMA_NOME)
                || !controllaLunghezza("Cognome", campoCognome, cognome, Persona.LUNGHEZZA_MASSIMA_COGNOME)
                || !controllaLunghezza("Indirizzo", campoIndirizzo, indirizzo, Persona.LUNGHEZZA_MASSIMA_INDIRIZZO)
                || !controllaLunghezza("Telefono", campoTelefono, telefono, Persona.LUNGHEZZA_MASSIMA_TELEFONO)) {
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

        Persona dati = new Persona(nome, cognome, indirizzo, telefono, eta);
        try {
            salvataggio.salva(dati);
        } catch (SQLException e) {
            Messaggi.erroreDatabase(this, e);
            return;
        }
        risultato = dati;
        dispose();
    }

    /** I campi non possono superare la lunghezza delle colonne del database. */
    private boolean controllaLunghezza(String etichetta, JTextField campo, String valore, int massimo) {
        if (valore.length() > massimo) {
            mostraErrore("Il campo " + etichetta + " può contenere al massimo " + massimo + " caratteri.", campo);
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
