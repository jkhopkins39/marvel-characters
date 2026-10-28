package com.marvel.characters.repository;

import com.marvel.characters.model.MarvelCharacter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * Spring Data JPA repository for MarvelCharacter entities.
 * Provides findAll(), findById(), and save() via JpaRepository.
 */
@Repository
public interface MarvelCharacterRepository extends JpaRepository<MarvelCharacter, Long> {
}
