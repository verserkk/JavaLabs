package com.example.labworknumber1.controller;

import com.example.labworknumber1.model.AnimeSeries;
import com.example.labworknumber1.service.AnimeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnimeController {

    private final AnimeService animeService;

    public AnimeController(AnimeService animeService) {
        this.animeService = animeService;
    }

    @GetMapping("/anime")
    public AnimeSeries getQueryAnime(@RequestParam("name") String name) {
        return animeService.getAnimeByName(name);
    }

    @GetMapping("/anime/{animeId}")
    public AnimeSeries getPathAnime(@PathVariable("animeId") Integer animeId) {
        return animeService.getAnimeById(animeId);
    }
}
