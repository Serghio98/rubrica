package rubrica.logica;

import java.io.IOException;
import java.util.Vector;

import rubrica.dominio.Persona;
import rubrica.persistenza.GestoreFile;

/**
 * Elenco delle persone della rubrica.
 * <p>
 * Dopo ogni aggiunta, modifica o eliminazione salva l'intero elenco su file,
 * così il contenuto del file corrisponde sempre a quello mostrato in tabella.
 * Se il salvataggio fallisce la modifica resta comunque applicata in memoria
 * e viene lanciata una {@link IOException}.
 */
public class Rubrica {

    private final Vector<Persona> persone = new Vector<>();
    private final GestoreFile gestoreFile;

    public Rubrica(GestoreFile gestoreFile) {
        this.gestoreFile = gestoreFile;
    }

    /** Sostituisce l'elenco in memoria con le persone salvate nel file. */
    public void carica() throws IOException {
        Vector<Persona> caricate = gestoreFile.carica();
        persone.clear();
        persone.addAll(caricate);
    }

    public int getNumeroPersone() {
        return persone.size();
    }

    public Persona getPersona(int indice) {
        return persone.get(indice);
    }

    public void aggiungi(Persona persona) throws IOException {
        persone.add(persona);
        salva();
    }

    /** Copia {@code nuoviDati} nella persona in posizione {@code indice}. */
    public void modifica(int indice, Persona nuoviDati) throws IOException {
        persone.get(indice).copiaDa(nuoviDati);
        salva();
    }

    public void elimina(int indice) throws IOException {
        persone.remove(indice);
        salva();
    }

    private void salva() throws IOException {
        gestoreFile.salva(persone);
    }
}
