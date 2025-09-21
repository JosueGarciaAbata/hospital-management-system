package consulting_service.dtos.request;

import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalCenterReadDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MedicalConsultationRequestDTO{

    @NotNull(message="El paciente es obligatorio")
    private Long patientId;
    @NotNull(message="El doctor es obligatorio")
    private Long doctorId;
    @NotNull(message="El centro medico es obligatorio")
    private Long centerId;
    @NotNull(message="La fecha es obligatiria")
    private LocalDateTime date;


    private String diagnosis;
    private String treatment;
    private String notes;

}
