package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.Doctor;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.Exists;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.Positive;

/**
 * Todos los campos son opcionales; solo se actualizan si vienen no-null.
 * Si se envía userId, se valida unicidad excluyendo el propio {id} del path.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DoctorUpdateRequest(

        @Positive(message = "El ID de usuario debe ser positivo")
        @UniqueValue(entity = Doctor.class, field = "userId", message = "El usuario ya está asociado a otro doctor")
        Long userId,

        @Positive(message = "El ID de especialidad debe ser positivo")
        @Exists(entity = Specialty.class, field = "id", message = "La especialidad no existe")
        Long specialtyId
) {}
