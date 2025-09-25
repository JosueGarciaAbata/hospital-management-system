package consulting_service.dtos.response;

import io.swagger.v3.oas.annotations.media.Schema;
import java.time.LocalDate;

@Schema(description = "Respuesta que representa los datos de un paciente")
public record PatientResponseDTO(

        @Schema(description = "Identificador único del paciente", example = "101")
        Long id,

        @Schema(description = "Número de cédula del paciente", example = "1728394056")
        String dni,

        @Schema(description = "Nombre del paciente", example = "Juan")
        String firstName,

        @Schema(description = "Apellido del paciente", example = "Pérez")
        String lastName,

        @Schema(description = "Fecha de nacimiento", example = "1990-05-21")
        LocalDate birthDate,

        @Schema(description = "Género del paciente", example = "FEMALE")
        String gender,

        @Schema(description = "Identificador del centro médico asociado", example = "5")
        Long centerId
) {}
