package consulting_service.services.MedicalConsultations;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.entities.MedicalConsultation;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;


public interface MedicalConsultationsService {

   Page<MedicalConsultationResponseDTO> getMedicalConsultations(Long doctorId, int page, int size);

   MedicalConsultationResponseDTO getMedicalConsultationById(Long id);
   MedicalConsultationResponseDTO addMedicalConsultation(MedicalConsultationRequestDTO request);

   MedicalConsultationResponseDTO updateMedicalConsultation(Long id,MedicalConsultationRequestDTO request);

   void deleteMedicalConsultation(Long id);
   boolean centerHasConsultations(Long centerId);
   boolean doctorHasConsultations(Long doctorId);

   Page<MedicalConsultationResponseDTO> getMedicalConsultationsByCenter(Long centerId, int page, int size);
   Page<MedicalConsultationResponseDTO> getAllMedicalConsultations(int page, int size);

   Page<MedicalConsultationResponseDTO> getMedicalConsultationsBySpecialty(Long specialtyId, int page, int size);
}
