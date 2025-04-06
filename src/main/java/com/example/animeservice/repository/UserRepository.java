package com.example.animeservice.repository;

import com.example.animeservice.model.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;


@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    boolean existsByUsername(String username);

    boolean existsByEmail(String email);

    List<User> findByUsernameContainingIgnoreCase(String username);

    List<User> findByEmailContainingIgnoreCase(String email);

    @SuppressWarnings("checkstyle:OperatorWrap")
    @Query("SELECT u FROM User u WHERE " +
            "LOWER(u.username) LIKE LOWER(CONCAT('%', :username, '%')) OR " +
            "LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%'))")
    List<User> searchByUsernameOrEmail(
            @Param("username") String username,
            @Param("email") String email
    );
}