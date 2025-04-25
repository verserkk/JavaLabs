package com.example.animeservice.repository;

import com.example.animeservice.model.Anime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface AnimeRepository extends JpaRepository<Anime, Long>,
        JpaSpecificationExecutor<Anime> {

}