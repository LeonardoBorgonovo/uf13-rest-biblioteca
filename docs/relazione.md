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

---

## 4. Task 2: Containerizzazione Multi-Stage e Isolamento dei Profili

### 4.1 Scelte Progettuali Dockerfile Multi-Stage
Per ottimizzare il deployment e ridurre l'impronta di memoria dell'immagine in produzione, è stato implementato un `Dockerfile` strutturato in due stadi:
1. **Stage 1 (`build`)**: Sfrutta un'immagine `maven:4.0.0-rc-4-amazoncorretto-21-debian` per isolare la fase di compilazione e scaricamento delle dipendenze, generando il file `.jar` tramite `mvn clean package`.
2. **Stage 2 (`run`)**: Sfrutta un'immagine minimale basata su JRE (`amazoncorretto:21-alpine3.21-jre`). Strumenti di compilazione e file sorgenti vengono scartati, riducendo drasticamente il peso dell'immagine finale e la superficie di attacco per la sicurezza.

Inoltre, è stata inserita la variabile d'ambiente `ENV SPRING_PROFILES_ACTIVE=prod` per garantire che il profilo di produzione sia attivo di default, lasciando comunque l'immagine dinamica e configurabile dall'esterno in fase di run.

### 4.2 Strategia di Logging e Configurazione Profili
La gestione dei comportamenti dinamici tra gli ambienti di Sviluppo (`dev`) e Produzione (`prod`) è stata centralizzata nel file `logback-spring.xml`:
- **Profilo `dev`**: Configurato con livello globale `TRACE`. Mostra i log in console e attiva il tracciamento granulare delle query SQL generate da Hibernate (`org.hibernate.SQL` su `DEBUG` e i descrittori su `TRACE`).
- **Profilo `prod`**: Configurato con livello globale `INFO`. Disabilita i log a console e reindirizza l'output esclusivamente su un file rotante posizionato in `logs/biblioteca.log`. La rotazione è gestita tramite `SizeAndTimeBasedRollingPolicy` con un limite di 10MB per singolo file e una cronologia massima di 30 giorni, preservando l'integrità del disco dell'host.
- **Sicurezza del Database**: Nel file `application-prod.properties`, la proprietà `spring.jpa.hibernate.ddl-auto` è stata impostata rigorosamente su `validate` (anziché `update`) per impedire modifiche strutturali accidentali allo schema del database in ambiente di produzione.