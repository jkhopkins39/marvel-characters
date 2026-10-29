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
                "Spider-Man", "Peter Parker", "Spider-sense and wall-crawling",
                "Earth-616", 1962, "Avengers"));
        repository.save(new MarvelCharacter(
                "Iron Man", "Tony Stark", "Powered armor suit",
                "Earth-616", 1963, "Avengers"));
        repository.save(new MarvelCharacter(
                "Black Panther", "T'Challa", "Enhanced strength and vibranium suit",
                "Earth-616", 1966, "Avengers"));
        repository.save(new MarvelCharacter(
                "Captain Marvel", "Carol Danvers", "Energy absorption and flight",
                "Earth-616", 1968, "Avengers"));
        repository.save(new MarvelCharacter(
                "Wolverine", "Logan", "Regeneration and adamantium claws",
                "Earth-616", 1974, "X-Men"));
        repository.save(new MarvelCharacter(
                "Scarlet Witch", "Wanda Maximoff", "Chaos magic and reality warping",
                "Earth-616", 1964, "Avengers"));
        repository.save(new MarvelCharacter(
                "Doctor Strange", "Stephen Strange", "Mystic arts and astral projection",
                "Earth-616", 1963, null));
        repository.save(new MarvelCharacter(
                "Miles Morales", "Miles Morales", "Venom blast and camouflage",
                "Earth-1610", 2011, null));
        repository.save(new MarvelCharacter(
                "Cyclops", "Scott Summers", "Optic blasts",
                "Earth-616", 1963, "X-Men"));
        repository.save(new MarvelCharacter(
                "Jean Grey", "Jean Grey", "Telepathy and telekinesis",
                "Earth-616", 1963, "X-Men"));
        repository.save(new MarvelCharacter(
                "Mister Fantastic", "Reed Richards", "Elasticity and genius intellect",
                "Earth-616", 1961, "Fantastic Four"));
        repository.save(new MarvelCharacter(
                "Invisible Woman", "Sue Storm", "Invisibility and force fields",
                "Earth-616", 1961, "Fantastic Four"));
    }
}
