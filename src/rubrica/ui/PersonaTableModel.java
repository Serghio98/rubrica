package rubrica.ui;

import javax.swing.table.AbstractTableModel;

import rubrica.dominio.Persona;
import rubrica.logica.Rubrica;

/**
 * Mostra le persone della rubrica in tabella: una riga per persona,
 * colonne nome, cognome e telefono. Le celle non sono modificabili.
 */
class PersonaTableModel extends AbstractTableModel {

    private static final String[] COLONNE = {"Nome", "Cognome", "Telefono"};

    private final Rubrica rubrica;

    PersonaTableModel(Rubrica rubrica) {
        this.rubrica = rubrica;
    }

    @Override
    public int getRowCount() {
        return rubrica.getNumeroPersone();
    }

    @Override
    public int getColumnCount() {
        return COLONNE.length;
    }

    @Override
    public String getColumnName(int colonna) {
        return COLONNE[colonna];
    }

    @Override
    public Object getValueAt(int riga, int colonna) {
        Persona persona = rubrica.getPersona(riga);
        switch (colonna) {
            case 0:
                return persona.getNome();
            case 1:
                return persona.getCognome();
            case 2:
                return persona.getTelefono();
            default:
                throw new IllegalArgumentException("Colonna non valida: " + colonna);
        }
    }
}
