package com.example.animeservice.controller;

import com.example.animeservice.dto.CollectionDto;
import com.example.animeservice.dto.CollectionWithAnimeDto;
import com.example.animeservice.service.CollectionService;
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
@RequestMapping("/api/collections")
@RequiredArgsConstructor
public class CollectionController {
    private final CollectionService collectionService;

    @GetMapping
    public ResponseEntity<List<CollectionDto>> getAllCollections() {
        return ResponseEntity.ok(collectionService.getAllCollections());
    }

    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollectionById(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(@Valid @RequestBody CollectionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.createCollection(dto));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody CollectionDto dto
    ) {
        return ResponseEntity.ok(collectionService.updateCollection(id, dto));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CollectionDto>> getCollectionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(collectionService.getCollectionsByUser(userId));
    }

    @GetMapping("/search")
    public ResponseEntity<List<CollectionDto>> searchCollections(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) Long animeId
    ) {
        return ResponseEntity.ok(collectionService.searchCollections(name, animeId));
    }

    @GetMapping("/search/anime")
    public ResponseEntity<List<CollectionWithAnimeDto>> searchCollectionsWithAnime(
        @RequestParam(required = false) String title,
        @RequestParam(required = false) String genre,
        @RequestParam(required = false) Integer releaseYear
    ) {
        List<CollectionWithAnimeDto> list =
                collectionService.searchCollectionsByAnimeParams(title, genre, releaseYear);
        return ResponseEntity.ok(list);
    }
}