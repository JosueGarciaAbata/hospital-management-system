    package com.hospital.admin_service.repo;

    import com.hospital.admin_service.model.MedicalCenter;
    import org.springframework.data.domain.Page;
    import org.springframework.data.domain.Pageable;
    import org.springframework.data.jpa.repository.*;
    import org.springframework.data.repository.query.Param;

    import jakarta.persistence.LockModeType;
    import java.util.Optional;
    import java.util.List;

    public interface MedicalCenterRepository extends JpaRepository<MedicalCenter, Long> {

        @Query(value = "SELECT * FROM medical_centers ORDER BY id", nativeQuery = true)
        List<MedicalCenter> findAllIncludingDeleted();

        @Query(
                value = "SELECT * FROM medical_centers ORDER BY id",
                countQuery = "SELECT COUNT(*) FROM medical_centers",
                nativeQuery = true
        )
        Page<MedicalCenter> findAllIncludingDeletedPage(Pageable pageable);

        @Query(value = "SELECT * FROM medical_centers WHERE id = :id", nativeQuery = true)
        Optional<MedicalCenter> findByIdIncludingDeleted(@Param("id") Long id);

        @Lock(LockModeType.PESSIMISTIC_WRITE)
        @Query("select m from MedicalCenter m where m.id = :id")
        Optional<MedicalCenter> lockById(@Param("id") Long id);

        boolean existsById(@Param("id") Long id);
    }
