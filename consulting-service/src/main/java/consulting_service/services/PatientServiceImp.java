package consulting_service.services;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.entities.Patient;
import consulting_service.exceptions.DuplicateDniException;
import consulting_service.exceptions.NotFoundException;
import consulting_service.mappers.PatientMapper;
import consulting_service.repositories.PatientRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class PatientServiceImp  implements  PatientService{

    private final PatientRepository repository;
    private final PatientMapper mapper;

    public PatientServiceImp(PatientRepository repository,PatientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<Patient> getPatients(Long centerId) {

        return repository.findByCenterIdAndDeletedFalse(centerId);
    }

    @Override
    public Patient getPatient( Long id,Long centerId) {
       return repository.findByIdAndCenterIdAndDeletedFalse(id,centerId).orElseThrow(
               ()-> new NotFoundException("Paciente no encontrado")
       );
    }


    @Override
    public Patient addPatient(PatientRequestDTO request) {

        Patient patient = mapper.toEntity(request);

        if(repository.existsByDni(patient.getDni())) {
            throw  new DuplicateDniException("El DNI ya existe");
        }

        return repository.save(patient);

    }


}
