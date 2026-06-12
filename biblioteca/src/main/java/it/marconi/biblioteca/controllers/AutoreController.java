package it.marconi.biblioteca.controllers;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import io.swagger.v3.oas.annotations.Operation;
import it.marconi.biblioteca.domain.AutoreDTO;
import it.marconi.biblioteca.domain.LibroDTO;
import it.marconi.biblioteca.domain.response.APIResponse;
import it.marconi.biblioteca.services.AutoreService;
import it.marconi.biblioteca.services.LibroService;
import jakarta.validation.Valid;

@RestController
@RequestMapping("/autori")
public class AutoreController {
    
    @Autowired
    LibroService libroService;

    @Autowired
    AutoreService autoreService;

    @GetMapping
    @Operation(summary = "Recupera tutti gli autori")
    public APIResponse<List<AutoreDTO>> getAll() {
        List<AutoreDTO> autori = autoreService.findAll();
        return APIResponse.successCollection(autori);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Cerca un autore dato il suo ID")
    public ResponseEntity<AutoreDTO> getAutore(@PathVariable Integer id) {

        Optional<AutoreDTO> autore = autoreService.getById(id);

        if (autore.isPresent())
            return ResponseEntity.of(autore);
        else
            return ResponseEntity.notFound().build();

        // return autoreService.getById(id).
        //     orElseThrow(() -> new RuntimeException("Autore non trovato"));
    }

    @GetMapping("/{id}/libri")
    @Operation(summary = "Recupera tutti i libri di un dato autore")
    public List<LibroDTO> getLibriByAutore(@PathVariable Integer id) {
        
        return libroService.getByAutoreId(id);
    }

    @PostMapping("/add")
    @Operation(summary = "Aggiunge un nuovo autore")
    public ResponseEntity<APIResponse<AutoreDTO>> addAutore(@Valid @RequestBody AutoreDTO autore) {

        AutoreDTO salvato = autoreService.save(autore);

        return ResponseEntity.ok(APIResponse.success(salvato));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Rimuove un autore dal database, e anche tutti i suoi libri")
    public ResponseEntity<APIResponse<String>> deleteAutore(@PathVariable Integer id) {

        boolean deleted = autoreService.deleteById(id);

        if (deleted)
            return ResponseEntity.ok(APIResponse.success("Autore eliminato correttamente"));
        else
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Autore non trovato");
    }
}
