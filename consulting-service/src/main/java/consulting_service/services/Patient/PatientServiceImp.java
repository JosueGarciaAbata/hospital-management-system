package consulting_service.services.Patient;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import consulting_service.exceptions.DuplicateDniException;
import consulting_service.exceptions.NotFoundException;
import consulting_service.mappers.PatientMapper;
import consulting_service.repositories.PatientRepository;

import org.springframework.data.domain.PageRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientServiceImp  implements  PatientService{

    private final PatientRepository repository;
    private final PatientMapper mapper;

    public PatientServiceImp(PatientRepository repository,PatientMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public Patient getPatient( Long id) {
       return repository.findByIdAndDeletedFalse(id).orElseThrow(
               ()-> new NotFoundException("Paciente no encontrado")
       );
    }

    @Override
    public Page<PatientResponseDTO> getPatients(Long centerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id"));

        return repository.findByCenterIdAndDeletedFalse(centerId, pageable)
                .map(mapper::toDTO);
    }



    @Override
    public Patient addPatient(PatientRequestDTO request) {

        Patient patient = mapper.toEntity(request);

        if(repository.existsByDni(patient.getDni())) {
            throw  new DuplicateDniException("El DNI ya existe");
        }

        return repository.save(patient);

    }

    @Override
    public Patient updatePatient(Long id, PatientRequestDTO request) {


        Patient patient = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paciente no encontrado"));

        repository.findByDniAndIdNot(request.dni(), id)
                .ifPresent(p -> {
                    throw new DuplicateDniException("El DNI está registrado con otro paciente");
                });


        mapper.updateEntityFromDto(request, patient);

        return repository.save(patient);

    }

    @Override
    public void deletePatient(Long id) {

        Patient patient = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Paciente no encontrado"));

        patient.setDeleted(true);

        repository.save(patient);

    }

    @Override
    public PatientResponseDTO getPatientTC(Long id) {
        Patient patient  = repository.findById(id).orElseThrow(
                ()-> new NotFoundException("Paciente no encontrado")
        );
        return this.mapper.toDTO(patient);
    }

    @Override
    public boolean centerHasPatients(Long centerId) {
        return repository.existsByCenterIdAndDeletedFalse(centerId);
    }


}
