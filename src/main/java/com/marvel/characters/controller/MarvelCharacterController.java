package com.marvel.characters.controller;

import com.marvel.characters.model.MarvelCharacter;
import com.marvel.characters.service.MarvelCharacterService;
import jakarta.validation.Valid;
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

import java.util.List;
import java.util.Optional;

/**
 * REST controller for Marvel Comics characters.
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
     * Optional query params:
     *   ?universe=Earth-616  — filter by universe (400 if blank/invalid)
     *   ?sortBy=name         — sort by a supported field (400 if unsupported)
     */
    @GetMapping
    public ResponseEntity<List<MarvelCharacter>> getAll(
            @RequestParam(required = false) String universe,
            @RequestParam(required = false) String sortBy) {

        List<MarvelCharacter> results;

        if (universe != null) {
            results = service.filterByUniverse(universe);
        } else if (sortBy != null) {
            results = service.sortBy(sortBy);
        } else {
            results = service.getAllActive();
        }

        return ResponseEntity.ok(results);
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

    /**
     * POST /api/characters — create a new character from validated JSON.
     * Returns 201 Created on success, 400 Bad Request when validation fails.
     */
    @PostMapping
    public ResponseEntity<MarvelCharacter> create(@Valid @RequestBody MarvelCharacter character) {
        character.setId(null);
        character.setDeleted(false);
        MarvelCharacter saved = service.save(character);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    /**
     * DELETE /api/characters/{id} — logical delete (sets deleted=true).
     * Does not physically remove the row. Returns 200 or 404.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<MarvelCharacter> delete(@PathVariable Long id) {
        Optional<MarvelCharacter> optional = service.logicalDelete(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        }
        return ResponseEntity.notFound().build();
    }
}
