package com.hospital.admin_service.DTO.medicalCenter;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record MedicalCenterCreateRequest(
        @NotBlank(message = "Nombre requerido")
        @Size(max = 100, message = "máx 100")
        String name,

        @NotBlank(message = "Ciudad requerida")
        @Size(max = 100, message = "máx 100")
        String city,

        @NotBlank(message = "Dirección requerida")
        @Size(max = 200, message = "máx 200")
        String address
) {}