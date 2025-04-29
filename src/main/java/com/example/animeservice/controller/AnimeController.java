package com.example.animeservice.controller;

import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.dto.ErrorResponse;
import com.example.animeservice.service.AnimeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Anime", description = "API for managing anime entities")
public class AnimeController {

    private final AnimeService animeService;

    @Operation(summary = "Get all anime", description = "Retrieves a list of all anime.")
    @ApiResponses(value = {@ApiResponse
            (responseCode = "200", description = "List of anime retrieved", content =
            @Content(schema = @Schema(implementation = AnimeDto.class))),
                              @ApiResponse(responseCode = "500",
                                      description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<AnimeDto>> getAllAnimes() {
        return ResponseEntity.ok(animeService.getAllAnimes());
    }

    @Operation(summary = "Get anime by ID", description = "Retrieves an anime by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Anime retrieved",
                    content = @Content(schema = @Schema(implementation = AnimeDto.class))),
                           @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<AnimeDto> getAnimeById(@PathVariable Long id) {
        return ResponseEntity.ok(animeService.getAnimeById(id));
    }

    @Operation(summary = "Create a new anime", description = "Creates a new anime entity.")
    @ApiResponses(value = {@ApiResponse(responseCode = "201", description = "Anime created",
                    content = @Content(schema = @Schema(implementation = AnimeDto.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<AnimeDto> createAnime(@RequestBody AnimeDto dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(animeService.createAnime(dto));
    }

    @Operation(summary = "Update an anime", description = "Updates an existing anime by its ID.")
    @ApiResponses(value = { @ApiResponse(responseCode = "200", description = "Anime updated",
                    content = @Content(schema = @Schema(implementation = AnimeDto.class))),
                            @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                            @ApiResponse(responseCode = "400", description = "Invalid input",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<AnimeDto> updateAnime(@PathVariable Long id, @RequestBody AnimeDto dto) {
        return ResponseEntity.ok(animeService.updateAnime(id, dto));
    }

    @Operation(summary = "Delete an anime", description = "Deletes an anime by its ID.")
    @ApiResponses(value = {@ApiResponse(responseCode = "204", description = "Anime deleted"),
                           @ApiResponse(responseCode = "404", description = "Anime not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteAnime(@PathVariable Long id) {
        animeService.deleteAnime(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Search anime", description =
            "Searches anime by title, genre, or release year.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Anime search results",
                    content = @Content(schema = @Schema(implementation = AnimeDto.class))),
                           @ApiResponse(responseCode = "400",
                                   description = "Invalid search parameters",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/search")
    public ResponseEntity<List<AnimeDto>> searchAnimes(
            @Parameter(description = "Anime title to search for (case-insensitive)")
            @RequestParam(required = false) String title,
            @Parameter(description = "Anime genre to filter by")
            @RequestParam(required = false) String genre,
            @Parameter(description = "Anime release year to filter by")
            @RequestParam(required = false) Integer year
    ) {
        return ResponseEntity.ok(animeService.searchAnimes(title, genre, year));
    }
}