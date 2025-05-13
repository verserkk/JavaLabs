package com.example.animeservice.service;

import com.example.animeservice.cache.CacheService;
import com.example.animeservice.dto.CollectionDto;
import com.example.animeservice.dto.CollectionWithAnimeDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.model.Collection;
import com.example.animeservice.model.User;
import com.example.animeservice.repository.AnimeRepository;
import com.example.animeservice.repository.CollectionRepository;
import com.example.animeservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CollectionServiceTest {

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private CollectionService collectionService;

    private User user;
    private Anime anime;
    private Collection collection;
    private CollectionDto collectionDto;

    @BeforeEach
    void setUp() {
        user = new User(1L, "testuser", "test@example.com", new ArrayList<>());
        anime = new Anime(1L, "Naruto", "Action", 2002, new ArrayList<>());
        collection = new Collection(1L, "My Collection", user, null, new ArrayList<>(Arrays.asList(anime)));
        collectionDto = new CollectionDto(1L, "My Collection", 1L, Arrays.asList(1L));
    }

    @Test
    void getAllCollections_ReturnsListOfCollectionDtos() {
        when(collectionRepository.findAll()).thenReturn(Arrays.asList(collection));

       List<CollectionDto> result = collectionService.getAllCollections();

        assertEquals(1, result.size());
        assertEquals(collectionDto, result.get(0));
        verify(collectionRepository).findAll();
    }

    @Test
    void getAllCollections_EmptyList_ReturnsEmptyList() {
        when(collectionRepository.findAll()).thenReturn(Collections.emptyList());

        List<CollectionDto> result = collectionService.getAllCollections();

        assertTrue(result.isEmpty());
        verify(collectionRepository).findAll();
    }

    @Test
    void getCollectionById_CacheHit_ReturnsCachedCollectionDto() {
        when(cacheService.get("collection_1")).thenReturn(collectionDto);

        CollectionDto result = collectionService.getCollectionById(1L);

        assertEquals(collectionDto, result);
        verify(cacheService).get("collection_1");
        verify(collectionRepository, never()).findById(anyLong());
    }

    @Test
    void getCollectionById_CacheMiss_ReturnsCollectionDto() {
        when(cacheService.get("collection_1")).thenReturn(null);
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));

        CollectionDto result = collectionService.getCollectionById(1L);

        assertEquals(collectionDto, result);
        verify(cacheService).get("collection_1");
        verify(collectionRepository).findById(1L);
        verify(cacheService).put("collection_1", result);
    }



    @Test
    void createCollection_Success_ReturnsCollectionDto() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animeRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(anime));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        CollectionDto result = collectionService.createCollection(collectionDto);

        assertEquals(collectionDto, result);
        verify(userRepository).findById(1L);
        verify(animeRepository).findAllById(Arrays.asList(1L));
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }

    @Test
    void createCollection_NoAnimes_Success_ReturnsCollectionDto() {
        CollectionDto noAnimeDto = new CollectionDto(1L, "My Collection", 1L, Collections.emptyList());
        Collection noAnimeCollection = new Collection(1L, "My Collection", user, null, new ArrayList<>());
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(collectionRepository.save(any(Collection.class))).thenReturn(noAnimeCollection);

        CollectionDto result = collectionService.createCollection(noAnimeDto);

        assertEquals(noAnimeDto, result);
        verify(userRepository).findById(1L);
        verify(animeRepository, never()).findAllById(anyList());
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }




    @Test
    void updateCollection_Success_ReturnsUpdatedCollectionDto() {
        User existingUser = new User(2L, "existing", "existing@example.com", new ArrayList<>());
        collection.setUser(existingUser);
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animeRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(anime));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        CollectionDto updatedDto = new CollectionDto(1L, "Updated Collection", 1L, Arrays.asList(1L));
        CollectionDto result = collectionService.updateCollection(1L, updatedDto);

        assertEquals(updatedDto, result);
        verify(collectionRepository).findById(1L);
        verify(userRepository).findById(1L);
        verify(animeRepository).findAllById(Arrays.asList(1L));
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }

    @Test
    void updateCollection_SameUserId_DoesNotCallUserRepository() {
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(animeRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(anime));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        CollectionDto updatedDto = new CollectionDto(1L, "Updated Collection", 1L, Arrays.asList(1L));
        CollectionDto result = collectionService.updateCollection(1L, updatedDto);

        assertEquals(updatedDto, result);
        verify(collectionRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(animeRepository).findAllById(Arrays.asList(1L));
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }

    @Test
    void updateCollection_NameNull_Success() {
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(animeRepository.findAllById(Arrays.asList(1L))).thenReturn(Arrays.asList(anime));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        CollectionDto updatedDto = new CollectionDto(1L, null, 1L, Arrays.asList(1L));
        CollectionDto result = collectionService.updateCollection(1L, updatedDto);

        assertEquals(1L, result.getId());
        assertEquals("My Collection", result.getName()); // Name should not change
        assertEquals(1L, result.getUserId());
        assertEquals(Arrays.asList(1L), result.getAnimeIds());
        verify(collectionRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(animeRepository).findAllById(Arrays.asList(1L));
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }

    @Test
    void updateCollection_AnimeIdsNull_Success() {
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(collectionRepository.save(any(Collection.class))).thenReturn(collection);

        CollectionDto updatedDto = new CollectionDto(1L, "Updated Collection", 1L, null);
        CollectionDto result = collectionService.updateCollection(1L, updatedDto);

        assertEquals(updatedDto.getName(), result.getName());
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertEquals(Arrays.asList(1L), result.getAnimeIds());
        verify(collectionRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(animeRepository, never()).findAllById(anyList());
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }

    @Test
    void updateCollection_EmptyAnimeIds_Success() {
        Collection updatedCollection = new Collection(1L, "Updated Collection", user, null, new ArrayList<>());
        when(collectionRepository.findById(1L)).thenReturn(Optional.of(collection));
        when(collectionRepository.save(any(Collection.class))).thenReturn(updatedCollection);

        CollectionDto updatedDto = new CollectionDto(1L, "Updated Collection", 1L, Collections.emptyList());
        CollectionDto result = collectionService.updateCollection(1L, updatedDto);

        assertEquals(updatedDto.getName(), result.getName());
        assertEquals(1L, result.getId());
        assertEquals(1L, result.getUserId());
        assertTrue(result.getAnimeIds().isEmpty());
        verify(collectionRepository).findById(1L);
        verify(userRepository, never()).findById(anyLong());
        verify(animeRepository).findAllById(Collections.emptyList());
        verify(collectionRepository).save(any(Collection.class));
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }



    @Test
    void deleteCollection_Success_CompletesWithoutException() {
        when(collectionRepository.existsById(1L)).thenReturn(true);

        collectionService.deleteCollection(1L);

        verify(collectionRepository).existsById(1L);
        verify(collectionRepository).deleteById(1L);
        verify(cacheService).invalidate("collection_1");
        verify(cacheService, times(4)).invalidateByPrefix(anyString());
    }



    @Test
    void getCollectionsByUser_CacheHit_ReturnsCachedList() {
        List<CollectionDto> cachedList = Arrays.asList(collectionDto);
        when(cacheService.get("collections_user_1")).thenReturn(cachedList);

        List<CollectionDto> result = collectionService.getCollectionsByUser(1L);

        assertEquals(cachedList, result);
        verify(cacheService).get("collections_user_1");
        verify(collectionRepository, never()).findByUserId(anyLong());
        verify(userRepository, never()).existsById(anyLong());
    }




    @Test
    void searchCollections_ByNameAndAnimeId_ReturnsFilteredList() {
        when(cacheService.get("collection_search_My Collection_1")).thenReturn(null);
        when(collectionRepository.findByNameContainingIgnoreCase("My Collection")).thenReturn(Arrays.asList(collection));

        List<CollectionDto> result = collectionService.searchCollections("My Collection", 1L);

        assertEquals(1, result.size());
        assertEquals(collectionDto, result.get(0));
        verify(cacheService).get("collection_search_My Collection_1");
        verify(collectionRepository).findByNameContainingIgnoreCase("My Collection");
        verify(cacheService).put("collection_search_My Collection_1", result);
    }

    @Test
    void searchCollections_ByNameOnly_ReturnsFilteredList() {
        when(cacheService.get("collection_search_My Collection_")).thenReturn(null);
        when(collectionRepository.findByNameContainingIgnoreCase("My Collection")).thenReturn(Arrays.asList(collection));

        List<CollectionDto> result = collectionService.searchCollections("My Collection", null);

        assertEquals(1, result.size());
        assertEquals(collectionDto, result.get(0));
        verify(cacheService).get("collection_search_My Collection_");
        verify(collectionRepository).findByNameContainingIgnoreCase("My Collection");
        verify(cacheService).put("collection_search_My Collection_", result);
    }

    @Test
    void searchCollections_ByAnimeIdOnly_ReturnsFilteredList() {
        when(cacheService.get("collection_search__1")).thenReturn(null);
        when(collectionRepository.findByAnimesId(1L)).thenReturn(Arrays.asList(collection));

        List<CollectionDto> result = collectionService.searchCollections(null, 1L);

        assertEquals(1, result.size());
        assertEquals(collectionDto, result.get(0));
        verify(cacheService).get("collection_search__1");
        verify(collectionRepository).findByAnimesId(1L);
        verify(cacheService).put("collection_search__1", result);
    }
    @Test
    void searchCollections_NoParameters_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> collectionService.searchCollections(null, null));
    }
    @Test
    void searchCollectionsByAnimeParams_Success_ReturnsCollections() {
        Object[] row = {1L, "My Collection", 1L, "Naruto", "Action", 2002};
        when(cacheService.get("collection_search_anime_Naruto_Action_2002")).thenReturn(null);
        when(collectionRepository.searchCollectionsWithAnimeByParams("Naruto", "Action", 2002))
                .thenReturn(Collections.singletonList(row));

        List<CollectionWithAnimeDto> result = collectionService.searchCollectionsByAnimeParams("Naruto", "Action", 2002);

        assertEquals(1, result.size());
        assertEquals("My Collection", result.get(0).getName());
        assertEquals(1, result.get(0).getAnimes().size());
        verify(cacheService).get("collection_search_anime_Naruto_Action_2002");
        verify(collectionRepository).searchCollectionsWithAnimeByParams("Naruto", "Action", 2002);
        verify(cacheService).put("collection_search_anime_Naruto_Action_2002", result);
    }

    @Test
    void searchCollectionsByAnimeParams_NullReleaseYear_Success() {
        Object[] row = {1L, "My Collection", 1L, "Naruto", "Action", null};
        when(cacheService.get("collection_search_anime_Naruto_Action_")).thenReturn(null);
        when(collectionRepository.searchCollectionsWithAnimeByParams("Naruto", "Action", null))
                .thenReturn(Collections.singletonList(row));

        List<CollectionWithAnimeDto> result = collectionService.searchCollectionsByAnimeParams("Naruto", "Action", null);

        assertEquals(1, result.size());
        assertEquals("My Collection", result.get(0).getName());
        assertEquals(1, result.get(0).getAnimes().size());
        assertNull(result.get(0).getAnimes().get(0).getReleaseYear());
        verify(cacheService).get("collection_search_anime_Naruto_Action_");
        verify(collectionRepository).searchCollectionsWithAnimeByParams("Naruto", "Action", null);
        verify(cacheService).put("collection_search_anime_Naruto_Action_", result);
    }

    @Test
    void searchCollectionsByAnimeParams_NoParameters_ThrowsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> collectionService.searchCollectionsByAnimeParams(null, null, null));
    }


    @Test
    void createCollections_Success_ReturnsListOfCollectionDtos() {
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(animeRepository.findById(1L)).thenReturn(Optional.of(anime));
        when(collectionRepository.saveAll(anyList())).thenReturn(Arrays.asList(collection));

        List<CollectionDto> result = collectionService.createCollections(Arrays.asList(collectionDto));

        assertEquals(1, result.size());
        assertEquals(collectionDto, result.get(0));
        verify(userRepository).findById(1L);
        verify(animeRepository).findById(1L);
        verify(collectionRepository).saveAll(anyList());
        verify(cacheService, times(5)).invalidateByPrefix(anyString());
    }

    @Test
    void createCollections_EmptyList_ReturnsEmptyList() {
        List<CollectionDto> result = collectionService.createCollections(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(userRepository, never()).findById(anyLong());
        verify(animeRepository, never()).findById(anyLong());
        verify(collectionRepository).saveAll(Collections.emptyList());
    }


}