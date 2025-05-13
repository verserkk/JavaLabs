package com.example.animeservice.service;

import com.example.animeservice.dto.VisitDto;
import com.example.animeservice.repository.VisitRepository;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Service
@Slf4j
public class VisitService {
    private final VisitRepository visitRepository;

    public VisitService(VisitRepository visitRepository) {
        this.visitRepository = visitRepository;
    }

    @PostConstruct
    public void init() {
        log.debug("Initializing Visit Cache");
        visitRepository.deleteAll();
    }

    public synchronized void incrementVisit(String url) {
        VisitDto visitDto = visitRepository.findByUrl(url)
                .orElseGet(() -> {
                    VisitDto newVisitDto = new VisitDto();
                    newVisitDto.setUrl(url);
                    newVisitDto.setVisitCount(0);
                    return newVisitDto;
                });
        visitDto.setVisitCount(visitDto.getVisitCount() + 1);
        visitRepository.save(visitDto);
    }

    public long getVisitCount(String url) {
        return visitRepository.findByUrl(url)
                .map(VisitDto::getVisitCount)
                .orElse(0L);
    }
}