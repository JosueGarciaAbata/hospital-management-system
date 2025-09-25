package consulting_service.dtos.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "Petición para crear o actualizar una consulta médica")
public class MedicalConsultationRequestDTO {

    @Schema(description = "Identificador del paciente asociado a la consulta", example = "2001")
    @NotNull(message = "El paciente es obligatorio")
    private Long patientId;

    @Schema(description = "Identificador del doctor que atiende la consulta", example = "301")
    @NotNull(message = "El doctor es obligatorio")
    private Long doctorId;

    @Schema(description = "Identificador del centro médico donde se realiza la consulta", example = "5")
    @NotNull(message = "El centro medico es obligatorio")
    private Long centerId;

    @Schema(description = "Fecha y hora de la consulta", example = "2025-09-24T10:30:00")
    @NotNull(message = "La fecha es obligatiria")
    private LocalDateTime date;

    @Schema(description = "Diagnóstico realizado en la consulta", example = "Hipertensión arterial")
    private String diagnosis;

    @Schema(description = "Tratamiento indicado", example = "Losartán 50mg cada 12h")
    private String treatment;

    @Schema(description = "Notas adicionales", example = "Control en 15 días")
    private String notes;
}
