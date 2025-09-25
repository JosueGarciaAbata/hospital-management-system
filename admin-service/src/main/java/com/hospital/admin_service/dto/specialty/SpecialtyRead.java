package com.hospital.admin_service.dto.specialty;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Representación de una especialidad médica en las respuestas del sistema.")
public record SpecialtyRead(

        @Schema(description = "Identificador de la especialidad médica.", example = "1")
        Long id,

        @Schema(description = "Versión de la entidad para control de concurrencia optimista.", example = "0")
        Long version,

        @Schema(description = "Nombre de la especialidad médica.", example = "Cardiología")
        String name,

        @Schema(description = "Descripción de la especialidad médica.",
                example = "Especialidad dedicada al diagnóstico y tratamiento de enfermedades cardiovasculares.")
        String description,

        @Schema(description = "Fecha y hora de creación del registro (UTC).", example = "2025-01-15T12:30:45Z")
        Instant createdAt,

        @Schema(description = "Fecha y hora de la última actualización del registro (UTC).", example = "2025-03-05T09:15:00Z")
        Instant updatedAt
) {}
