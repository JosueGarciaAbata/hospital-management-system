package com.hospital.repositories;

import com.hospital.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(
            value = "SELECT * FROM users",
            countQuery = "SELECT COUNT(*) FROM users",
            nativeQuery = true
    )
    Page<User> findAllIncludingDeleted(Pageable pageable);

    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllTesting();

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<User> findUserById(Long id);

    // Usuarios activos (implícito gracias a @SoftDelete)
    Optional<User> findFirstActiveByCenterId(Long centerId);
    boolean existsActiveByCenterId(Long centerId);

    // Usuarios incluyendo deshabilitados (usando native query)
    @Query(value = "SELECT * FROM users u WHERE u.center_id = :centerId", nativeQuery = true)
        Optional<User> findFirstByCenterId(Long centerId);
    @Query(value = "SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM users u WHERE u.center_id = :centerId", nativeQuery = true)
    boolean existsByCenterId(Long centerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :input OR u.email = :input")
    Optional<User> findByUsernameOrEmail(@Param("input") String input);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
    int hardDeleteById(@Param("userId") Long userId);
}
