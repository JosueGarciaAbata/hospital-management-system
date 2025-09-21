package consulting_service.repositories;

import consulting_service.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface PatientRepository  extends JpaRepository<Patient,Long> {

    List<Patient> findByCenterIdAndDeletedFalse(Long centerId);
    Optional<Patient> findByIdAndDeletedFalse(Long id);
    boolean existsByDni(String dni);
    Optional<Patient> findByDniAndIdNot(String dni, Long id);

}
