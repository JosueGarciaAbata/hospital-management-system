package com.hospital.repositories;

import com.hospital.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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

    @Query(
            value = "SELECT * FROM users u WHERE u.id <> :excludedUserId AND u.enabled = true",
            countQuery = "SELECT COUNT(*) FROM users u WHERE u.id <> :excludedUserId",
            nativeQuery = true
    )
    Page<User> findAllExcludingUser(@Param("excludedUserId") Long excludedUserId, Pageable pageable);

    @Query(
            value = "SELECT * FROM users u WHERE u.id <> :excludedUserId",
            countQuery = "SELECT COUNT(*) FROM users u WHERE u.id <> :excludedUserId",
            nativeQuery = true
    )
    Page<User> findAllIncludingDeletedExcludingUser(@Param("excludedUserId") Long excludedUserId, Pageable pageable);


    @Query(value = "SELECT * FROM users", nativeQuery = true)
    List<User> findAllTesting();

    @Query(value = "SELECT exists (SELECT 1 FROM users u WHERE u.dni = :dni)", nativeQuery = true)
    boolean existsByUsername(@Param("dni") String username);

    @Query(value = "SELECT exists (SELECT 1 FROM users u WHERE u.email = :email)", nativeQuery = true)
    boolean existsByEmail(@Param("email") String email);

    @EntityGraph(attributePaths = {"roles"})
    Optional<User> findUserById(Long id);

    @Query(value = "SELECT * FROM users u WHERE u.id = :id", nativeQuery = true)
    Optional<User> findUserByIdIncludingDisabled(@Param("id") Long id);

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
