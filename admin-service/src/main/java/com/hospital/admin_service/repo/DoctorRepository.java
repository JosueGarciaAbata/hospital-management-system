package com.hospital.admin_service.repo;

import com.hospital.admin_service.model.Doctor;
import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    @Query(value = "SELECT * FROM doctors ORDER BY id", nativeQuery = true)
    List<Doctor> findAllIncludingDeleted();

    @Query(
            value = "SELECT * FROM doctors ORDER BY id",
            countQuery = "SELECT COUNT(*) FROM doctors",
            nativeQuery = true
    )
    Page<Doctor> findAllIncludingDeletedPage(Pageable pageable);

    @Query(value = "SELECT * FROM doctors WHERE id = :id", nativeQuery = true)
    Optional<Doctor> findByIdIncludingDeleted(@Param("id") Long id);

    Optional<Doctor> findByUserId(Long userId);
    Page<Doctor> findAllBySpecialty_Id(Long specialtyId, Pageable pageable);
    long countBySpecialty_Id(Long specialtyId);

    boolean existsByUserId(Long userId);

    boolean existsByUserIdAndIdNot(Long userId, Long id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select d from Doctor d where d.id = :id")
    Optional<Doctor> lockById(@Param("id") Long id);
}
