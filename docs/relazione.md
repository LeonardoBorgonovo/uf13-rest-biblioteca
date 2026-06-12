# Relazione Tecnica - Standardizzazione Risposte ed Error Handling
**Studente:** Leonardo Borgonovo
**Corso:** Sviluppi SpringBoot 1 (UF13)

---

## 1. Obiettivi del Progetto e Criteri di Accettazione
L'intervento ha l'obiettivo di risolvere la frammentazione delle risposte HTTP fornite dall'applicazione e mettere in sicurezza il sistema contro la fuga di dati sensibili (stack trace). 

I criteri di accettazione soddisfatti sono:
- Implementazione di un contratto di risposta unico (Standard JSend).
- Centralizzazione della gestione delle eccezioni.
- Validazione dei dati real-time con messaggi d'errore localizzati per campo.
- Rispetto del vincolo di isolamento (nessun blocco try-catch nei controller).

## 2. Scelte Progettuali e Implementazione

### 2.1 Architettura della Risposta (`APIResponse<T>`)
È stata introdotta la classe generica `APIResponse<T>` nel package `domain.response` per incapsulare ogni tipo di output del server. La classe utilizza l'annotazione `@JsonInclude(JsonInclude.Include.NON_NULL)` per omettere i campi non necessari (es. `results` in caso di oggetto singolo). Fornisce metodi statici per gestire:
- Successi singoli (`success`) e collezioni (`successCollection`), calcolandone automaticamente la dimensione.
- Fallimenti di validazione (`fail`).
- Errori interni di sistema (`error`).

### 2.2 Gestione Centralizzata delle Eccezioni (`GlobalExceptionHandler`)
Sfruttando l'annotazione `@RestControllerAdvice`, è stata creata una classe deputata all'intercettazione globale delle anomalie:
- **`MethodArgumentNotValidException`**: Intercettata per mappare gli errori di validazione (400 Bad Request) restituendo una mappa chiave-valore con i campi non validi e i relativi messaggi di feedback.
- **`ResponseStatusException`**: Gestisce gli errori di business standardizzati (es. 404 Not Found).
- **`Exception.class`**: Cattura qualsiasi errore imprevisto di sistema (500 Internal Server Error), scrivendo lo stack trace nei log del server a uso degli sviluppatori, ma nascondendolo al client tramite un messaggio generico per motivi di sicurezza.

### 2.3 Layer dei Controller e DTO
I controller (`AutoreController` e `LibroController`) sono stati rifattorizzati per restituire esclusivamente oggetti `APIResponse` tipizzati, rispettando rigorosamente il vincolo di **non esporre le entità JPA**, ma mediando ogni scambio dati tramite oggetti DTO (`AutoreDTO`, `LibroDTO`).

---

## 3. Test Eseguiti e Validazione
Il sistema è stato avviato in ambiente di staging isolato tramite container Docker (MySQL 9.0) e testato tramite l'interfaccia Swagger UI (`/swagger-ui-dev.html`).

### 3.1 Test di Validazione Fallita (400 Bad Request)
Inviando un payload parziale all'endpoint `POST /libri/add`, i validatori JSR-383 (es. `@NotBlank`) hanno attivato correttamente l'handler. 
*Risultato ottenuto:*
```json
{
  "status": "FAIL",
  "message": "Errore nell validazione dei dati",
  "code": 400,
  "results": 3,
  "data": {
    "titolo": "Il titolo non può essere vuoto",
    "isbn": "ISBN campo obbligatorio",
    "autore": "ID autore obbligatorio"
  }
}
```