package com.example.animeservice.service;

import com.example.animeservice.cache.CacheService;
import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.dto.CollectionDto;
import com.example.animeservice.dto.CollectionWithAnimeDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.model.Collection;
import com.example.animeservice.model.User;
import com.example.animeservice.repository.AnimeRepository;
import com.example.animeservice.repository.CollectionRepository;
import com.example.animeservice.repository.UserRepository;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CollectionService {

    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final AnimeRepository animeRepository;
    private final CacheService cacheService;

    public List<CollectionDto> getAllCollections() {
        return collectionRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CollectionDto getCollectionById(Long id) {
        String cacheKey = "collection_" + id;
        CollectionDto cached = (CollectionDto) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        CollectionDto result = collectionRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Collection not found with id: " + id));
        cacheService.put(cacheKey, result);
        return result;
    }

    @Transactional
    public CollectionDto createCollection(CollectionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() -> new EntityNotFoundException(
                        "User not found with id: " + dto.getUserId()));

        Collection collection = new Collection();
        collection.setName(dto.getName());
        collection.setUser(user);

        if (dto.getAnimeIds() != null && !dto.getAnimeIds().isEmpty()) {
            List<Anime> animes = animeRepository.findAllById(dto.getAnimeIds());
            if (animes.size() != dto.getAnimeIds().size()) {
                throw new EntityNotFoundException("One or more anime not found");
            }
            collection.setAnimes(animes);
        }

        CollectionDto result = convertToDto(collectionRepository.save(collection));
        invalidateCollectionCache();
        return result;
    }

    @Transactional
    public CollectionDto updateCollection(Long id, CollectionDto dto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Collection not found with id: " + id));

        if (dto.getName() != null) {
            collection.setName(dto.getName());
        }

        if (dto.getUserId() != null && !dto.getUserId().equals(collection.getUser().getId())) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() -> new EntityNotFoundException(
                            "User not found with id: " + dto.getUserId()));
            collection.setUser(user);
        }

        if (dto.getAnimeIds() != null) {
            List<Anime> animes = animeRepository.findAllById(dto.getAnimeIds());
            if (animes.size() != dto.getAnimeIds().size()) {
                throw new EntityNotFoundException("One or more anime not found");
            }
            collection.setAnimes(animes);
        }

        CollectionDto result = convertToDto(collectionRepository.save(collection));
        invalidateCollectionCache();
        return result;
    }

    @Transactional
    public void deleteCollection(Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new EntityNotFoundException("Collection not found with id: " + id);
        }
        collectionRepository.deleteById(id);

        cacheService.invalidate("collection_" + id);
        cacheService.invalidateByPrefix("collections_user_");
        cacheService.invalidateByPrefix("collection_search_");
        cacheService.invalidateByPrefix("collection_search_anime_");
        cacheService.invalidateByPrefix("user_collections_");
    }

    public List<CollectionDto> getCollectionsByUser(Long userId) {
        String cacheKey = "collections_user_" + userId;
        @SuppressWarnings("unchecked")
        List<CollectionDto> cached = (List<CollectionDto>) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<CollectionDto> result = collectionRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
        cacheService.put(cacheKey, result);
        return result;
    }

    public List<CollectionDto> searchCollections(String name, Long animeId) {
        String cacheKey = "collection_search_" + (name != null ? name : "") + "_"
                + (animeId != null ? animeId : "");
        @SuppressWarnings("unchecked")
        List<CollectionDto> cached = (List<CollectionDto>) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        List<CollectionDto> result;
        if (name != null && animeId != null) {
            result = collectionRepository.findByNameContainingIgnoreCase(name)
                    .stream()
                    .filter(collection -> collection.getAnimes().stream()
                            .anyMatch(anime -> anime.getId().equals(animeId)))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (name != null) {
            result = collectionRepository.findByNameContainingIgnoreCase(name)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (animeId != null) {
            result = collectionRepository.findByAnimesId(animeId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("At least one parameter must be provided");
        }
        cacheService.put(cacheKey, result);
        return result;
    }

    @Transactional(readOnly = true)
    public List<CollectionWithAnimeDto> searchCollectionsByAnimeParams(
            String title, String genre, Integer releaseYear) {
        String cacheKey = "collection_search_anime_" + (title != null ? title : "") + "_"
                + (genre != null ? genre : "") + "_" + (releaseYear != null ? releaseYear : "");
        @SuppressWarnings("unchecked")
        List<CollectionWithAnimeDto> cached =
                (List<CollectionWithAnimeDto>) cacheService.get(cacheKey);
        if (cached != null) {
            return cached;
        }
        if (title == null && genre == null && releaseYear == null) {
            throw new IllegalArgumentException("At least one parameter must be provided");
        }

        List<Object[]> results = collectionRepository
                .searchCollectionsWithAnimeByParams(title, genre, releaseYear);

        Map<Long, CollectionWithAnimeDto> collectionMap = new LinkedHashMap<>();

        for (Object[] row : results) {
            Long collectionId = ((Number) row[0]).longValue();
            String collectionName = (String) row[1];
            Long animeId = ((Number) row[2]).longValue();
            String animeTitle = (String) row[3];
            String animeGenre = (String) row[4];
            Integer animeYear = (row[5] != null) ? ((Number) row[5]).intValue() : null;

            CollectionWithAnimeDto collectionDto = collectionMap
                    .computeIfAbsent(collectionId, id -> {
                        CollectionWithAnimeDto dto = new CollectionWithAnimeDto();
                        dto.setId(id);
                        dto.setName(collectionName);
                        dto.setAnimes(new ArrayList<>());
                        return dto;
                    });

            AnimeDto animeDto = new AnimeDto(animeId, animeTitle, animeGenre, animeYear);
            collectionDto.getAnimes().add(animeDto);
        }
        List<CollectionWithAnimeDto> result = new ArrayList<>(collectionMap.values());
        cacheService.put(cacheKey, result);
        return result;
    }

    private CollectionDto convertToDto(Collection collection) {
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setName(collection.getName());
        dto.setUserId(collection.getUser().getId());
        dto.setAnimeIds(collection.getAnimes().stream()
                .map(Anime::getId)
                .collect(Collectors.toList()));
        return dto;
    }

    private void invalidateCollectionCache() {
        cacheService.invalidateByPrefix("collection_");
        cacheService.invalidateByPrefix("collections_user_");
        cacheService.invalidateByPrefix("collection_search_");
        cacheService.invalidateByPrefix("collection_search_anime_");
        cacheService.invalidateByPrefix("user_collections_");
    }

    @Transactional
    public List<CollectionDto> createCollections(List<CollectionDto> dtos) {
        Map<Long, User> userMap = dtos.stream()
                .map(CollectionDto::getUserId)
                .distinct()
                .collect(Collectors.toMap(
                        userId -> userId,
                        userId -> userRepository.findById(userId)
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "User not found with id: " + userId))
                ));

        Map<Long, List<Anime>> animeMap = dtos.stream()
                .flatMap(dto -> dto.getAnimeIds().stream())
                .distinct()
                .collect(Collectors.toMap(
                        animeId -> animeId,
                        animeId -> animeRepository.findById(animeId)
                                .map(List::of)
                                .orElseThrow(() -> new EntityNotFoundException(
                                        "Anime not found with id: " + animeId))
                ));

        List<Collection> collections = dtos.stream()
                .map(dto -> {
                    Collection collection = new Collection();
                    collection.setName(dto.getName());
                    collection.setUser(userMap.get(dto.getUserId()));
                    collection.setAnimes(dto.getAnimeIds().stream()
                            .flatMap(id -> animeMap.get(id).stream())
                            .collect(Collectors.toList()));
                    return collection;
                })
                .collect(Collectors.toList());

        List<CollectionDto> result = collectionRepository.saveAll(collections)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());

        invalidateCollectionCache();
        return result;
    }
}