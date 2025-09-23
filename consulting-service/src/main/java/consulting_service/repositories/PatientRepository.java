package consulting_service.repositories;

import consulting_service.entities.Patient;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import java.util.Optional;


public interface PatientRepository  extends JpaRepository<Patient,Long> {

    Page<Patient> findByCenterIdAndDeletedFalse(Long centerId, Pageable pageable);
    Optional<Patient> findByIdAndDeletedFalse(Long id);
    boolean existsByDni(String dni);
    boolean existsByCenterIdAndDeletedFalse(Long centerId);
    Optional<Patient> findByDniAndIdNot(String dni, Long id);
}
