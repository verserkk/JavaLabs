package com.example.animeservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class CollectionWithAnimeDto {
    private Long id;

    @NotNull(message = "Name is required")
    private String name;
    private List<AnimeDto> animes;
}

