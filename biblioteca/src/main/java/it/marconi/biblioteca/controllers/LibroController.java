package it.marconi.biblioteca.controllers;

import java.util.List;
import java.util.Optional;

import it.marconi.biblioteca.domain.response.APIResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import io.swagger.v3.oas.annotations.Operation;
import it.marconi.biblioteca.domain.LibroDTO;
import it.marconi.biblioteca.services.LibroService;
import jakarta.validation.Valid;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/libri")
public class LibroController {

    private final LibroService libroService;

    @Autowired
    public LibroController(LibroService libroService) {
        this.libroService = libroService;
    }


    @GetMapping
    @Operation(summary = "Recupera la lista di tutti i libri")
    public APIResponse<List<LibroDTO>> getAll() {
        //return libroService.findAll();
        List<LibroDTO> listaLibri = libroService.findAll();
        return APIResponse.successCollection(listaLibri);
    }

    @GetMapping("/{isbn}")
    @Operation(summary = "Cerca un libro dal sui ISBN")
    public APIResponse<LibroDTO> getLibroByIsbn(@PathVariable String isbn) {
        Optional<LibroDTO> libro = libroService.getByIsbn(isbn);

        return libro.map(APIResponse::success)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Libro non trovato per ISBN"
                        )
                );
    }

    @GetMapping("/libro")
    @Operation(summary = "Cerca un libro per titolo esatto")
    public APIResponse<LibroDTO> getLibroByTitolo(@RequestParam("titolo") String titolo){
        Optional<LibroDTO> libro = libroService.getByTitolo(titolo);
        return libro.map(APIResponse::success)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND,
                                "Libro non trovato per titolo"
                        )
                );
    }

    @PostMapping("/add")
    @Operation(summary = "Aggiunge un nuovo libro, dato l'autore")
    public ResponseEntity<APIResponse<LibroDTO>> addLibro(@Valid @RequestBody LibroDTO libro) {
        
        Optional<LibroDTO> libroSalvato = libroService.save(libro);
        
        if (libroSalvato.isPresent()){
            LibroDTO datiLibro = libroSalvato.get();
            APIResponse<LibroDTO> risposta = APIResponse.success(datiLibro);

            return ResponseEntity.ok(risposta);
        }else{
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossibile aggiungere il libro. Autore non trovato.");
        }
    }

    @DeleteMapping("/{isbn}")
    @Operation(summary = "Elimina un libro dato il suo ISBN")
    public ResponseEntity<APIResponse<String>> deleteLibro(@PathVariable String isbn) {

        boolean deleted = libroService.deleteByIsbn(isbn);

        if (deleted){
            APIResponse<String> risposta = APIResponse.success("Libro eliminato con successo.");

            return ResponseEntity.ok(risposta);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Impossibile eliminare il libro. Libro non trovato");
        }
    }

    @GetMapping("/stress-test-500")
    public void provocaErrore500() {
        throw new RuntimeException("Simulazione guasto controllato per difesa orale esame UF13");
    }

}
