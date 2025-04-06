package com.example.animeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AnimeDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String genre;

    @NotNull(message = "Release year is required")
    private Integer releaseYear;
}