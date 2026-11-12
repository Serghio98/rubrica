package rubrica.ui;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.GridBagLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.function.Consumer;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import rubrica.dominio.Utente;
import rubrica.logica.Autenticazione;

/**
 * Finestra di login, l'unica mostrata all'avvio. Con utente e password
 * corretti si chiude e lascia il posto alla rubrica di quell'utente.
 * <p>
 * Il bottone REGISTRA crea un nuovo utente e, se la registrazione riesce,
 * entra subito nella sua rubrica (vuota). Al primo avvio, quando non esiste
 * ancora nessun utente, la registrazione si apre da sola.
 */
public class FinestraLogin extends JFrame {

    private static final String INTRO_PRIMO_AVVIO = "Primo avvio: crea il primo utente per accedere alla rubrica.";

    private final Autenticazione autenticazione;
    private final Consumer<Utente> dopoAccesso;

    private final JTextField campoUtente = new JTextField(18);
    private final JPasswordField campoPassword = new JPasswordField(18);

    /**
     * @param primoAvvio true se non esiste ancora nessun utente
     * @param dopoAccesso eseguito con l'utente entrato, dopo un login o una registrazione riusciti
     */
    public FinestraLogin(Autenticazione autenticazione, boolean primoAvvio, Consumer<Utente> dopoAccesso) {
        super("Rubrica - Login");
        this.autenticazione = autenticazione;
        this.dopoAccesso = dopoAccesso;

        JPanel modulo = new JPanel(new GridBagLayout());
        modulo.setBorder(BorderFactory.createEmptyBorder(12, 12, 4, 12));
        Modulo.aggiungiRiga(modulo, 0, "Utente", campoUtente);
        Modulo.aggiungiRiga(modulo, 1, "Password", campoPassword);

        JButton login = new JButton("LOGIN");
        login.addActionListener(e -> accedi());
        JButton registra = new JButton("REGISTRA");
        registra.addActionListener(e -> registra(null));
        // Invio = LOGIN
        getRootPane().setDefaultButton(login);

        JPanel bottoni = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bottoni.setBorder(BorderFactory.createEmptyBorder(0, 8, 8, 8));
        bottoni.add(login);
        bottoni.add(registra);

        add(modulo, BorderLayout.CENTER);
        add(bottoni, BorderLayout.SOUTH);

        if (primoAvvio) {
            addWindowListener(new WindowAdapter() {
                @Override
                public void windowOpened(WindowEvent e) {
                    SwingUtilities.invokeLater(() -> registra(INTRO_PRIMO_AVVIO));
                }
            });
        }

        setIconImages(IconaApplicazione.immagini());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setResizable(false);
        pack();
        setLocationRelativeTo(null);
    }

    private void accedi() {
        char[] password = campoPassword.getPassword();
        try {
            Utente utente = autenticazione.accedi(campoUtente.getText(), password);
            if (utente != null) {
                entra(utente);
            } else {
                JOptionPane.showMessageDialog(this, "Login errato.", "Login", JOptionPane.ERROR_MESSAGE);
                campoPassword.setText("");
                campoPassword.requestFocusInWindow();
            }
        } catch (SQLException e) {
            Messaggi.erroreDatabase(this, e);
        } finally {
            Arrays.fill(password, '\0');
        }
    }

    /** Apre la registrazione; se il nuovo utente viene creato entra subito nella sua rubrica. */
    private void registra(String introduzione) {
        Utente nuovo = FinestraRegistrazione.apri(this, autenticazione, introduzione);
        if (nuovo != null) {
            entra(nuovo);
        }
    }

    /** Apre la rubrica e chiude il login (dispose non termina il programma, a differenza della X). */
    private void entra(Utente utente) {
        dopoAccesso.accept(utente);
        dispose();
    }
}
