package com.example.animeservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Response containing log file status information")
public class LogStatusResponse {

    @Schema(description = "Unique identifier of the log file",
            example = "550e8400-e29b-41d4-a716-446655440000")
    private String logId;

    @Schema(
            description = "Current status of the log file",
            example = "IN_PROGRESS",
            allowableValues = {"IN_PROGRESS", "COMPLETED", "FAILED"}
    )
    private String status;
}