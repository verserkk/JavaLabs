package com.example.animeservice.service;

import com.example.animeservice.cache.CacheService;
import com.example.animeservice.dto.AnimeDto;
import com.example.animeservice.exception.EntityNotFoundException;
import com.example.animeservice.model.Anime;
import com.example.animeservice.model.Collection;
import com.example.animeservice.repository.AnimeRepository;
import com.example.animeservice.repository.CollectionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AnimeServiceTest {

    @Mock
    private AnimeRepository animeRepository;

    @Mock
    private CollectionRepository collectionRepository;

    @Mock
    private CacheService cacheService;

    @InjectMocks
    private AnimeService animeService;

    private Anime anime;
    private AnimeDto animeDto;
    private Collection collection;

    @BeforeEach
    void setUp() {
        anime = new Anime(1L, "Naruto", "Action", 2002, new ArrayList<>());
        animeDto = new AnimeDto(1L, "Naruto", "Action", 2002);
        collection = new Collection(1L, "My Collection", null, null, new ArrayList<>());
    }

    @Test
    void getAllAnimes_ReturnsListOfAnimeDtos() {
        when(animeRepository.findAll(any(Sort.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.getAllAnimes();

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(animeRepository).findAll(Sort.by("title"));
    }

    @Test
    void getAllAnimes_EmptyList_ReturnsEmptyList() {
        when(animeRepository.findAll(any(Sort.class))).thenReturn(Collections.emptyList());

        List<AnimeDto> result = animeService.getAllAnimes();

        assertTrue(result.isEmpty());
        verify(animeRepository).findAll(Sort.by("title"));
    }

    @Test
    void getAnimeById_CacheHit_ReturnsCachedAnimeDto() {
        when(cacheService.get("anime_1")).thenReturn(animeDto);

        AnimeDto result = animeService.getAnimeById(1L);

        assertEquals(animeDto, result);
        verify(cacheService).get("anime_1");
        verify(animeRepository, never()).findById(anyLong());
    }

    @Test
    void getAnimeById_CacheMiss_ReturnsAnimeDto() {
        when(cacheService.get("anime_1")).thenReturn(null);
        when(animeRepository.findById(1L)).thenReturn(Optional.of(anime));

        AnimeDto result = animeService.getAnimeById(1L);

        assertEquals(animeDto, result);
        verify(cacheService).get("anime_1");
        verify(animeRepository).findById(1L);
        verify(cacheService).put("anime_1", result);
    }

    @Test
    void getAnimeById_NotFound_ThrowsEntityNotFoundException() {
        when(cacheService.get("anime_1")).thenReturn(null);
        when(animeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> animeService.getAnimeById(1L));
        verify(cacheService).get("anime_1");
        verify(animeRepository).findById(1L);
        verify(cacheService, never()).put(anyString(), any());
    }

    @Test
    void createAnime_Success_ReturnsAnimeDto() {
        when(animeRepository.save(any(Anime.class))).thenReturn(anime);

        AnimeDto result = animeService.createAnime(animeDto);

        assertEquals(animeDto, result);
        verify(animeRepository).save(any(Anime.class));
        verify(cacheService, times(3)).invalidateByPrefix(anyString());
    }

    @Test
    void updateAnime_Success_ReturnsUpdatedAnimeDto() {
        when(animeRepository.findById(1L)).thenReturn(Optional.of(anime));
        when(animeRepository.save(any(Anime.class))).thenReturn(anime);

        AnimeDto result = animeService.updateAnime(1L, animeDto);

        assertEquals(animeDto, result);
        verify(animeRepository).findById(1L);
        verify(animeRepository).save(any(Anime.class));
        verify(cacheService, times(3)).invalidateByPrefix(anyString());
    }

    @Test
    void updateAnime_NotFound_ThrowsEntityNotFoundException() {
        when(animeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> animeService.updateAnime(1L, animeDto));
        verify(animeRepository).findById(1L);
        verify(animeRepository, never()).save(any());
    }

    @Test
    void deleteAnime_Success_CompletesWithoutException() {
        collection.setAnimes(new ArrayList<>(Arrays.asList(anime)));
        anime.setCollections(Arrays.asList(collection));
        when(animeRepository.findById(1L)).thenReturn(Optional.of(anime));
        when(collectionRepository.saveAll(anyList())).thenReturn(Collections.emptyList());

        animeService.deleteAnime(1L);

        verify(animeRepository).findById(1L);
        verify(collectionRepository).saveAll(anyList());
        verify(animeRepository).delete(eq(anime));
        verify(cacheService).invalidate("anime_1");
        verify(cacheService, times(2)).invalidateByPrefix(anyString());
    }

    @Test
    void deleteAnime_NotFound_ThrowsEntityNotFoundException() {
        when(animeRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(EntityNotFoundException.class, () -> animeService.deleteAnime(1L));
        verify(animeRepository).findById(1L);
        verify(animeRepository, never()).delete(any(Anime.class));
        verify(cacheService, never()).invalidate(anyString());
    }

    @Test
    void deleteAnime_NoCollections_CompletesWithoutException() {
        anime.setCollections(Collections.emptyList());
        when(animeRepository.findById(1L)).thenReturn(Optional.of(anime));
        when(collectionRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        animeService.deleteAnime(1L);

        verify(animeRepository).findById(1L);
        verify(collectionRepository).saveAll(Collections.emptyList());
        verify(animeRepository).delete(eq(anime));
        verify(cacheService).invalidate("anime_1");
        verify(cacheService, times(2)).invalidateByPrefix(anyString());
    }

    @Test
    void searchAnimes_CacheHit_ReturnsCachedList() {
        List<AnimeDto> cachedList = Arrays.asList(animeDto);
        when(cacheService.get("anime_search_Naruto_Action_2002")).thenReturn(cachedList);

        List<AnimeDto> result = animeService.searchAnimes("Naruto", "Action", 2002);

        assertEquals(cachedList, result);
        verify(cacheService).get("anime_search_Naruto_Action_2002");
        verify(animeRepository, never()).findAll(any(Specification.class));
    }

    @Test
    void searchAnimes_CacheMiss_ReturnsFilteredList() {
        when(cacheService.get("anime_search_Naruto_Action_2002")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes("Naruto", "Action", 2002);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search_Naruto_Action_2002");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search_Naruto_Action_2002", result);
    }

    @Test
    void searchAnimes_EmptyResult_ReturnsEmptyList() {
        when(cacheService.get("anime_search_Naruto_Action_2002")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Collections.emptyList());

        List<AnimeDto> result = animeService.searchAnimes("Naruto", "Action", 2002);

        assertTrue(result.isEmpty());
        verify(cacheService).get("anime_search_Naruto_Action_2002");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search_Naruto_Action_2002", result);
    }

    @Test
    void searchAnimes_OnlyTitle_ReturnsFilteredList() {
        when(cacheService.get("anime_search_Naruto__")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes("Naruto", null, null);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search_Naruto__");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search_Naruto__", result);
    }

    @Test
    void searchAnimes_OnlyGenre_ReturnsFilteredList() {
        when(cacheService.get("anime_search__Action_")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes(null, "Action", null);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search__Action_");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search__Action_", result);
    }

    @Test
    void searchAnimes_OnlyReleaseYear_ReturnsFilteredList() {
        when(cacheService.get("anime_search___2002")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes(null, null, 2002);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search___2002");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search___2002", result);
    }

    @Test
    void searchAnimes_TitleAndGenre_ReturnsFilteredList() {
        when(cacheService.get("anime_search_Naruto_Action_")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes("Naruto", "Action", null);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search_Naruto_Action_");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search_Naruto_Action_", result);
    }

    @Test
    void searchAnimes_TitleAndReleaseYear_ReturnsFilteredList() {
        when(cacheService.get("anime_search_Naruto__2002")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes("Naruto", null, 2002);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search_Naruto__2002");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search_Naruto__2002", result);
    }

    @Test
    void searchAnimes_GenreAndReleaseYear_ReturnsFilteredList() {
        when(cacheService.get("anime_search__Action_2002")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes(null, "Action", 2002);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search__Action_2002");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search__Action_2002", result);
    }

    @Test
    void searchAnimes_AllNull_ReturnsAllAnimes() {
        when(cacheService.get("anime_search___")).thenReturn(null);
        when(animeRepository.findAll(any(Specification.class))).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.searchAnimes(null, null, null);

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(cacheService).get("anime_search___");
        verify(animeRepository).findAll(any(Specification.class));
        verify(cacheService).put("anime_search___", result);
    }

    @Test
    void createAnimes_Success_ReturnsListOfAnimeDtos() {
        when(animeRepository.saveAll(anyList())).thenReturn(Arrays.asList(anime));

        List<AnimeDto> result = animeService.createAnimes(Arrays.asList(animeDto));

        assertEquals(1, result.size());
        assertEquals(animeDto, result.get(0));
        verify(animeRepository).saveAll(anyList());
        verify(cacheService, times(3)).invalidateByPrefix(anyString());
    }

    @Test
    void createAnimes_EmptyList_ReturnsEmptyList() {
        when(animeRepository.saveAll(Collections.emptyList())).thenReturn(Collections.emptyList());

        List<AnimeDto> result = animeService.createAnimes(Collections.emptyList());

        assertTrue(result.isEmpty());
        verify(animeRepository).saveAll(Collections.emptyList());
        verify(cacheService).invalidateByPrefix("anime_");
        verify(cacheService).invalidateByPrefix("anime_search_");
        verify(cacheService).invalidateByPrefix("collection_search_anime_");
    }
}