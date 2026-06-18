package consulting_service.dtos.response.MedicalConsultations;

import consulting_service.dtos.response.PatientResponseDTO;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Respuesta que representa una consulta médica")
public class MedicalConsultationResponseDTO {

    @Schema(description = "Identificador único de la consulta", example = "101")
    private Long id;

    @Schema(description = "Paciente asociado a la consulta")
    private PatientResponseDTO patient;

    @Schema(description = "Doctor que atendió la consulta")
    private DoctorReadDTO doctor;

    @Schema(description = "Centro médico donde se realizó la consulta")
    private MedicalCenterReadDTO center;

    @Schema(description = "Fecha y hora de la consulta", example = "2025-09-24T10:30:00")
    private LocalDateTime date;

    @Schema(description = "Diagnóstico registrado en la consulta", example = "Hipertensión arterial")
    private String diagnosis;

    @Schema(description = "Tratamiento indicado en la consulta", example = "Losartán 50mg cada 12h")
    private String treatment;

    @Schema(description = "Notas adicionales de la consulta", example = "Paciente debe regresar en 15 días")
    private String notes;
}
