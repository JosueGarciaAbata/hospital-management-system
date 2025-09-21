package consulting_service.services;

import consulting_service.entities.Patient;
import consulting_service.exceptions.NotFoundException;
import consulting_service.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PatientServiceImp  implements  PatientService{

    private final PatientRepository repository;

    public PatientServiceImp(PatientRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<Patient> getPatients(Long centerId) {

        return repository.findByCenterId(centerId);
    }

    @Override
    public Patient getPatient( Long id,Long centerId) {
       return repository.findByIdAndCenterId(id,centerId).orElseThrow(
               ()-> new NotFoundException("Paciente no encontrado")
       );
    }
}
