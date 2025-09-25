package com.hospital.admin_service.dto.medicalCenter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Representación de un centro médico en las respuestas del sistema.")
public record MedicalCenterRead(

        @Schema(description = "Identificador del centro médico.", example = "1")
        Long id,

        @Schema(description = "Versión de la entidad para control de concurrencia optimista.", example = "0")
        Long version,

        @Schema(description = "Nombre del centro médico.", example = "Hospital Metropolitano")
        String name,

        @Schema(description = "Ciudad donde se ubica el centro médico.", example = "Quito")
        String city,

        @Schema(description = "Dirección exacta del centro médico.", example = "Av. 10 de Agosto y Colón")
        String address,

        @Schema(description = "Fecha y hora de creación del registro (UTC).", example = "2025-01-15T12:30:45Z")
        Instant createdAt,

        @Schema(description = "Fecha y hora de la última actualización del registro (UTC).", example = "2025-03-05T09:15:00Z")
        Instant updatedAt
) {}
