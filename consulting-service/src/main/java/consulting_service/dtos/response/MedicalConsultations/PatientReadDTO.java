package consulting_service.dtos.response.MedicalConsultations;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Información resumida del paciente asociada a una consulta médica")
public record PatientReadDTO(

        @Schema(description = "Identificador único del paciente", example = "101")
        Long id,

        @Schema(description = "Nombre del paciente", example = "Juan")
        String firstName,

        @Schema(description = "Apellido del paciente", example = "Pérez")
        String lastName
) {}
