package consulting_service.services.MedicalConsultations;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.entities.MedicalConsultation;

import java.util.List;


public interface MedicalConsultationsService {

   List<MedicalConsultationResponseDTO> getMedicalConsultations(Long doctorId);

   MedicalConsultationResponseDTO getMedicalConsultationById(Long id);
   MedicalConsultationResponseDTO addMedicalConsultation(MedicalConsultationRequestDTO request);

   MedicalConsultationResponseDTO updateMedicalConsultation(Long id,MedicalConsultationRequestDTO request);

   void deleteMedicalConsultation(Long id);
   boolean centerHasConsultations(Long centerId);
   boolean doctorHasConsultations(Long doctorId);
}
