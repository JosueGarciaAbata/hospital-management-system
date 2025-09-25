package com.hospital.admin_service.dto.specialty;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Solicitud para crear una nueva especialidad médica.")
public record SpecialtyCreateRequest(

        @Schema(description = "Nombre de la especialidad médica. Debe ser único.",
                example = "Cardiología", requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
        @NotBlank(message = "Nombre requerido")
        @Size(max = 100, message = "máx 100")
        @UniqueValue(entity = Specialty.class, field = "name", message = "Nombre ya registrado")
        String name,

        @Schema(description = "Descripción detallada de la especialidad médica.",
                example = "Especialidad dedicada al estudio, diagnóstico y tratamiento de enfermedades del corazón.",
                maxLength = 1000)
        @Size(max = 1000, message = "máx 1000")
        String description
) {}
