package com.example.animeservice.service;


import com.example.animeservice.dto.CollectionDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.model.Collection;
import com.example.animeservice.model.User;
import com.example.animeservice.repository.AnimeRepository;
import com.example.animeservice.repository.CollectionRepository;
import com.example.animeservice.repository.UserRepository;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class CollectionService {
    private final CollectionRepository collectionRepository;
    private final UserRepository userRepository;
    private final AnimeRepository animeRepository;

    public List<CollectionDto> getAllCollections() {
        return collectionRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public CollectionDto getCollectionById(Long id) {
        return collectionRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() ->
                        new EntityNotFoundException("Collection not found with id: " + id));
    }

    @Transactional
    public CollectionDto createCollection(CollectionDto dto) {
        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new EntityNotFoundException("User not found with id: " + dto.getUserId()));

        Collection collection = new Collection();
        collection.setName(dto.getName());
        collection.setUser(user);

        if (dto.getAnimeIds() != null && !dto.getAnimeIds().isEmpty()) {
            Set<Anime> animes = new HashSet<>(animeRepository.findAllById(dto.getAnimeIds()));
            if (animes.size() != dto.getAnimeIds().size()) {
                throw new EntityNotFoundException("One or more anime not found");
            }
            collection.setAnimes(animes);
        }

        return convertToDto(collectionRepository.save(collection));
    }

    @Transactional
    public CollectionDto updateCollection(Long id, CollectionDto dto) {
        Collection collection = collectionRepository.findById(id)
                .orElseThrow(() ->
                        new EntityNotFoundException("Collection not found with id: " + id));

        if (dto.getName() != null) {
            collection.setName(dto.getName());
        }

        if (dto.getUserId() != null && !dto.getUserId().equals(collection.getUser().getId())) {
            User user = userRepository.findById(dto.getUserId())
                    .orElseThrow(() ->
                            new EntityNotFoundException("User not found with id: "
                                    + dto.getUserId()));
            collection.setUser(user);
        }

        if (dto.getAnimeIds() != null) {
            Set<Anime> animes = new HashSet<>(animeRepository.findAllById(dto.getAnimeIds()));
            if (animes.size() != dto.getAnimeIds().size()) {
                throw new EntityNotFoundException("One or more anime not found");
            }
            collection.setAnimes(animes);
        }

        return convertToDto(collectionRepository.save(collection));
    }

    @Transactional
    public void deleteCollection(Long id) {
        if (!collectionRepository.existsById(id)) {
            throw new EntityNotFoundException("Collection not found with id: " + id);
        }
        collectionRepository.deleteById(id);
    }

    public List<CollectionDto> getCollectionsByUser(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found with id: " + userId);
        }
        return collectionRepository.findByUserId(userId)
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public List<CollectionDto> searchCollections(String name, Long animeId) {
        if (name != null && animeId != null) {
            return collectionRepository.findByNameContainingIgnoreCase(name)
                    .stream()
                    .filter(collection -> collection.getAnimes().stream()
                            .anyMatch(anime -> anime.getId().equals(animeId)))
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (name != null) {
            return collectionRepository.findByNameContainingIgnoreCase(name)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (animeId != null) {
            return collectionRepository.findByAnimesId(animeId)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return collectionRepository.findAll()
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        }
    }

    private CollectionDto convertToDto(Collection collection) {
        CollectionDto dto = new CollectionDto();
        dto.setId(collection.getId());
        dto.setName(collection.getName());
        dto.setUserId(collection.getUser().getId());
        dto.setAnimeIds(collection.getAnimes().stream()
                .map(Anime::getId)
                .collect(Collectors.toSet()));
        return dto;
    }
}