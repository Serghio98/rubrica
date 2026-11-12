package rubrica.logica;

import java.sql.SQLException;
import java.util.Vector;

import rubrica.dominio.Persona;
import rubrica.dominio.Utente;
import rubrica.persistenza.GestorePersone;

/**
 * I contatti di un utente, tenuti in un {@link Vector}.
 * <p>
 * Ogni aggiunta, modifica o eliminazione viene prima salvata nel database e
 * solo se il salvataggio riesce viene applicata all'elenco in memoria: se il
 * database dà errore, l'elenco (e quindi la tabella) resta com'era.
 */
public class Rubrica {

    private final Vector<Persona> persone = new Vector<>();
    private final GestorePersone gestore;
    private final Utente utente;

    public Rubrica(GestorePersone gestore, Utente utente) {
        this.gestore = gestore;
        this.utente = utente;
    }

    public Utente getUtente() {
        return utente;
    }

    /** Sostituisce l'elenco in memoria con i contatti dell'utente salvati nel database. */
    public void carica() throws SQLException {
        Vector<Persona> caricate = gestore.carica(utente.getId());
        persone.clear();
        persone.addAll(caricate);
    }

    public int getNumeroPersone() {
        return persone.size();
    }

    public Persona getPersona(int indice) {
        return persone.get(indice);
    }

    public void aggiungi(Persona persona) throws SQLException {
        gestore.inserisci(utente.getId(), persona);
        persone.add(persona);
    }

    /** Copia {@code nuoviDati} nella persona in posizione {@code indice}. */
    public void modifica(int indice, Persona nuoviDati) throws SQLException {
        Persona attuale = persone.get(indice);
        Persona aggiornata = new Persona(nuoviDati.getNome(), nuoviDati.getCognome(),
                nuoviDati.getIndirizzo(), nuoviDati.getTelefono(), nuoviDati.getEta());
        aggiornata.setId(attuale.getId());
        gestore.aggiorna(utente.getId(), aggiornata);
        attuale.copiaDa(nuoviDati);
    }

    public void elimina(int indice) throws SQLException {
        gestore.elimina(utente.getId(), persone.get(indice));
        persone.remove(indice);
    }
}
