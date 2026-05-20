package it.marconi.biblioteca.controllers.exception;

import it.marconi.biblioteca.domain.response.APIResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<APIResponse<Object>> handle(Exception ex){
        return ResponseEntity.internalServerError().body(
                APIResponse.error(
                        ex.getMessage(),
                        500
                )
        );
    }

    @ExceptionHandler(ResponseStatusException.class)
    public ResponseEntity<APIResponse<Object>> handle(ResponseStatusException ex){
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
