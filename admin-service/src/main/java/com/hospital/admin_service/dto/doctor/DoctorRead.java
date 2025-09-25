package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Representación de un doctor en las respuestas del sistema.")
public record DoctorRead(

        @Schema(description = "Identificador del doctor.", example = "1")
        Long id,

        @Schema(description = "Versión de la entidad para control de concurrencia optimista.", example = "0")
        Long version,

        @Schema(description = "Identificador del usuario asociado al doctor.", example = "10")
        Long userId,

        @Schema(description = "Identificador de la especialidad asociada.", example = "3")
        Long specialtyId,

        @Schema(description = "Nombre de la especialidad.", example = "Cardiología")
        String specialtyName,

        @Schema(description = "Nombre de usuario del doctor.", example = "drperez")
        String username,

        @Schema(description = "Nombre del doctor.", example = "Juan")
        String firstName,

        @Schema(description = "Apellido del doctor.", example = "Pérez")
        String lastName,

        @Schema(description = "Género del doctor.", allowableValues = {"MALE", "FEMALE"}, example = "MALE")
        String gender,

        @Schema(description = "Fecha y hora de creación del registro (UTC).", example = "2025-01-15T12:30:45Z")
        Instant createdAt,

        @Schema(description = "Fecha y hora de última actualización del registro (UTC).", example = "2025-03-05T09:15:00Z")
        Instant updatedAt
) {}
