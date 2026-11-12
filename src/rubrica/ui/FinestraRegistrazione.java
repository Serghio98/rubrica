package rubrica.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.sql.SQLException;
import java.util.Arrays;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.KeyStroke;

import rubrica.dominio.Utente;
import rubrica.logica.Autenticazione;

/**
 * Finestra per registrare un nuovo utente: nome utente, password e conferma.
 * Della password viene salvato solo l'hash.
 */
public class FinestraRegistrazione extends JDialog {

    private final Autenticazione autenticazione;

    private final JTextField campoUtente = new JTextField(18);
    private final JPasswordField campoPassword = new JPasswordField(18);
    private final JPasswordField campoConferma = new JPasswordField(18);

    /** L'utente registrato, oppure null finché la registrazione non riesce. */
    private Utente registrato;

    /**
     * Apre la registrazione sopra la finestra indicata e attende che venga chiusa.
     *
     * @param introduzione testo da mostrare in cima al modulo, oppure null
     * @return l'utente creato, oppure null se l'utente ha annullato
     */
    static Utente apri(Window proprietario, Autenticazione autenticazione, String introduzione) {
        FinestraRegistrazione finestra = new FinestraRegistrazione(proprietario, autenticazione, introduzione);
        finestra.setVisible(true);
        return finestra.registrato;
    }

    private FinestraRegistrazione(Window proprietario, Autenticazione autenticazione, String introduzione) {
        super(proprietario, "Rubrica - Registrazione", ModalityType.APPLICATION_MODAL);
        this.autenticazione = autenticazione;

        JPanel modulo = new JPanel(new GridBagLayout());
        modulo.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        int riga = 0;
        if (introduzione != null) {
            Modulo.aggiungiTesto(modulo, riga++, introduzione, false);
        }
        Modulo.aggiungiRiga(modulo, riga++, "Utente", campoUtente);
        Modulo.aggiungiRiga(modulo, riga++, "Password", campoPassword);
        Modulo.aggiungiRiga(modulo, riga++, "Conferma password", campoConferma);
        Modulo.aggiungiTesto(modulo, riga, "La password deve avere almeno "
                + Autenticazione.LUNGHEZZA_MINIMA_PASSWORD + " caratteri.", true);

        JButton registra = new JButton("REGISTRA");
        registra.addActionListener(e -> registra());
        JButton annulla = new JButton("ANNULLA");
        annulla.addActionListener(e -> dispose());
        // Invio = REGISTRA, Esc = ANNULLA
        getRootPane().setDefaultButton(registra);
        getRootPane().registerKeyboardAction(e -> dispose(),
                KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_IN_FOCUSED_WINDOW);

        JPanel bottoni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottoni.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        bottoni.add(registra);
        bottoni.add(annulla);

        add(modulo, BorderLayout.CENTER);
        add(bottoni, BorderLayout.SOUTH);

        setIconImages(IconaApplicazione.immagini());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(proprietario);
    }

    private void registra() {
        String utente = campoUtente.getText().trim();
        char[] password = campoPassword.getPassword();
        char[] conferma = campoConferma.getPassword();
        try {
            if (utente.isEmpty()) {
                mostraErrore("Inserire il nome utente.", campoUtente);
            } else if (!Autenticazione.usernameValido(utente)) {
                mostraErrore("Il nome utente può avere al massimo "
                        + Utente.LUNGHEZZA_MASSIMA_USERNAME + " caratteri.", campoUtente);
            } else if (autenticazione.esiste(utente)) {
                mostraErrore("Il nome utente \"" + utente + "\" esiste già.", campoUtente);
            } else if (password.length < Autenticazione.LUNGHEZZA_MINIMA_PASSWORD) {
                mostraErrore("La password deve avere almeno "
                        + Autenticazione.LUNGHEZZA_MINIMA_PASSWORD + " caratteri.", campoPassword);
            } else if (!Arrays.equals(password, conferma)) {
                mostraErrore("Le due password non coincidono.", campoConferma);
            } else {
                registrato = autenticazione.registra(utente, password);
                dispose();
            }
        } catch (SQLException e) {
            Messaggi.erroreDatabase(this, e);
        } finally {
            Arrays.fill(password, '\0');
            Arrays.fill(conferma, '\0');
        }
    }

    private void mostraErrore(String messaggio, JTextField campo) {
        JOptionPane.showMessageDialog(this, messaggio, "Dati non validi", JOptionPane.ERROR_MESSAGE);
        campo.requestFocusInWindow();
        campo.selectAll();
    }
}
