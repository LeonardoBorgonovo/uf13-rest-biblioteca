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

## 5. Task 3: Monitoraggio Proattivo e Sistemi di Alerting

### 5.1 Infrastruttura di Monitoraggio (Prometheus & Grafana)
L'architettura è stata estesa introducendo un sistema di telemetria proattiva per prevenire i disservizi in produzione. Tramite `docker-compose.yml`, sono stati orchestrati tre sistemi interconnessi:
- **Spring Boot Actuator + Micrometer**: Espone i dati interni della JVM in formato Prometheus sull'endpoint `/actuator/prometheus`.
- **Prometheus**: Configurato con uno `scrape_interval` di 5 secondi, interroga l'applicazione e storicizza le metriche temporali.
- **Grafana**: Configurato sulla porta 3000, interroga Prometheus come Data Source. È stata importata la dashboard ufficiale JVM (ID: 4701) per la visualizzazione in tempo reale di memoria Heap, Garbage Collector e stato dei Thread.

### 5.2 Configurazione del Sistema di Alerting e Simulazione di Guasto
Per soddisfare i criteri di accettazione e validazione legati alla tolleranza ai guasti, è stata implementata una regola di allarme (Alert Rule) basata sulla seguente query PromQL:
`sum(http_server_requests_seconds_count{status="500"})`

La regola prevede l'attivazione automatica dello stato di **Firing** (Allarme Critico Visivo) al superamento della soglia di 10 errori interni del server (HTTP 500).

Al fine di testare il sistema, è stato predisposto un endpoint di stress-test dedicato nel controller (`/libri/stress-test-500`). La chiamata ripetuta a tale endpoint innesca il `GlobalExceptionHandler` configurato nel Task 1, incrementando la metrica e dimostrando visivamente il corretto funzionamento dell'allarme e il cambio di stato della cabina di regia su Grafana.
