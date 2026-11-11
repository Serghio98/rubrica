# Rubrica

Rubrica telefonica in Java Swing: elenco di persone (nome, cognome, indirizzo,
telefono, età) con inserimento, modifica ed eliminazione. I dati vengono salvati
nel file `informazioni.txt`.

## Esecuzione

Serve Java 8 o successivo.

- Doppio clic su `Rubrica.jar`, oppure da terminale: `java -jar Rubrica.jar`
- I dati sono in `informazioni.txt`, **nella stessa cartella di `Rubrica.jar`**.
  Se il file non esiste la rubrica parte vuota e il file viene creato al primo salvataggio.
- Formato del file (UTF-8), una persona per riga: `nome;cognome;indirizzo;telefono;eta`.
  I campi non compilati restano vuoti, ad esempio `Mario;;;+39 348 000 0000;`

## Uso

| Finestra | Comando | Tasti |
|---|---|---|
| Principale | Nuovo | Alt+N |
| Principale | Modifica | Alt+M, oppure doppio clic su una riga |
| Principale | Elimina | Alt+E |
| Editor persona | Salva | Alt+S oppure Invio |
| Editor persona | Annulla | Alt+A oppure Esc |

Regole dell'editor:

- è obbligatorio almeno uno tra nome e cognome;
- il telefono è obbligatorio e può contenere solo cifre, spazi, i simboli `( ) - . /`
  e un `+` iniziale (es. `+39 348 000 0000`, `(212) 555-1234`, `030/123456`), con
  almeno 3 cifre. Gli altri caratteri non si possono nemmeno digitare;
- indirizzo ed età sono facoltativi; l'età, se indicata, deve essere un numero intero tra 1 e 150;
- il carattere `;` non è ammesso, perché è il separatore del file.

## Compilazione

Con un JDK 9 o successivo, da PowerShell nella cartella del progetto:

```powershell
.\build.ps1
```

Lo script compila i sorgenti per Java 8, esegue le verifiche automatiche della
cartella `test/` (persistenza su file, regole su età e telefono) e crea `Rubrica.jar`.

## Struttura

| Package | Classi | Ruolo |
|---|---|---|
| `rubrica` | `Main` | Avvio: carica i dati e apre la finestra principale |
| `rubrica.dominio` | `Persona` | Dati di un contatto e regole su età e telefono |
| `rubrica.logica` | `Rubrica` | Elenco (`Vector`) delle persone; riscrive il file dopo ogni modifica |
| `rubrica.persistenza` | `GestoreFile` | Lettura con `Scanner` e scrittura con `PrintStream` di `informazioni.txt` |
| `rubrica.ui` | `FinestraPrincipale`, `EditorPersona`, `PersonaTableModel`, `FiltroTelefono`, `BottoneToolbar`, `Icona`, `IconaApplicazione` | Interfaccia Swing con `JToolBar`; le icone dei bottoni e delle finestre sono disegnate via codice |

## Test manuali (dalla traccia)

- [ ] 1. Aggiungo una persona e la trovo in tabella
- [ ] 2. Aggiungo una persona, la seleziono, premo Modifica: l'editor mostra i suoi dati
- [ ] 3. Aggiungo 3 persone, elimino quella di mezzo, modifico l'ultima: tabella ed editor sono coerenti
- [ ] 4. Aggiungo una persona, chiudo e riapro: la persona c'è, in tabella e nell'editor. Ne aggiungo altre 2, richiudo e riapro: c'è tutto
- [ ] 5. Tra un'apertura e l'altra il contenuto di `informazioni.txt` è corretto
- [ ] 6. Il programma funziona anche su un altro computer
