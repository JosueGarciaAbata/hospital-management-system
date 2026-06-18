package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.Doctor;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.Exists;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * Todos los campos son opcionales; solo se actualizan si se envían no nulos.
 * Si se envía userId, se valida la unicidad excluyendo el propio {id} del path.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Solicitud para actualizar un doctor existente. Todos los campos son opcionales.")
public record DoctorUpdateRequest(

        @Schema(description = "Nuevo identificador de usuario asociado al doctor.", example = "15")
        @Positive(message = "El ID de usuario debe ser positivo")
        @UniqueValue(entity = Doctor.class, field = "userId", message = "El usuario ya está asociado a otro doctor")
        Long userId,

        @Schema(description = "Nuevo identificador de especialidad asociado al doctor.", example = "4")
        @Positive(message = "El ID de especialidad debe ser positivo")
        @Exists(entity = Specialty.class, field = "id", message = "La especialidad no existe")
        Long specialtyId
) {}
