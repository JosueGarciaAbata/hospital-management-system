package consulting_service.repositories;

import consulting_service.entities.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


public interface PatientRepository  extends JpaRepository<Patient,Long> {

}
