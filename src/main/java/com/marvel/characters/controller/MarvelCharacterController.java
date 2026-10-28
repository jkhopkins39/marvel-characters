package com.marvel.characters.controller;

import com.marvel.characters.exception.ResourceNotFoundException;
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
 * Uses constructor injection and ResponseEntity for JSON responses with status codes.
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
     * Optional query params (combinable):
     *   ?universe=Earth-616
     *   ?firstAppearanceYear=1966
     *   ?beforeYear=1980
     *   ?afterYear=1960
     *   ?sortBy=name
     */
    @GetMapping
    public ResponseEntity<List<MarvelCharacter>> getAll(
            @RequestParam(required = false) String universe,
            @RequestParam(required = false) Integer firstAppearanceYear,
            @RequestParam(required = false) Integer beforeYear,
            @RequestParam(required = false) Integer afterYear,
            @RequestParam(required = false) String sortBy) {

        boolean hasFilters = universe != null
                || firstAppearanceYear != null
                || beforeYear != null
                || afterYear != null
                || sortBy != null;

        List<MarvelCharacter> results = hasFilters
                ? service.search(universe, firstAppearanceYear, beforeYear, afterYear, sortBy)
                : service.getAllActive();

        return ResponseEntity.ok(results);
    }

    /**
     * GET /api/characters/{id} — find one active character by ID.
     * Uses @PathVariable, findById(), and Optional. Returns 200 or 404.
     */
    @GetMapping("/{id}")
    public ResponseEntity<MarvelCharacter> getById(@PathVariable Long id) {
        Optional<MarvelCharacter> optional = service.findById(id);
        if (optional.isPresent()) {
            return ResponseEntity.ok(optional.get());
        }
        throw new ResourceNotFoundException("Character not found with id: " + id);
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
        return ResponseEntity.ok(service.logicalDelete(id));
    }
}
