package com.example.animeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing log file ID after creation initiation")
public class LogCreateResponse {

    @Schema(description = "Unique identifier of the log file",
            example = "550e8400-e29b-41d4-a716-4466554400003")
    private String logId;
}