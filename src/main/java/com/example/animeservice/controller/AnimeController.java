package com.example.animeservice.controller;

import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.service.AnimeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;



@RestController
@RequestMapping("/api/animes")
@RequiredArgsConstructor
public class AnimeController {
    private final AnimeService animeService;

    @GetMapping
    public ResponseEntity<List<AnimeDto>> getAllAnimes() {
        return ResponseEntity.ok(animeService.getAllAnimes());
    }

    @GetMapping("/{id}")
    public ResponseEntity<AnimeDto> getAnimeById(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.getAnimeById(id));
    }

    @PostMapping
    public ResponseEntity<AnimeDto> createAnime(@Valid @RequestBody AnimeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.createAnime(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<AnimeDto> updateAnime(
            @PathVariable Long id,
            @Valid @RequestBody AnimeDto dto
    ) {
        return ResponseEntity.ok(animeService.updateAnime(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnime(@PathVariable Long id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/search")
    public ResponseEntity<List<AnimeDto>> searchAnimes(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String genre,
            @RequestParam(required = false) Integer year
    ) {
        List<AnimeDto> list = animeService.searchAnimes(title, genre, year);
        if (list.isEmpty()) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(list);
    }
}