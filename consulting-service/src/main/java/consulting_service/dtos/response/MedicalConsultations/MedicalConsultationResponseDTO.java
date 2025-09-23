package consulting_service.dtos.response.MedicalConsultations;

import consulting_service.dtos.response.PatientResponseDTO;
import lombok.Data;

import java.time.LocalDateTime;


@Data
public class MedicalConsultationResponseDTO {
    private Long id;
    private PatientResponseDTO patient;
    private DoctorReadDTO doctor;
    private MedicalCenterReadDTO center;
    private LocalDateTime date;
    private String diagnosis;
    private String treatment;
    private String notes;

}