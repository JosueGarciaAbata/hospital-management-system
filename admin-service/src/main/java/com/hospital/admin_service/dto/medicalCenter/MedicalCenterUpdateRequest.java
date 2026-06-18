package com.hospital.admin_service.dto.medicalCenter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Solicitud para actualizar un centro médico existente.")
public record MedicalCenterUpdateRequest(

        @Schema(description = "Nuevo nombre del centro médico. Debe ser único.", example = "Hospital Universitario",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
        @NotBlank(message = "Nombre requerido")
        @Size(max = 100, message = "máx 100")
        @UniqueValue(entity = MedicalCenter.class, field = "name", message = "Nombre ya registrado")
        String name,

        @Schema(description = "Nueva ciudad donde se ubica el centro médico.", example = "Guayaquil",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 100)
        @NotBlank(message = "Ciudad requerida")
        @Size(max = 100, message = "máx 100")
        String city,

        @Schema(description = "Nueva dirección exacta del centro médico. Debe ser única.", example = "Malecón 2000",
                requiredMode = Schema.RequiredMode.REQUIRED, maxLength = 200)
        @NotBlank(message = "Dirección requerida")
        @Size(max = 200, message = "máx 200")
        @UniqueValue(entity = MedicalCenter.class, field = "address", message = "Dirección ya registrada")
        String address
) {}
