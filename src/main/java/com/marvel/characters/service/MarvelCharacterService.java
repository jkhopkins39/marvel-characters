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
import java.util.stream.Stream;

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
     * Filters and optionally sorts active characters.
     * Supported filters (combinable):
     *   universe — exact match (case-insensitive)
     *   firstAppearanceYear — exact year
     *   beforeYear — firstAppearanceYear &lt; year
     *   afterYear — firstAppearanceYear &gt; year
     * Optional sortBy uses the same supported sort fields as sortBy(...).
     */
    public List<MarvelCharacter> search(
            String universe,
            Integer firstAppearanceYear,
            Integer beforeYear,
            Integer afterYear,
            String sortBy) {

        if (universe != null && universe.isBlank()) {
            throw new InvalidRequestException("Universe filter must not be blank");
        }
        if (beforeYear != null && afterYear != null && beforeYear <= afterYear) {
            throw new InvalidRequestException(
                    "beforeYear must be greater than afterYear when both are provided");
        }

        Stream<MarvelCharacter> stream = getAllActive().stream();

        if (universe != null) {
            String target = universe.trim().toLowerCase(Locale.ROOT);
            stream = stream.filter(c -> c.getUniverse() != null
                    && c.getUniverse().toLowerCase(Locale.ROOT).equals(target));
        }

        if (firstAppearanceYear != null) {
            stream = stream.filter(c -> firstAppearanceYear.equals(c.getFirstAppearanceYear()));
        }

        if (beforeYear != null) {
            stream = stream.filter(c -> c.getFirstAppearanceYear() != null
                    && c.getFirstAppearanceYear() < beforeYear);
        }

        if (afterYear != null) {
            stream = stream.filter(c -> c.getFirstAppearanceYear() != null
                    && c.getFirstAppearanceYear() > afterYear);
        }

        if (sortBy != null) {
            stream = stream.sorted(comparatorFor(sortBy));
        }

        return stream.collect(Collectors.toList());
    }

    /**
     * Filters active characters by universe (case-insensitive).
     * Throws InvalidRequestException when the universe value is blank.
     */
    public List<MarvelCharacter> filterByUniverse(String universe) {
        return search(universe, null, null, null, null);
    }

    /**
     * Sorts active characters by a supported field using a Comparator and stream.
     * Supported fields: name, alias, power, universe, firstAppearanceYear
     */
    public List<MarvelCharacter> sortBy(String field) {
        return search(null, null, null, null, field);
    }

    private Comparator<MarvelCharacter> comparatorFor(String field) {
        if (field == null || field.isBlank()) {
            throw new InvalidRequestException("Sort field must not be blank");
        }

        switch (field.trim().toLowerCase(Locale.ROOT)) {
            case "name":
                return Comparator.comparing(MarvelCharacter::getName, String.CASE_INSENSITIVE_ORDER);
            case "alias":
                return Comparator.comparing(MarvelCharacter::getAlias, String.CASE_INSENSITIVE_ORDER);
            case "power":
                return Comparator.comparing(MarvelCharacter::getPower, String.CASE_INSENSITIVE_ORDER);
            case "universe":
                return Comparator.comparing(MarvelCharacter::getUniverse, String.CASE_INSENSITIVE_ORDER);
            case "firstappearanceyear":
            case "first_appearance_year":
                return Comparator.comparing(MarvelCharacter::getFirstAppearanceYear);
            default:
                throw new InvalidRequestException(
                        "Unsupported sort field: " + field
                                + ". Supported fields: name, alias, power, universe, firstAppearanceYear");
        }
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
