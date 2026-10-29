package com.marvel.characters.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * JPA entity representing a Marvel Comics character.
 * Uses a generated ID, validated fields, an optional team, and a deleted flag for logical deletion.
 */
@Entity
@Table(name = "marvel_characters")
public class MarvelCharacter {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 100, message = "Name must be between 2 and 100 characters")
    @Column(nullable = false, length = 100)
    private String name;

    @NotBlank(message = "Alias is required")
    @Size(min = 1, max = 100, message = "Alias must be between 1 and 100 characters")
    @Column(nullable = false, length = 100)
    private String alias;

    @NotBlank(message = "Power is required")
    @Size(min = 2, max = 200, message = "Power must be between 2 and 200 characters")
    @Column(nullable = false, length = 200)
    private String power;

    @NotBlank(message = "Universe is required")
    @Column(nullable = false, length = 50)
    private String universe;

    /**
     * Optional team affiliation (e.g. X-Men, Avengers, Fantastic Four).
     * Null or blank means the character is unaffiliated / solo.
     */
    @Size(max = 100, message = "Team must be at most 100 characters")
    @Column(length = 100)
    private String team;

    @NotNull(message = "First appearance year is required")
    @Min(value = 1939, message = "First appearance year must be 1939 or later")
    @Max(value = 2100, message = "First appearance year must be 2100 or earlier")
    @Column(name = "first_appearance_year", nullable = false)
    private Integer firstAppearanceYear;

    @Column(nullable = false)
    private boolean deleted = false;

    public MarvelCharacter() {
    }

    public MarvelCharacter(String name, String alias, String power, String universe,
                           Integer firstAppearanceYear, String team) {
        this.name = name;
        this.alias = alias;
        this.power = power;
        this.universe = universe;
        this.firstAppearanceYear = firstAppearanceYear;
        this.team = team;
        this.deleted = false;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public String getPower() {
        return power;
    }

    public void setPower(String power) {
        this.power = power;
    }

    public String getUniverse() {
        return universe;
    }

    public void setUniverse(String universe) {
        this.universe = universe;
    }

    public String getTeam() {
        return team;
    }

    public void setTeam(String team) {
        this.team = team;
    }

    public Integer getFirstAppearanceYear() {
        return firstAppearanceYear;
    }

    public void setFirstAppearanceYear(Integer firstAppearanceYear) {
        this.firstAppearanceYear = firstAppearanceYear;
    }

    public boolean isDeleted() {
        return deleted;
    }

    public void setDeleted(boolean deleted) {
        this.deleted = deleted;
    }
}
