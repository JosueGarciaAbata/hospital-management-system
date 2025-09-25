package consulting_service.dtos.response.MedicalConsultations;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Centro médico asociado a una consulta")
public record MedicalCenterReadDTO(

        @Schema(description = "Identificador único del centro médico", example = "5")
        Long id,

        @Schema(description = "Nombre del centro médico", example = "Hospital Central")
        String name
) {}
