package rubrica;

import rubrica.dominio.Persona;

/**
 * Verifica automatica delle regole su età e telefono (eseguita da build.ps1).
 * Termina con codice di uscita diverso da 0 se un controllo fallisce.
 */
public class VerificaRegole {

    private static int controlli = 0;

    public static void main(String[] args) {
        verifica(!Persona.etaValida(0), "età 0 rifiutata");
        verifica(!Persona.etaValida(-5), "età negativa rifiutata");
        verifica(Persona.etaValida(1), "età 1 accettata");
        verifica(Persona.etaValida(150), "età 150 accettata");
        verifica(!Persona.etaValida(151), "età 151 rifiutata");

        verifica(new Persona("Mario", "", "", "123", 30).getNomeCompleto().equals("Mario"), "nome completo: solo nome");
        verifica(new Persona("", "Rossi", "", "123", 30).getNomeCompleto().equals("Rossi"), "nome completo: solo cognome");
        verifica(new Persona("Mario", "Rossi", "", "123", 30).getNomeCompleto().equals("Mario Rossi"), "nome completo: entrambi");
        verifica(!new Persona("Mario", "", "", "123", Persona.ETA_NON_INDICATA).isEtaIndicata(), "età non indicata");
        verifica(new Persona("Mario", "", "", "123", 30).isEtaIndicata(), "età indicata");

        String[] validi = {
            "+39 348 000 0000",     // Italia, cellulare con prefisso internazionale
            "0039 348 000 0000",
            "3480000000",
            "06 1234567",
            "112",
            "(212) 555-1234",        // Stati Uniti
            "+1 (212) 555-1234",
            "030/123456",            // Germania
            "01.23.45.67.89",        // Francia
        };
        for (String telefono : validi) {
            verifica(Persona.telefonoValido(telefono), "telefono valido: '" + telefono + "'");
        }

        String[] nonValidi = {"", "+","+ ", "12", "+39", "348abc", "39+348", "++39 348", "348;000", "tel. 348 000", "348#1"};
        for (String telefono : nonValidi) {
            verifica(!Persona.telefonoValido(telefono), "telefono non valido: '" + telefono + "'");
        }

        // mentre si scrive: si può iniziare con +, ma non metterlo dopo né usare lettere
        verifica(Persona.caratteriTelefonoAmmessi("+"), "si può iniziare a scrivere con +");
        verifica(!Persona.caratteriTelefonoAmmessi("3+"), "+ ammesso solo all'inizio");
        verifica(!Persona.caratteriTelefonoAmmessi("3a"), "lettere non ammesse");

        System.out.println("Verifica regole: " + controlli + " controlli superati.");
    }

    private static void verifica(boolean condizione, String descrizione) {
        controlli++;
        if (!condizione) {
            throw new AssertionError("FALLITO: " + descrizione);
        }
    }
}
