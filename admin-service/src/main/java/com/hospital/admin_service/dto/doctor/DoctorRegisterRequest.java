package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.Exists;
import jakarta.validation.constraints.*;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DoctorRegisterRequest(

        @NotBlank(message = "El nombre de usuario es requerido")
        String username,

        @NotBlank(message = "La contraseña es requerida")
        @Size(min = 8, message = "La contraseña debe tener al menos 8 caracteres")
        String password,

        @Pattern(regexp = "MALE|FEMALE", message = "El género debe ser MALE o FEMALE")
        String gender,

        @NotBlank(message = "El nombre es requerido")
        @JsonProperty("first_name")
        String firstName,

        @NotBlank(message = "El apellido es requerido")
        @JsonProperty("last_name")
        String lastName,

        @NotNull(message = "El ID del centro es requerido")
        @Positive(message = "El ID del centro debe ser positivo")
        @JsonProperty("center_id")
        Long centerId,

        @NotNull(message = "El ID de especialidad es requerido")
        @Positive(message = "El ID de especialidad debe ser positivo")
        @Exists(entity = Specialty.class, field = "id", message = "La especialidad no existe")
        Long specialtyId
) {}
