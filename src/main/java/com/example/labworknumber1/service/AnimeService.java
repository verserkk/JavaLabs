package com.example.labworknumber1.service;

import com.example.labworknumber1.model.AnimeSeries;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;



@Service
public class AnimeService {
    private final Map<Integer, AnimeSeries> animeDatabase = Map.of(
            1, new AnimeSeries("Naruto", 1),
            2, new AnimeSeries("One Piece", 2),
            3, new AnimeSeries("Attack on Titan", 3),
            4, new AnimeSeries("Naruto", 4)
    );

    public AnimeSeries createAnimeSeries(String name, Integer id) {
        return new AnimeSeries(name, id);
    }

    public AnimeSeries getAnimeById(Integer id) {
        if (!animeDatabase.containsKey(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Anime not found");
        }
        return animeDatabase.get(id);
    }

    public AnimeSeries getAnimeByName(String name) {
        return animeDatabase.values().stream()
                .filter(anime -> anime.getName().equalsIgnoreCase(name))
                .findAny()
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                        "Anime not found"));
    }
}
