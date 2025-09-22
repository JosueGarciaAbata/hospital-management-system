package com.hospital.repositories;

import com.hospital.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    boolean existsByUsername(String username);

    Optional<User> findUserById(Long id);

    Optional<User> findFirstByCenterIdAndEnabledTrue(Long centerId);
    boolean existsByCenterIdAndEnabledTrue(Long centerId);

    Optional<User> findFirstByCenterId(Long centerId);
    boolean existsByCenterId(Long centerId);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.roles WHERE u.username = :username")
    Optional<User> findByUsername(String username);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query(value = "DELETE FROM users WHERE id = :userId", nativeQuery = true)
    int hardDeleteById(@Param("userId") Long userId);
}
