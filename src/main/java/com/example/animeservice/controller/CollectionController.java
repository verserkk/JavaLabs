package com.example.animeservice.controller;

import com.example.animeservice.dto.CollectionDto;
import com.example.animeservice.dto.CollectionWithAnimeDto;
import com.example.animeservice.dto.ErrorResponse;
import com.example.animeservice.service.CollectionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Collection", description = "API for managing collection entities")
public class CollectionController {
    private final CollectionService collectionService;

    @Operation(summary = "Get all collections",
            description = "Retrieves a list of all collections.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "List of collections retrieved",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<CollectionDto>> getAllCollections() {
        return ResponseEntity.ok(collectionService.getAllCollections());
    }

    @Operation(summary = "Get collection by ID", description = "Retrieves a collection by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Collection retrieved",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "404", description = "Collection not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<CollectionDto> getCollectionById(@PathVariable Long id) {
        return ResponseEntity.ok(collectionService.getCollectionById(id));
    }

    @Operation(summary = "Create a new collection",
            description = "Creates a new collection entity.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Collection created",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<CollectionDto> createCollection(@Valid @RequestBody CollectionDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.createCollection(dto));
    }

    @Operation(summary = "Update a collection",
            description = "Updates an existing collection by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Collection updated",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "404", description = "Collection not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<CollectionDto> updateCollection(
            @PathVariable Long id,
            @Valid @RequestBody CollectionDto dto
    ) {
        return ResponseEntity.ok(collectionService.updateCollection(id, dto));
    }

    @Operation(summary = "Delete a collection", description = "Deletes a collection by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Collection deleted"),
                           @ApiResponse(responseCode = "404", description = "Collection not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCollection(@PathVariable Long id) {
        collectionService.deleteCollection(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Get collections by user ID",
            description = "Retrieves all collections for a specific user.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "List of collections retrieved",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<CollectionDto>> getCollectionsByUser(@PathVariable Long userId) {
        return ResponseEntity.ok(collectionService.getCollectionsByUser(userId));
    }

    @Operation(summary = "Search collections",
            description = "Searches collections by name or anime ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Collection search results",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "400",
                                   description = "Invalid search parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "404", description = "No collections found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<CollectionDto>> searchCollections(
            @Parameter(description = "Collection name to search for (case-insensitive)")
            @RequestParam(required = false) String name,
            @Parameter(description = "Anime ID to filter collections")
            @RequestParam(required = false) Long animeId
    ) {
        return ResponseEntity.ok(collectionService.searchCollections(name, animeId));
    }

    @Operation(summary = "Search collections by anime parameters",
            description = "Searches collections containing anime with "
                    + "specified title, genre, or release year.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Collection search results",
                    content = @Content(schema =
                    @Schema(implementation = CollectionWithAnimeDto.class))),
                           @ApiResponse(responseCode = "400",
                                   description = "Invalid search parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "404", description = "No collections found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search/anime")
    public ResponseEntity<List<CollectionWithAnimeDto>> searchCollectionsWithAnime(
            @Parameter(description = "Anime title to search for (case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Anime genre to filter by")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Anime release year to filter by")
            @RequestParam(required = false) Integer releaseYear
    ) {
        List<CollectionWithAnimeDto> list =
                collectionService.searchCollectionsByAnimeParams(title, genre, releaseYear);
        return ResponseEntity.ok(list);
    }

    @Operation(summary = "Create multiple collections",
            description = "Creates multiple collection entities in a single request.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201",
                                    description = "Collections list created",
                    content = @Content(schema = @Schema(implementation = CollectionDto.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "404",
                                   description = "User or anime not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping("/bulk")
    public ResponseEntity<List<CollectionDto>> createCollections(
            @Valid @RequestBody List<CollectionDto> dtos) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(collectionService.createCollections(dtos));
    }
}