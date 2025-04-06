package com.example.animeservice.repository;

import com.example.animeservice.model.Collection;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;



public interface CollectionRepository extends JpaRepository<Collection, Long> {
    List<Collection> findByUserId(Long userId);

    List<Collection> findByNameContainingIgnoreCase(String name);

    @Query("SELECT c FROM Collection c JOIN c.animes a WHERE a.id = :animeId")
    List<Collection> findByAnimesId(@Param("animeId") Long animeId);
}