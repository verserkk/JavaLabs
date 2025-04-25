package com.example.animeservice.dto;

import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.Data;

@Data
public class UserWithCollectionsDto {
    private Long id;

    @NotNull(message = "Username is required")
    private String username;

    @NotNull(message = "Email is required")
    private String email;
    private List<CollectionWithAnimeDto> collections;
}