package com.example.animeservice.repository;

import com.example.animeservice.dto.VisitDto;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Repository;

@Repository
public class VisitRepository {
    private final Map<String, VisitDto> visitMap = new ConcurrentHashMap<>();

    public Optional<VisitDto> findByUrl(String url) {
        return Optional.ofNullable(visitMap.get(url));
    }

    public synchronized VisitDto save(VisitDto visitDto) {
        visitMap.put(visitDto.getUrl(), visitDto);
        return visitDto;
    }

    public void deleteAll() {
        visitMap.clear();
    }
}