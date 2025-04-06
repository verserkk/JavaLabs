package com.example.animeservice.service;

import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.repository.AnimeRepository;
import com.example.animeservice.specs.AnimeSpecifications;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;




@Service
@RequiredArgsConstructor
public class AnimeService {
    private final AnimeRepository animeRepository;

    public List<AnimeDto> getAllAnimes() {
        return animeRepository.findAll(Sort.by("title"))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AnimeDto getAnimeById(Long id) {
        return animeRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id: " + id));
    }

    @Transactional
    public AnimeDto createAnime(AnimeDto dto) {
        Anime anime = new Anime();
        anime.setTitle(dto.getTitle());
        anime.setGenre(dto.getGenre());
        anime.setReleaseYear(dto.getReleaseYear());
        return convertToDto(animeRepository.save(anime));
    }

    @Transactional
    public AnimeDto updateAnime(Long id, AnimeDto dto) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Anime not found with id: " + id));
        anime.setTitle(dto.getTitle());
        anime.setGenre(dto.getGenre());
        anime.setReleaseYear(dto.getReleaseYear());
        return convertToDto(animeRepository.save(anime));
    }

    @Transactional
    public void deleteAnime(Long id) {
        if (!animeRepository.existsById(id)) {
            throw new EntityNotFoundException("Anime not found with id: " + id);
        }
        animeRepository.deleteById(id);
    }


    public List<AnimeDto> searchAnimes(String title, String genre, Integer year) {
        Specification<Anime> spec = Specification.where(AnimeSpecifications.withTitle(title))
                .and(AnimeSpecifications.withGenre(genre))
                .and(AnimeSpecifications.withReleaseYear(year));

        return animeRepository.findAll(spec)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    private AnimeDto convertToDto(Anime anime) {
        AnimeDto dto = new AnimeDto();
        dto.setId(anime.getId());
        dto.setTitle(anime.getTitle());
        dto.setGenre(anime.getGenre());
        dto.setReleaseYear(anime.getReleaseYear());
        return dto;
    }
}