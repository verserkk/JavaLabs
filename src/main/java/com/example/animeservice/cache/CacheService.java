package com.example.animeservice.cache;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.concurrent.ConcurrentHashMap;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
@EnableScheduling
@Slf4j
public class CacheService {

    private static final int MAX_CACHE_SIZE = 100;
    private static final int ENTRIES_TO_REMOVE = 5;
    private static final long TTL_SECONDS = 60;

    private final ConcurrentHashMap<String, CacheEntry> cache = new ConcurrentHashMap<>();

    public void put(String key, Object value) {
        if (cache.size() >= MAX_CACHE_SIZE) {
            removeOldestEntries();
        }
        cache.put(key, new CacheEntry(value, LocalDateTime.now()));
    }

    public Object get(String key) {
        CacheEntry entry = cache.get(key);
        if (entry == null) {
            return null;
        }
        if (isExpired(entry)) {
            cache.remove(key);
            return null;
        }
        log.info("Cache hit for key: {}", key);
        return entry.getValue();
    }

    public void invalidate(String key) {
        cache.remove(key);
    }

    public void invalidateByPrefix(String prefix) {
        cache.keySet().removeIf(key -> key.startsWith(prefix));
    }

    @Scheduled(fixedRate = 60000)
    public void cleanExpiredEntries() {
        cache.entrySet().removeIf(entry -> isExpired(entry.getValue()));
    }

    private boolean isExpired(CacheEntry entry) {
        return entry.getTimestamp()
                .plusSeconds(TTL_SECONDS)
                .isBefore(LocalDateTime.now());
    }

    private void removeOldestEntries() {
        cache.entrySet().stream()
                .sorted(Comparator.comparing(
                        entry -> entry.getValue().getTimestamp()))
                .limit(ENTRIES_TO_REMOVE)
                .forEach(entry -> cache.remove(entry.getKey()));
    }

    @Getter
    @Setter
    @RequiredArgsConstructor
    private static class CacheEntry {
        private final Object value;
        private final LocalDateTime timestamp;
    }
}