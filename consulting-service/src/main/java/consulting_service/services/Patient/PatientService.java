package consulting_service.services.Patient;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import org.springframework.data.domain.Page;

import java.util.List;


public interface PatientService {

    Patient addPatient(PatientRequestDTO request);
    Patient getPatient(Long id);
    Page<PatientResponseDTO> getPatients(Long centerId, int page, int size);
    Patient  updatePatient(Long id,PatientRequestDTO request);
    void  deletePatient(Long id);
    PatientResponseDTO getPatientTC(Long id);
    boolean centerHasPatients(Long centerId);

}
