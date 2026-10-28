package com.marvel.characters.controller;

import com.marvel.characters.model.MarvelCharacter;
import com.marvel.characters.service.MarvelCharacterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

/**
 * REST controller for Marvel Comics characters.
 * Endpoints are added incrementally as features are implemented.
 */
@RestController
@RequestMapping("/api/characters")
public class MarvelCharacterController {

    private final MarvelCharacterService service;

    public MarvelCharacterController(MarvelCharacterService service) {
        this.service = service;
    }

    /**
     * GET /api/characters — returns all active (not deleted) characters.
     */
    @GetMapping
    public ResponseEntity<List<MarvelCharacter>> getAll() {
        return ResponseEntity.ok(service.getAllActive());
    }

    /**
     * GET /api/characters/{id} — find one active character by ID.
     * Returns 200 when found, 404 when missing or logically deleted.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarvelCharacter> getById(@PathVariable Long id) {
        Optional<MarvelCharacter> optional = service.findById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        }
        return ResponseEntity.notFound().build();
    }
}
