package com.marvel.characters.config;

import com.marvel.characters.model.MarvelCharacter;
import com.marvel.characters.repository.MarvelCharacterRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

/**
 * Loads starter Marvel Comics characters into the H2 database on startup.
 */
@Component
public class DataLoader implements CommandLineRunner {

    private final MarvelCharacterRepository repository;

    public DataLoader(MarvelCharacterRepository repository) {
        this.repository = repository;
    }

    @Override
    public void run(String... args) {
        if (repository.count() > 0) {
            return;
        }

        repository.save(new MarvelCharacter(
                "Spider-Man", "Peter Parker", "Spider-sense and wall-crawling", "Earth-616", 1962));
        repository.save(new MarvelCharacter(
                "Iron Man", "Tony Stark", "Powered armor suit", "Earth-616", 1963));
        repository.save(new MarvelCharacter(
                "Black Panther", "T'Challa", "Enhanced strength and vibranium suit", "Earth-616", 1966));
        repository.save(new MarvelCharacter(
                "Captain Marvel", "Carol Danvers", "Energy absorption and flight", "Earth-616", 1968));
        repository.save(new MarvelCharacter(
                "Wolverine", "Logan", "Regeneration and adamantium claws", "Earth-616", 1974));
        repository.save(new MarvelCharacter(
                "Scarlet Witch", "Wanda Maximoff", "Chaos magic and reality warping", "Earth-616", 1964));
        repository.save(new MarvelCharacter(
                "Doctor Strange", "Stephen Strange", "Mystic arts and astral projection", "Earth-616", 1963));
        repository.save(new MarvelCharacter(
                "Miles Morales", "Miles Morales", "Venom blast and camouflage", "Earth-1610", 2011));
    }
}
