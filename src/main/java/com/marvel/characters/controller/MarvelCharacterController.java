package com.marvel.characters.controller;

import com.marvel.characters.service.MarvelCharacterService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
