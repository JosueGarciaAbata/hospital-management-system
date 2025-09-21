package com.hospital.admin_service.DTO.specialty;

import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record SpecialtyCreateRequest(
        @NotBlank(message = "Nombre requerido")
        @Size(max = 100, message = "máx 100")
        @UniqueValue(entity = Specialty.class, field = "name")
        String name,

        @Size(max = 1000, message = "máx 1000")
        String description
) {}
