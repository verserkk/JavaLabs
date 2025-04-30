package com.example.animeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
public class AnimeDto {
    private Long id;

    @NotBlank(message = "Title is required")
    private String title;

    private String genre;

    @NotNull(message = "Release year is required")
    private Integer releaseYear;
}