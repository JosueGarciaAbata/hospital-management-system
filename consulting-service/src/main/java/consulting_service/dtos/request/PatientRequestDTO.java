package consulting_service.dtos.request;

import consulting_service.annotations.EcuadorianDni;
import consulting_service.enums.GenderType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

@Schema(description = "Petición para registrar o actualizar un paciente")
public record PatientRequestDTO(

        @Schema(description = "Número de cédula del paciente (10 dígitos válidos en Ecuador)", example = "1728394056")
        @NotBlank(message="La cedula es obligatoria")
        @Size(min=10,max=10,message="La cedula debe tener 10 digitos")
        @EcuadorianDni
        String dni,

        @Schema(description = "Nombre del paciente", example = "Juan")
        @NotBlank(message="El nombre es obligatorio")
        String firstName,

        @Schema(description = "Apellido del paciente", example = "Pérez")
        @NotBlank(message="El apellido es obligatorio")
        String lastName,

        @Schema(description = "Fecha de nacimiento del paciente", example = "1990-05-21")
        @Past(message="Fecha de nacimiento inválida")
        LocalDate birthDate,

        @Schema(description = "Género del paciente", example = "MALE")
        @NotNull(message="El género es obligatorio")
        GenderType gender,

        @Schema(description = "Identificador del centro médico asociado", example = "5")
        @NotNull(message="El centro medico es obligatorio")
        Long centerId
) {}
