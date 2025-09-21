package consulting_service.services.MedicalConsultations;

import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.entities.MedicalConsultation;

import java.util.List;


public interface MedicalConsultationsService {

   List<MedicalConsultationResponseDTO> getMedicalConsultations(Long doctorId);
   MedicalConsultationResponseDTO getMedicalConsultationById(Long id);
}
