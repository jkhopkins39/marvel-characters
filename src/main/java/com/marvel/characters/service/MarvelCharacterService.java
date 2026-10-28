package com.marvel.characters.service;

import com.marvel.characters.exception.InvalidRequestException;
import com.marvel.characters.exception.ResourceNotFoundException;
import com.marvel.characters.model.MarvelCharacter;
import com.marvel.characters.repository.MarvelCharacterRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Business logic for Marvel characters.
 * Uses constructor injection; filters out logically deleted records.
 */
@Service
public class MarvelCharacterService {

    private final MarvelCharacterRepository repository;

    public MarvelCharacterService(MarvelCharacterRepository repository) {
        this.repository = repository;
    }

    /**
     * Returns all active (not deleted) characters.
     */
    public List<MarvelCharacter> getAllActive() {
        List<MarvelCharacter> active = new ArrayList<>();
        for (MarvelCharacter character : repository.findAll()) {
            if (!character.isDeleted()) {
                active.add(character);
            }
        }
        return active;
    }

    /**
     * Finds an active character by ID.
     */
    public Optional<MarvelCharacter> findById(Long id) {
        Optional<MarvelCharacter> optional = repository.findById(id);
        if (optional.isPresent() && optional.get().isDeleted()) {
            return Optional.empty();
        }
        return optional;
    }

    /**
     * Filters active characters by universe (case-insensitive).
     * Throws InvalidRequestException when the universe value is blank.
     */
    public List<MarvelCharacter> filterByUniverse(String universe) {
        if (universe == null || universe.isBlank()) {
            throw new InvalidRequestException("Universe filter must not be blank");
        }
        String target = universe.trim().toLowerCase(Locale.ROOT);
        return getAllActive().stream()
                .filter(c -> c.getUniverse() != null
                        && c.getUniverse().toLowerCase(Locale.ROOT).equals(target))
                .collect(Collectors.toList());
    }

    /**
     * Sorts active characters by a supported field using a Comparator and stream.
     * Supported fields: name, alias, power, universe, firstAppearanceYear
     */
    public List<MarvelCharacter> sortBy(String field) {
        if (field == null || field.isBlank()) {
            throw new InvalidRequestException("Sort field must not be blank");
        }

        Comparator<MarvelCharacter> comparator;
        switch (field.trim().toLowerCase(Locale.ROOT)) {
            case "name":
                comparator = Comparator.comparing(MarvelCharacter::getName, String.CASE_INSENSITIVE_ORDER);
                break;
            case "alias":
                comparator = Comparator.comparing(MarvelCharacter::getAlias, String.CASE_INSENSITIVE_ORDER);
                break;
            case "power":
                comparator = Comparator.comparing(MarvelCharacter::getPower, String.CASE_INSENSITIVE_ORDER);
                break;
            case "universe":
                comparator = Comparator.comparing(MarvelCharacter::getUniverse, String.CASE_INSENSITIVE_ORDER);
                break;
            case "firstappearanceyear":
            case "first_appearance_year":
                comparator = Comparator.comparing(MarvelCharacter::getFirstAppearanceYear);
                break;
            default:
                throw new InvalidRequestException(
                        "Unsupported sort field: " + field
                                + ". Supported fields: name, alias, power, universe, firstAppearanceYear");
        }

        return getAllActive().stream()
                .sorted(comparator)
                .collect(Collectors.toList());
    }

    /**
     * Saves a new or updated character.
     */
    public MarvelCharacter save(MarvelCharacter character) {
        return repository.save(character);
    }

    /**
     * Logically deletes a character by setting deleted to true.
     * Throws ResourceNotFoundException if the character does not exist or is already deleted.
     */
    public MarvelCharacter logicalDelete(Long id) {
        Optional<MarvelCharacter> optional = repository.findById(id);
        if (optional.isEmpty() || optional.get().isDeleted()) {
            throw new ResourceNotFoundException("Character not found with id: " + id);
        }
        MarvelCharacter character = optional.get();
        character.setDeleted(true);
        return repository.save(character);
    }

    /**
     * Returns an active character by ID or throws ResourceNotFoundException.
     */
    public MarvelCharacter getByIdOrThrow(Long id) {
        return findById(id).orElseThrow(
                () -> new ResourceNotFoundException("Character not found with id: " + id));
    }
}
