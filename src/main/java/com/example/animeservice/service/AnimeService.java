package com.example.animeservice.service;

import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.model.Collection;
import com.example.animeservice.repository.AnimeRepository;
import com.example.animeservice.repository.CollectionRepository;
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
    private final CollectionRepository collectionRepository;
    private final CacheService cacheService;

    public List<AnimeDto> getAllAnimes() {
        return animeRepository.findAll(Sort.by("title"))
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public AnimeDto getAnimeById(Long id) {
        String cacheKey = "anime_" + id;
        AnimeDto cached = (AnimeDto) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        AnimeDto result = animeRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Anime not found with id: " + id));
        cacheService.put(cacheKey, result);
        return result;
    }

    @Transactional
    public AnimeDto createAnime(AnimeDto dto) {
        Anime anime = new Anime();
        anime.setTitle(dto.getTitle());
        anime.setGenre(dto.getGenre());
        anime.setReleaseYear(dto.getReleaseYear());
        AnimeDto result = convertToDto(animeRepository.save(anime));
        invalidateAnimeCache();
        return result;
    }

    @Transactional
    public AnimeDto updateAnime(Long id, AnimeDto dto) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Anime not found with id: " + id));
        anime.setTitle(dto.getTitle());
        anime.setGenre(dto.getGenre());
        anime.setReleaseYear(dto.getReleaseYear());
        AnimeDto result = convertToDto(animeRepository.save(anime));
        invalidateAnimeCache();
        return result;
    }

    @Transactional
    public void deleteAnime(Long id) {
        Anime anime = animeRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Anime not found with id: " + id));

        List<Collection> collections = anime.getCollections();
        for (Collection collection : collections) {
            collection.getAnimes().remove(anime);
        }
        collectionRepository.saveAll(collections);
        animeRepository.delete(anime);
        cacheService.invalidate("anime_" + id);
        cacheService.invalidateByPrefix("anime_search_");
        cacheService.invalidateByPrefix("collection_search_anime_");
    }

    public List<AnimeDto> searchAnimes(String title, String genre, Integer year) {
        String cacheKey = "anime_search_" + (title != null ? title : "") + "_"
                + (genre != null ? genre : "") + "_" + (year != null ? year : "");
        @SuppressWarnings("unchecked")
        List<AnimeDto> cached = (List<AnimeDto>) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        Specification<Anime> spec = Specification.where(AnimeSpecifications.withTitle(title))
                .and(AnimeSpecifications.withGenre(genre))
                .and(AnimeSpecifications.withReleaseYear(year));

        List<AnimeDto> result = animeRepository.findAll(spec)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        cacheService.put(cacheKey, result);
        return result;
    }

    private AnimeDto convertToDto(Anime anime) {
        AnimeDto dto = new AnimeDto();
        dto.setId(anime.getId());
        dto.setTitle(anime.getTitle());
        dto.setGenre(anime.getGenre());
        dto.setReleaseYear(anime.getReleaseYear());
        return dto;
    }

    private void invalidateAnimeCache() {
        cacheService.invalidateByPrefix("anime_");
        cacheService.invalidateByPrefix("anime_search_");
        cacheService.invalidateByPrefix("collection_search_anime_");
    }
}