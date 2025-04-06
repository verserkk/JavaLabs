package com.example.animeservice.specs;

import com.example.animeservice.model.Anime;
import org.springframework.data.jpa.domain.Specification;

public class AnimeSpecifications {
    public static Specification<Anime> withTitle(String title) {
        return (root, query, cb) ->
                title == null ? null :
                        cb.like(cb.lower(root.get("title")), "%" + title.toLowerCase() + "%");
    }

    public static Specification<Anime> withGenre(String genre) {
        return (root, query, cb) ->
                genre == null ? null : cb.equal(root.get("genre"), genre);
    }

    public static Specification<Anime> withReleaseYear(Integer year) {
        return (root, query, cb) ->
                year == null ? null : cb.equal(root.get("releaseYear"), year);
    }
}