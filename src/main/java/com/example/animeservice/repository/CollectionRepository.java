package com.example.animeservice.repository;

import com.example.animeservice.model.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserId(Long userId);

    List<Collection> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Collection c JOIN c.animes a WHERE a.id = :animeId")
    List<Collection> findByAnimesId(@Param("animeId") Long animeId);

    @Query("SELECT DISTINCT c FROM Collection c LEFT JOIN FETCH c.animes WHERE c IN :collections")
    List<Collection> fetchCollectionsWithAnimes(@Param("collections") List<Collection> collections);

    @Query(
            value = """
        SELECT DISTINCT c.id as collection_id, c.name as collection_name,
                    a.id as anime_id, a.title, a.genre, a.release_year
            FROM collections c
            JOIN collection_anime ca ON c.id = ca.collection_id
            JOIN animes a ON ca.anime_id = a.id
            WHERE (:title IS NULL OR a.title ILIKE CONCAT('%', :title, '%'))
             AND (:genre IS NULL OR a.genre ILIKE CONCAT('%', :genre, '%'))
             AND (:releaseYear IS NULL OR a.release_year = :releaseYear)
            ORDER BY c.id, a.id""",
            nativeQuery = true
    )
    List<Object[]> searchCollectionsWithAnimeByParams(
            @Param("title") String title,
            @Param("genre") String genre,
            @Param("releaseYear") Integer releaseYear
    );
}