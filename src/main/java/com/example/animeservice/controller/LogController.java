package com.example.animeservice.controller;

import com.example.animeservice.dto.ErrorResponse;
import com.example.animeservice.exception.EntityNotFoundException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/logs")
@Slf4j
@Tag(name = "Logs", description = "API for retrieving and downloading application logs")
public class LogController {

    private static final String LOG_PATH = "logs";
    private static final String LOG_FILE_PREFIX = "app-";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String MAIN_LOG_FILE = LOG_PATH + "/app.log";

    @Operation(summary = "Get logs by date",
            description = "Retrieves log entries from "
                    + "the application log file for the specified date.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Logs retrieved successfully",
                    content = @Content(schema = @Schema(implementation = String.class))),
                           @ApiResponse(responseCode = "400", description = "Invalid date format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "404", description = "Log file not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping
    public ResponseEntity<List<String>> getLogsByDate(
            @Parameter(description = "Date for which to retrieve logs (format: yyyy-MM-dd)")
            @RequestParam("date") String date
    ) {
        try {
            LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            String fileName = LOG_PATH + "/" + LOG_FILE_PREFIX + logDate + LOG_FILE_EXTENSION;
            File logFile = new File(fileName);

            // Check if the file exists, otherwise try the main log file for today
            if (!logFile.exists() && logDate.equals(LocalDate.now())) {
                logFile = new File(MAIN_LOG_FILE);
            }

            if (!logFile.exists()) {
                log.warn("Log file not found for date: {}", date);
                throw new EntityNotFoundException("Log file not found for date: " + date);
            }

            List<String> logEntries = new ArrayList<>();
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                String datePrefix = logDate.toString();
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(datePrefix)) {
                        logEntries.add(line);
                    }
                }
            }

            log.info("Retrieved {} log entries for date: {}", logEntries.size(), date);
            return ResponseEntity.ok(logEntries);
        } catch (java.time.format.DateTimeParseException ex) {
            log.error("Invalid date format: {}", date, ex);
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        } catch (IOException ex) {
            log.error("Error reading log file for date: {}", date, ex);
            throw new RuntimeException("Error reading log file: " + ex.getMessage());
        }
    }

    @Operation(summary = "Download log file by date",
            description = "Downloads the application log file "
                    + "for the specified date as a file attachment.")
    @ApiResponses(value = {@ApiResponse(responseCode = "200",
            description = "Log file downloaded successfully",
                    content = @Content(mediaType = "application/octet-stream")),
                           @ApiResponse(responseCode = "400", description = "Invalid date format",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "404",
                                   description = "No log entries found for date",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
                           @ApiResponse(responseCode = "500", description = "Internal server error",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/download")
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(description = "Date for which to download the log file (format: yyyy-MM-dd)")
            @RequestParam("date") String date
    ) {
        try {
            LocalDate logDate = LocalDate.parse(date, DateTimeFormatter.ISO_LOCAL_DATE);
            String fileName = LOG_PATH + "/" + LOG_FILE_PREFIX + logDate + LOG_FILE_EXTENSION;
            File logFile = new File(fileName);


            if (!logFile.exists()) {
                List<String> logEntries = getLogEntriesForDate(logDate);
                if (logEntries.isEmpty()) {
                    log.warn("No log entries found for date: {}", date);
                    throw new EntityNotFoundException("No log entries found for date: " + date);
                }

                logFile.getParentFile().mkdirs();
                try (FileWriter writer = new FileWriter(logFile)) {
                    for (String entry : logEntries) {
                        writer.write(entry + System.lineSeparator());
                    }
                }
            }

            Resource resource = new FileSystemResource(logFile);
            String contentType = "application/octet-stream";
            String headerValue = "attachment; filename=\"log(" + logDate + ").log\"";

            log.info("Downloading log file for date: {}", date);
            return ResponseEntity.ok()
                    .contentType(MediaType.parseMediaType(contentType))
                    .header(HttpHeaders.CONTENT_DISPOSITION, headerValue)
                    .body(resource);
        } catch (java.time.format.DateTimeParseException ex) {
            log.error("Invalid date format: {}", date, ex);
            throw new IllegalArgumentException("Invalid date format. Use yyyy-MM-dd");
        } catch (IOException ex) {
            log.error("Error generating or reading log file for date: {}", date, ex);
            throw new RuntimeException("Error processing log file: " + ex.getMessage());
        }
    }

    private List<String> getLogEntriesForDate(LocalDate logDate) throws IOException {
        List<String> logEntries = new ArrayList<>();
        String datePrefix = logDate.toString();

        // Check main log file
        File mainLogFile = new File(MAIN_LOG_FILE);
        if (mainLogFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(mainLogFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(datePrefix)) {
                        logEntries.add(line);
                    }
                }
            }
        }

        String fileName = LOG_PATH + "/" + LOG_FILE_PREFIX + logDate + LOG_FILE_EXTENSION;
        File dateLogFile = new File(fileName);
        if (dateLogFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(dateLogFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(datePrefix) && !logEntries.contains(line)) {
                        logEntries.add(line);
                    }
                }
            }
        }

        return logEntries;
    }
}