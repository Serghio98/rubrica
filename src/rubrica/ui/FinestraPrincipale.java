package rubrica.ui;

import java.awt.BorderLayout;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.SQLException;

import javax.swing.Box;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JToolBar;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;

import rubrica.dominio.Persona;
import rubrica.logica.Rubrica;

/**
 * Finestra principale: tabella dei contatti dell'utente che ha fatto il login
 * e toolbar con Nuovo, Modifica, Elimina e, a destra, Logout.
 */
public class FinestraPrincipale extends JFrame {

    private final Rubrica rubrica;
    private final Runnable dopoLogout;
    private final PersonaTableModel modello;
    private final JTable tabella;

    /**
     * @param dopoLogout eseguito dal bottone Logout, per tornare alla finestra di login
     */
    public FinestraPrincipale(Rubrica rubrica, Runnable dopoLogout) {
        super("Rubrica - " + rubrica.getUtente().getUsername());
        this.rubrica = rubrica;
        this.dopoLogout = dopoLogout;
        this.modello = new PersonaTableModel(rubrica);
        this.tabella = new JTable(modello);

        tabella.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        tabella.setFillsViewportHeight(true);
        tabella.getTableHeader().setReorderingAllowed(false);
        tabella.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                boolean doppioClicSuRiga = e.getClickCount() == 2
                        && SwingUtilities.isLeftMouseButton(e)
                        && tabella.rowAtPoint(e.getPoint()) >= 0;
                if (doppioClicSuRiga) {
                    modifica();
                }
            }
        });

        JToolBar toolbar = new JToolBar();
        toolbar.setFloatable(false);
        toolbar.add(new BottoneToolbar("Nuovo", Icona.Tipo.NUOVO, KeyEvent.VK_N, e -> nuovo()));
        toolbar.add(new BottoneToolbar("Modifica", Icona.Tipo.MODIFICA, KeyEvent.VK_M, e -> modifica()));
        toolbar.add(new BottoneToolbar("Elimina", Icona.Tipo.ELIMINA, KeyEvent.VK_E, e -> elimina()));
        toolbar.add(Box.createHorizontalGlue());
        toolbar.add(new BottoneToolbar("Logout", Icona.Tipo.LOGOUT, KeyEvent.VK_L, e -> logout()));

        add(toolbar, BorderLayout.NORTH);
        add(new JScrollPane(tabella), BorderLayout.CENTER);

        setIconImages(IconaApplicazione.immagini());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(640, 420);
        setLocationRelativeTo(null);
    }

    private void nuovo() {
        if (EditorPersona.apri(this, null, rubrica::aggiungi) == null) {
            return;
        }
        int riga = rubrica.getNumeroPersone() - 1;
        modello.fireTableRowsInserted(riga, riga);
        seleziona(riga);
    }

    private void modifica() {
        int riga = rigaSelezionata();
        if (riga < 0) {
            mostraErroreSelezione("Per modificare è necessario prima selezionare una persona.");
            return;
        }
        if (EditorPersona.apri(this, rubrica.getPersona(riga), dati -> rubrica.modifica(riga, dati)) == null) {
            return;
        }
        modello.fireTableRowsUpdated(riga, riga);
    }

    private void elimina() {
        int riga = rigaSelezionata();
        if (riga < 0) {
            mostraErroreSelezione("Per eliminare è necessario prima selezionare una persona.");
            return;
        }
        Persona persona = rubrica.getPersona(riga);
        int scelta = JOptionPane.showConfirmDialog(this,
                "Eliminare la persona " + persona.getNomeCompleto() + "?",
                "Conferma eliminazione", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        if (scelta != JOptionPane.YES_OPTION) {
            return;
        }
        try {
            rubrica.elimina(riga);
        } catch (SQLException e) {
            Messaggi.erroreDatabase(this, e);
            return;
        }
        modello.fireTableRowsDeleted(riga, riga);
    }

    /**
     * Chiude la rubrica di questo utente e torna al login, senza riavviare il programma.
     * Non serve chiedere conferma: ogni modifica è già salvata nel database.
     */
    private void logout() {
        // prima si apre il login, poi si chiude questa finestra (dispose non termina il programma)
        dopoLogout.run();
        dispose();
    }

    /** Indice nella rubrica della riga selezionata, oppure -1 se nessuna riga è selezionata. */
    private int rigaSelezionata() {
        int rigaVista = tabella.getSelectedRow();
        return rigaVista < 0 ? -1 : tabella.convertRowIndexToModel(rigaVista);
    }

    private void seleziona(int riga) {
        int rigaVista = tabella.convertRowIndexToView(riga);
        tabella.setRowSelectionInterval(rigaVista, rigaVista);
        tabella.scrollRectToVisible(tabella.getCellRect(rigaVista, 0, true));
    }

    private void mostraErroreSelezione(String messaggio) {
        JOptionPane.showMessageDialog(this, messaggio, "Nessuna persona selezionata", JOptionPane.ERROR_MESSAGE);
    }
}
