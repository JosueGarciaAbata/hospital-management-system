package consulting_service.repositories;

import consulting_service.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


public interface PatientRepository  extends JpaRepository<Patient,Long> {

    Optional<Patient> findByIdAndCenterId(Long id,Long centerId);
    List<Patient> findByCenterId(Long centerId);

}
