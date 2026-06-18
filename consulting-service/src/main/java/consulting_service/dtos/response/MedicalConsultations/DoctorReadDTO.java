package consulting_service.dtos.response.MedicalConsultations;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
@Schema(description = "Doctor asociado a una consulta médica")
public class DoctorReadDTO {

    @Schema(description = "Identificador único del doctor", example = "301")
    private Long id;

    @Schema(description = "Nombre del doctor", example = "Carlos")
    private String firstName;

    @Schema(description = "Apellido del doctor", example = "Pérez")
    private String lastName;
}
