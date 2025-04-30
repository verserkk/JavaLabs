package com.example.animeservice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CollectionDto {
    private Long id;

    @NotBlank(message = "Collection name is required")
    private String name;

    @NotNull(message = "User ID is required")
    private Long userId;

    private List<Long> animeIds;
}