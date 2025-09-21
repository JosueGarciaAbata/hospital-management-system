package com.hospital.admin_service.repo;

import com.hospital.admin_service.model.Specialty;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface SpecialtyRepository extends JpaRepository<Specialty, Long> {

    @Query(value = "SELECT * FROM specialties ORDER BY id", nativeQuery = true)
    List<Specialty> findAllIncludingDeleted();

    @Query(
            value = "SELECT * FROM specialties ORDER BY id",
            countQuery = "SELECT COUNT(*) FROM specialties",
            nativeQuery = true
    )
    Page<Specialty> findAllIncludingDeletedPage(Pageable pageable);

    @Query(value = "SELECT * FROM specialties WHERE id = :id", nativeQuery = true)
    Optional<Specialty> findByIdIncludingDeleted(@Param("id") Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select s from Specialty s where s.id = :id")
    Optional<Specialty> lockById(@Param("id") Long id);
}
