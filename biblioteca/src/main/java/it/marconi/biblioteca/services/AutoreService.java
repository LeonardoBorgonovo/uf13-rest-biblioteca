package it.marconi.biblioteca.services;

import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import it.marconi.biblioteca.domain.Autore;
import it.marconi.biblioteca.domain.AutoreDTO;
import it.marconi.biblioteca.domain.AutoreMapper;
import it.marconi.biblioteca.repositories.AutoreRepository;

@Service
public class AutoreService {
    
    // dependency injection
    private final AutoreRepository autoreRepo;

    private final AutoreMapper mapper;

    @Autowired
    public AutoreService(AutoreRepository autoreRepo, AutoreMapper mapper) {
        this.autoreRepo = autoreRepo;
        this.mapper = mapper;
    }

    public AutoreDTO save(AutoreDTO autore) {

        Autore entity = mapper.toEntity(autore);
        entity.setId(null);     // cancello ID dalle API
        entity = autoreRepo.save(entity);
        return mapper.toDto(entity);
    }

    public List<AutoreDTO> findAll() {
                                                    // mapper::toDto
        return autoreRepo.findAll().stream().map(autore -> mapper.toDto(autore)).toList();
    }

    public Optional<AutoreDTO> getById(int id) {

        return autoreRepo.findById(id).map(autore -> mapper.toDto(autore));
    }

    public boolean deleteById(int id) {
        if (autoreRepo.existsById(id)) {
            autoreRepo.deleteById(id);
            return true;
        }
        return false;
    }
}
