package com.example.animeservice.service;

import com.example.animeservice.cache.CacheService;
import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.dto.CollectionWithAnimeDto;
import com.example.animeservice.dto.UserDto;
import com.example.animeservice.dto.UserWithCollectionsDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.model.Collection;
import com.example.animeservice.model.User;
import com.example.animeservice.repository.CollectionRepository;
import com.example.animeservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private UserService userService;

    private User user;
    private UserDto userDto;
    private Collection collection;
    private Anime anime;

    @BeforeEach
    void setUp() {
        // Инициализация тестовых данных без циклических ссылок
        user = new User();
        user.setId(1L);
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setCollections(new ArrayList<>());

        userDto = new UserDto();
        userDto.setId(1L);
        userDto.setUsername("testuser");
        userDto.setEmail("test@example.com");

        collection = new Collection();
        collection.setId(1L);
        collection.setName("Test Collection");
        collection.setAnimes(new ArrayList<>());

        anime = new Anime();
        anime.setId(1L);
        anime.setTitle("Test Anime");
        anime.setGenre("Action");
        anime.setReleaseYear(2020);
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() {
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.getAllUsers();

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
        verify(userRepository).findAll();
    }

    @Test
    void getAllUsers_ShouldReturnEmptyList() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList());

        List<UserDto> result = userService.getAllUsers();

        assertTrue(result.isEmpty());
        verify(userRepository).findAll();
    }

    @Test
    void getUserById_ShouldReturnUserFromCache() {
        when(cacheService.get("user_1")).thenReturn(userDto);

        UserDto result = userService.getUserById(1L);

        assertEquals(userDto, result);
        verify(cacheService).get("user_1");
        verify(userRepository, never()).findById(any());
    }

    @Test
    void getUserById_ShouldReturnUserFromRepository() {
        when(cacheService.get("user_1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        UserDto result = userService.getUserById(1L);

        assertEquals(userDto, result);
        verify(cacheService).get("user_1");
        verify(userRepository).findById(1L);
        verify(cacheService).put("user_1", userDto);
    }

    @Test
    void getUserById_ShouldThrowEntityNotFoundException() {
        when(cacheService.get("user_1")).thenReturn(null);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserById(1L));
        verify(cacheService).get("user_1");
        verify(userRepository).findById(1L);
    }

    @Test
    void createUser_ShouldCreateNewUser() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenReturn(user);

        UserDto result = userService.createUser(userDto);

        assertEquals(userDto, result);
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository).save(any(User.class));
        verify(cacheService).invalidateByPrefix("user_");
        verify(cacheService).invalidateByPrefix("user_search_");
        verify(cacheService).invalidateByPrefix("user_collections_");
        verify(cacheService).invalidateByPrefix("collections_user_");
    }

    @Test
    void createUser_ShouldThrowExceptionWhenUsernameExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDto));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void createUser_ShouldThrowExceptionWhenEmailExists() {
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(userRepository.existsByEmail("test@example.com")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> userService.createUser(userDto));
        verify(userRepository).existsByUsername("testuser");
        verify(userRepository).existsByEmail("test@example.com");
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldUpdateExistingUser() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("newuser")).thenReturn(false);
        when(userRepository.existsByEmail("new@example.com")).thenReturn(false);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setUsername("newuser");
        updatedDto.setEmail("new@example.com");

        UserDto result = userService.updateUser(1L, updatedDto);

        assertEquals(updatedDto, result);
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("newuser");
        verify(userRepository).existsByEmail("new@example.com");
        verify(userRepository).save(any(User.class));
        verify(cacheService).invalidateByPrefix("user_");
        verify(cacheService).invalidateByPrefix("user_search_");
        verify(cacheService).invalidateByPrefix("user_collections_");
        verify(cacheService).invalidateByPrefix("collections_user_");
    }

    @Test
    void updateUser_ShouldThrowEntityNotFoundException() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.updateUser(1L, userDto));
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByUsername(anyString());
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateUser_ShouldThrowExceptionWhenUsernameExists() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        when(userRepository.existsByUsername("existinguser")).thenReturn(true);

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setUsername("existinguser");
        updatedDto.setEmail("old@example.com");

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, updatedDto));
        verify(userRepository).findById(1L);
        verify(userRepository).existsByUsername("existinguser");
        verify(userRepository, never()).existsByEmail(anyString());
        verify(userRepository, never()).save(any());
    }


    @Test
    void updateUser_ShouldThrowExceptionWhenEmailExists() {
        User existingUser = new User();
        existingUser.setId(1L);
        existingUser.setUsername("olduser");
        existingUser.setEmail("old@example.com");

        when(userRepository.findById(1L)).thenReturn(Optional.of(existingUser));
        // Не проверяем existsByUsername, так как username не меняется
        when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);

        UserDto updatedDto = new UserDto();
        updatedDto.setId(1L);
        updatedDto.setUsername("olduser"); // username остается тем же
        updatedDto.setEmail("existing@example.com"); // email изменяется на существующий

        assertThrows(IllegalArgumentException.class, () -> userService.updateUser(1L, updatedDto));

        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsByUsername(anyString()); // username не проверяем
        verify(userRepository).existsByEmail("existing@example.com");
        verify(userRepository, never()).save(any());
    }


    @Test
    void deleteUser_ShouldDeleteUser() {
        when(userRepository.existsById(1L)).thenReturn(true);

        userService.deleteUser(1L);

        verify(userRepository).existsById(1L);
        verify(userRepository).deleteById(1L);
        verify(cacheService).invalidate("user_1");
        verify(cacheService).invalidateByPrefix("user_search_");
        verify(cacheService).invalidateByPrefix("user_collections_");
        verify(cacheService).invalidateByPrefix("collections_user_1");
    }

    @Test
    void deleteUser_ShouldThrowEntityNotFoundException() {
        when(userRepository.existsById(1L)).thenReturn(false);

        assertThrows(EntityNotFoundException.class, () -> userService.deleteUser(1L));
        verify(userRepository).existsById(1L);
        verify(userRepository, never()).deleteById(any());
        verify(cacheService, never()).invalidate(any());
    }

    @Test
    void searchUsers_ShouldReturnFromCache() {
        List<UserDto> cachedResult = List.of(userDto);
        when(cacheService.get("user_search_testuser_")).thenReturn(cachedResult);

        List<UserDto> result = userService.searchUsers("testuser", null);

        assertEquals(cachedResult, result);
        verify(cacheService).get("user_search_testuser_");
        verify(userRepository, never()).searchByUsernameOrEmail(anyString(), anyString());
        verify(userRepository, never()).findByUsernameContainingIgnoreCase(anyString());
        verify(userRepository, never()).findByEmailContainingIgnoreCase(anyString());
    }

    @Test
    void searchUsers_ShouldSearchByUsernameAndEmail() {
        when(cacheService.get("user_search_test_test@")).thenReturn(null);
        when(userRepository.searchByUsernameOrEmail("test", "test@")).thenReturn(List.of(user));

        List<UserDto> result = userService.searchUsers("test", "test@");

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
        verify(cacheService).get("user_search_test_test@");
        verify(userRepository).searchByUsernameOrEmail("test", "test@");
        verify(cacheService).put("user_search_test_test@", result);
    }

    @Test
    void searchUsers_ShouldSearchByUsernameOnly() {
        when(cacheService.get("user_search_test_")).thenReturn(null);
        when(userRepository.findByUsernameContainingIgnoreCase("test")).thenReturn(List.of(user));

        List<UserDto> result = userService.searchUsers("test", null);

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
        verify(cacheService).get("user_search_test_");
        verify(userRepository).findByUsernameContainingIgnoreCase("test");
        verify(cacheService).put("user_search_test_", result);
    }

    @Test
    void searchUsers_ShouldSearchByEmailOnly() {
        when(cacheService.get("user_search__test@")).thenReturn(null);
        when(userRepository.findByEmailContainingIgnoreCase("test@")).thenReturn(List.of(user));

        List<UserDto> result = userService.searchUsers(null, "test@");

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
        verify(cacheService).get("user_search__test@");
        verify(userRepository).findByEmailContainingIgnoreCase("test@");
        verify(cacheService).put("user_search__test@", result);
    }

    @Test
    void searchUsers_ShouldReturnAllUsersWhenNoParams() {
        when(cacheService.get("user_search__")).thenReturn(null);
        when(userRepository.findAll()).thenReturn(List.of(user));

        List<UserDto> result = userService.searchUsers(null, null);

        assertEquals(1, result.size());
        assertEquals(userDto, result.get(0));
        verify(cacheService).get("user_search__");
        verify(userRepository).findAll();
        verify(cacheService).put("user_search__", result);
    }

    @Test
    void getUserWithCollectionsAndAnime_ShouldReturnFromCache() {
        UserWithCollectionsDto cachedDto = new UserWithCollectionsDto();
        cachedDto.setId(1L);
        cachedDto.setUsername("testuser");
        cachedDto.setEmail("test@example.com");

        when(cacheService.get("user_collections_1")).thenReturn(cachedDto);

        UserWithCollectionsDto result = userService.getUserWithCollectionsAndAnime(1L);

        assertEquals(cachedDto, result);
        verify(cacheService).get("user_collections_1");
        verify(userRepository, never()).findByIdWithCollections(any());
        verify(collectionRepository, never()).fetchCollectionsWithAnimes(any());
    }

    @Test
    void getUserWithCollectionsAndAnime_ShouldReturnFromRepository() {
        User userWithCollections = new User();
        userWithCollections.setId(1L);
        userWithCollections.setUsername("testuser");
        userWithCollections.setEmail("test@example.com");
        userWithCollections.setCollections(List.of(collection));

        when(cacheService.get("user_collections_1")).thenReturn(null);
        when(userRepository.findByIdWithCollections(1L)).thenReturn(Optional.of(userWithCollections));
        when(collectionRepository.fetchCollectionsWithAnimes(anyList())).thenReturn(List.of(collection));

        UserWithCollectionsDto result = userService.getUserWithCollectionsAndAnime(1L);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("testuser", result.getUsername());
        assertEquals("test@example.com", result.getEmail());
        assertEquals(1, result.getCollections().size());

        verify(cacheService).get("user_collections_1");
        verify(userRepository).findByIdWithCollections(1L);
        verify(collectionRepository).fetchCollectionsWithAnimes(anyList());
        verify(cacheService).put("user_collections_1", result);
    }

    @Test
    void getUserWithCollectionsAndAnime_ShouldThrowEntityNotFoundException() {
        when(cacheService.get("user_collections_1")).thenReturn(null);
        when(userRepository.findByIdWithCollections(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> userService.getUserWithCollectionsAndAnime(1L));
        verify(cacheService).get("user_collections_1");
        verify(userRepository).findByIdWithCollections(1L);
        verify(collectionRepository, never()).fetchCollectionsWithAnimes(any());
    }

    @Test
    void getAllUsersWithCollectionsAndAnimes_ShouldReturnUsersWithCollections() {
        User userWithCollections = new User();
        userWithCollections.setId(1L);
        userWithCollections.setUsername("testuser");
        userWithCollections.setEmail("test@example.com");
        userWithCollections.setCollections(List.of(collection));

        when(userRepository.findAllWithCollections()).thenReturn(List.of(userWithCollections));
        when(collectionRepository.fetchCollectionsWithAnimes(anyList())).thenReturn(List.of(collection));

        List<UserWithCollectionsDto> result = userService.getAllUsersWithCollectionsAndAnimes();

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1, result.get(0).getCollections().size());

        verify(userRepository).findAllWithCollections();
        verify(collectionRepository).fetchCollectionsWithAnimes(anyList());
    }

    @Test
    void getAllUsersWithCollectionsAndAnimes_ShouldReturnEmptyList() {
        when(userRepository.findAllWithCollections()).thenReturn(Collections.emptyList());

        List<UserWithCollectionsDto> result = userService.getAllUsersWithCollectionsAndAnimes();

        assertTrue(result.isEmpty());
        verify(userRepository).findAllWithCollections();
        // Разрешаем вызов с пустым списком, так как это нормальное поведение
        verify(collectionRepository).fetchCollectionsWithAnimes(Collections.emptyList());
    }
}