package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.Exists;
import jakarta.validation.constraints.*;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Solicitud para registrar un nuevo usuario con rol DOCTOR y crear el doctor correspondiente.")
public record DoctorRegisterRequest(

        @Schema(description = "Nombre de usuario único para el inicio de sesión.", example = "drperez", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre de usuario es requerido")
        String username,

        @Schema(description = "Contraseña de la cuenta (mínimo 8 caracteres).", example = "ClaveSegura123", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Schema(description = "Correo electrónico del doctor.", example = "drperez@hospital.com", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El correo es requerido")
        @Email(message = "Correo inválido")
        @Size(max = 150, message = "máx 150")
        String email,

        @Schema(description = "Género del doctor.", allowableValues = {"MALE", "FEMALE"}, example = "MALE")
        @Pattern(regexp = "MALE|FEMALE", message = "El género debe ser MALE o FEMALE")
        String gender,

        @Schema(description = "Nombre del doctor.", example = "Juan", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El nombre es requerido")
        @JsonProperty("first_name")
        String firstName,

        @Schema(description = "Apellido del doctor.", example = "Pérez", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El apellido es requerido")
        @JsonProperty("last_name")
        String lastName,

        @Schema(description = "Identificador del centro médico donde trabaja el doctor.", example = "2", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID del centro es requerido")
        @Positive(message = "El ID del centro debe ser positivo")
        @JsonProperty("center_id")
        Long centerId,

        @Schema(description = "Identificador de la especialidad del doctor.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de especialidad es requerido")
        @Positive(message = "El ID de especialidad debe ser positivo")
        @Exists(entity = Specialty.class, field = "id", message = "La especialidad no existe")
        Long specialtyId
) {}
