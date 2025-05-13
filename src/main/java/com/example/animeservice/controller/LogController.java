package com.example.animeservice.controller;

import com.example.animeservice.dto.LogCreateResponse;
import com.example.animeservice.dto.LogStatusResponse;
import com.example.animeservice.service.AsyncLogService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.CompletableFuture;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/logs")
@Slf4j
@RequiredArgsConstructor
public class LogController {

    private final AsyncLogService asyncLogService;

    @GetMapping("/create")
    @Operation(summary = "Create log file",
            responses = {@ApiResponse(responseCode = "200", description = "Log creation started"),
                         @ApiResponse(responseCode = "400", description = "Invalid date format")})
    public CompletableFuture<ResponseEntity<LogCreateResponse>> createLogFile(
            @Parameter(description = "Date in yyyy-MM-dd format")
            @RequestParam String date) throws InterruptedException {
        log.info("Creating log file for date: {}", date);
        return asyncLogService.createLogFile(date)
                .thenApply(LogCreateResponse::new)
                .thenApply(ResponseEntity::ok);
    }

    @GetMapping("/{logId}/status")
    @Operation(summary = "Get log status",
            responses = {@ApiResponse(responseCode = "200", description = "Status retrieved"),
                         @ApiResponse(responseCode = "404", description = "Log not found")})
    public ResponseEntity<LogStatusResponse> getLogStatus(
            @PathVariable String logId,
            @Parameter(description = "Date in yyyy-MM-dd format")
            @RequestParam String date) {
        log.info("Checking status for log ID: {} and date: {}", logId, date);
        return ResponseEntity.ok(asyncLogService.getLogStatus(logId, date));
    }

    @GetMapping("/download")
    @Operation(summary = "Download log file",
            responses = {@ApiResponse(responseCode = "200", description = "File downloaded"),
                         @ApiResponse(responseCode = "404", description = "File not found")})
    public ResponseEntity<Resource> downloadLogFile(
            @Parameter(description = "Date in yyyy-MM-dd format")
            @RequestParam String date) throws InterruptedException, IOException {
        log.info("Downloading log file for date: {}", date);
        try {
            File logFile = asyncLogService.getLogFile(date);
            return buildFileResponse(logFile, date);
        } catch (InterruptedException e) {
            log.error("Error downloading log file for date: {}", date, e);
            throw new InterruptedException();
        } catch (IOException e) {
            log.error("Error file is not for date: {}", date, e);
            throw new IOException();
        }
    }

    private ResponseEntity<Resource> buildFileResponse(File file, String date) {
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        buildContentDispositionHeader(date))
                .body(new FileSystemResource(file));
    }

    private String buildContentDispositionHeader(String date) {
        return "attachment; filename=\"log_" + date + ".log\"";
    }
}