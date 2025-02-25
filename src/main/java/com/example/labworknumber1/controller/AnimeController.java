package com.example.labworknumber1.controller;

import com.example.labworknumber1.model.AnimeSeries;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class AnimeController {

    @GetMapping("/anime")
    public AnimeSeries getQueryAnime(@RequestParam("name") String name,
                                     @RequestParam("id") Integer id) {
        return new AnimeSeries(name, id);
    }

    @GetMapping("/anime/{animeId}")
    public AnimeSeries getPathAnime(@PathVariable("animeId") Integer animeId,
                                    @RequestParam("name") String name) {
        return new AnimeSeries(name, animeId);
    }
}
