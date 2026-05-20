package it.marconi.biblioteca.controllers.exception;

import it.marconi.biblioteca.domain.response.APIResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.*;

@ControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handle(Exception ex){
        List<String> errors = new ArrayList<>();
        Arrays.stream(ex.getStackTrace())
                .forEach(st -> errors.add(st.toString()));

        log.error("Errore non gestito: {}", ex.getMessage(), ex);

        return ResponseEntity.internalServerError().body(
                APIResponse.error(
                        errors,
                        ex.getMessage(),
                        500
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<APIResponse<Object>> handle(ResponseStatusException ex){
        log.warn("Errore gestione risposte: {}", ex.getReason());
        log.trace("Stack trace completo: ", ex);

        return new ResponseEntity<>(
                APIResponse.fail(ex.getReason(), ex.getStatusCode().value()),
                ex.getStatusCode()
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<APIResponse<Object>> handle(MethodArgumentNotValidException ex){
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult()
                .getFieldErrors()
                .forEach(er ->
                        errors.put(er.getField(), er.getDefaultMessage())
                );

        return ResponseEntity.badRequest().body(
                APIResponse.fail(
                        errors,
                        "Errore nell validazione dei dati",
                        ex.getStatusCode().value(),
                        ex.getErrorCount()
                )
        );
    }
}
