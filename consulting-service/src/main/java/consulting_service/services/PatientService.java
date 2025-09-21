package consulting_service.services;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.entities.Patient;
import consulting_service.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;


public interface PatientService {

    Patient addPatient(PatientRequestDTO patient);
    Patient getPatient(Long id,Long centerId);
    List<Patient> getPatients(Long centerId);

}
