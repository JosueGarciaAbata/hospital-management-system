package consulting_service.repositories;

import consulting_service.entities.Patient;

import feign.Param;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;


public interface PatientRepository extends JpaRepository<Patient, Long> {


    @Query("SELECT p FROM Patient p " +
            "WHERE p.centerId = :centerId AND p.deleted = false ")
    Page<Patient> findByCenterIdAndDeletedFalse(@Param("centerId") Long centerId, Pageable pageable);

    Optional<Patient> findByIdAndDeletedFalse(Long id);

    boolean existsByDni(String dni);

    boolean existsByCenterIdAndDeletedFalse(Long centerId);

    Optional<Patient> findByDniAndIdNot(String dni, Long id);

    List<Patient> findAllByCenterIdAndDeletedFalse(Long centerId);
}
