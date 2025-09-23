package com.hospital.admin_service.dto.medicalCenter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MedicalCenterCreateRequest(
        @NotBlank(message = "Nombre requerido")
        @Size(max = 100, message = "máx 100")
        @UniqueValue(entity = MedicalCenter.class, field = "name", message = "Nombre ya registrado")
        String name,

        @NotBlank(message = "Ciudad requerida")
        @Size(max = 100, message = "máx 100")
        String city,

        @NotBlank(message = "Dirección requerida")
        @Size(max = 200, message = "máx 200")
        @UniqueValue(entity = MedicalCenter.class, field = "address", message = "Dirección ya registrada")
        String address
) {}