package com.example.animeservice.service;

import com.example.animeservice.dto.LogStatusResponse;
import com.example.animeservice.exception.EntityNotFoundException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class AsyncLogService {

    private static final String LOG_PATH = "logs";
    private static final String LOG_FILE_PREFIX = "app-";
    private static final String LOG_FILE_EXTENSION = ".log";
    private static final String MAIN_LOG_FILE = LOG_PATH + "/app.log";
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ISO_LOCAL_DATE;

    private final ConcurrentHashMap<String, String> logStatuses = new ConcurrentHashMap<>();

    @Async
    public CompletableFuture<String> createLogFile(String date) throws InterruptedException {
        String logId = UUID.randomUUID().toString();
        logStatuses.put(logId, "IN_PROGRESS");

        try {
            Thread.sleep(20000);
            LocalDate logDate = parseDate(date);
            String fileName = buildLogFilePath(logDate);
            createLogFileIfNotExists(logDate, fileName);
            logStatuses.put(logId, "COMPLETED");
            return CompletableFuture.completedFuture(logId);
        } catch (InterruptedException e) {
            logStatuses.put(logId, "FAILED");
            log.error("Log creation failed for date: {}", date, e);
            throw new InterruptedException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public LogStatusResponse getLogStatus(String logId, String date) throws InterruptedException {
        String status = logStatuses.getOrDefault(logId, "NOT_FOUND");
        if ("COMPLETED".equals(status) && !verifyLogFileExists(date)) {
            status = "FAILED";
        }
        return new LogStatusResponse(logId, status);
    }

    public File getLogFile(String date) throws IOException, InterruptedException {
        LocalDate logDate = parseDate(date);
        String fileName = buildLogFilePath(logDate);
        File logFile = new File(fileName);

        if (!logFile.exists()) {
            createLogFileIfNotExists(logDate, fileName);
        }

        if (!logFile.exists() || logFile.length() == 0) {
            throw new EntityNotFoundException("Log file not found or empty for date: " + date);
        }

        return logFile;
    }

    private LocalDate parseDate(String date) {
        return LocalDate.parse(date, DATE_FORMATTER);
    }

    private String buildLogFilePath(LocalDate date) {
        return LOG_PATH + "/" + LOG_FILE_PREFIX + date + LOG_FILE_EXTENSION;
    }

    private void createLogFileIfNotExists(LocalDate date, String fileName)
            throws InterruptedException, IOException {
        File logFile = new File(fileName);
        if (!logFile.exists()) {
            ensureLogDirectoryExists();
            writeLogEntriesToFile(date, logFile);
        }
    }

    private void ensureLogDirectoryExists() throws IOException {
        File logDir = new File(LOG_PATH);
        if (!logDir.exists() && !logDir.mkdirs()) {
            throw new IOException("Failed to create log directory");
        }
    }

    private void writeLogEntriesToFile(LocalDate date, File logFile) throws IOException {
        try (FileWriter writer = new FileWriter(logFile)) {
            for (String entry : collectLogEntries(date)) {
                writer.write(entry + System.lineSeparator());
            }
        }
    }

    private boolean verifyLogFileExists(String date) throws InterruptedException {
        try {
            return getLogFile(date).exists();
        } catch (InterruptedException e) {
            throw new InterruptedException();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private List<String> collectLogEntries(LocalDate date) throws IOException {
        List<String> entries = new ArrayList<>();
        String datePrefix = date.toString();

        collectEntriesFromFile(new File(MAIN_LOG_FILE), datePrefix, entries);
        collectEntriesFromFile(new File(buildLogFilePath(date)), datePrefix, entries);

        return entries;
    }

    private void collectEntriesFromFile(File file, String datePrefix, List<String> entries)
            throws IOException {
        if (file.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith(datePrefix)) {
                        entries.add(line);
                    }
                }
            }
        }
    }
}