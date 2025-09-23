package consulting_service.services.Patient;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;

import java.util.List;


public interface PatientService {

    Patient addPatient(PatientRequestDTO request);
    Patient getPatient(Long id);
    List<Patient> getPatients(Long centerId);
    Patient  updatePatient(Long id,PatientRequestDTO request);
    void  deletePatient(Long id);
    PatientResponseDTO getPatientTC(Long id);
}
