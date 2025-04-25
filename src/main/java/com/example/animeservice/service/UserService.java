package com.example.animeservice.service;

import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.dto.CollectionWithAnimeDto;
import com.example.animeservice.dto.UserDto;
import com.example.animeservice.dto.UserWithCollectionsDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Collection;
import com.example.animeservice.model.User;
import com.example.animeservice.repository.CollectionRepository;
import com.example.animeservice.repository.UserRepository;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final CollectionRepository collectionRepository;

    public List<UserDto> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(this::convertToDto)
                .collect(Collectors.toList());
    }

    public UserDto getUserById(Long id) {
        return userRepository.findById(id)
                .map(this::convertToDto)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));
    }

    @Transactional
    public UserDto createUser(UserDto dto) {
        if (userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }
        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        return convertToDto(userRepository.save(user));
    }

    @Transactional
    public UserDto updateUser(Long id, UserDto dto) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("User not found with id: " + id));

        if (!user.getUsername().equals(dto.getUsername())
                && userRepository.existsByUsername(dto.getUsername())) {
            throw new IllegalArgumentException("Username already exists");
        }

        if (!user.getEmail().equals(dto.getEmail())
                && userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        return convertToDto(userRepository.save(user));
    }

    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new EntityNotFoundException("User not found with id: " + id);
        }
        userRepository.deleteById(id);
    }

    public List<UserDto> searchUsers(String username, String email) {
        if (username != null && email != null) {
            return userRepository.searchByUsernameOrEmail(username, email)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (username != null) {
            return userRepository.findByUsernameContainingIgnoreCase(username)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else if (email != null) {
            return userRepository.findByEmailContainingIgnoreCase(email)
                    .stream()
                    .map(this::convertToDto)
                    .collect(Collectors.toList());
        } else {
            return getAllUsers();
        }
    }

    private UserDto convertToDto(User user) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        return dto;
    }

    @Transactional(readOnly = true)
    public UserWithCollectionsDto getUserWithCollectionsAndAnime(Long userId) {
        User user = userRepository.findByIdWithCollections(userId)
                .orElseThrow(() -> new
                        EntityNotFoundException("User not found with id: " + userId));
        List<Collection> collections
                = collectionRepository.fetchCollectionsWithAnimes(user.getCollections());
        user.setCollections(collections);
        return mapToUserWithCollectionsDto(user);
    }

    private UserWithCollectionsDto mapToUserWithCollectionsDto(User user) {
        UserWithCollectionsDto dto = new UserWithCollectionsDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());

        dto.setCollections(user.getCollections().stream()
                .map(collection -> {
                    CollectionWithAnimeDto collectionDto = new CollectionWithAnimeDto();
                    collectionDto.setId(collection.getId());
                    collectionDto.setName(collection.getName());
                    collectionDto.setAnimes(collection.getAnimes().stream()
                            .map(anime -> {
                                AnimeDto animeDto = new AnimeDto();
                                animeDto.setId(anime.getId());
                                animeDto.setTitle(anime.getTitle());
                                animeDto.setGenre(anime.getGenre());
                                animeDto.setReleaseYear(anime.getReleaseYear());
                                return animeDto;
                            })
                            .collect(Collectors.toList()));
                    return collectionDto;
                })
                .collect(Collectors.toList()));

        return dto;
    }

    @Transactional(readOnly = true)
    public List<UserWithCollectionsDto> getAllUsersWithCollectionsAndAnimes() {
        List<User> users = userRepository.findAllWithCollections();
        List<Collection> allCollections = users.stream()
                .flatMap(user -> user.getCollections().stream())
                .distinct()
                .collect(Collectors.toList());
        List<Collection> collectionsWithAnimes
                = collectionRepository.fetchCollectionsWithAnimes(allCollections);
        Map<Long, Collection> collectionMap = collectionsWithAnimes.stream()
                .collect(Collectors.toMap(Collection::getId, c -> c));

        for (User user : users) {
            List<Collection> updated = user.getCollections().stream()
                    .map(c -> collectionMap.getOrDefault(c.getId(), c))
                    .collect(Collectors.toList());
            user.setCollections(updated);
        }
        return users.stream()
                .map(this::mapToUserWithCollectionsDto)
                .collect(Collectors.toList());
    }
}