package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import com.hospital.admin_service.model.Doctor;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.validation.annotation.Exists;
import com.hospital.admin_service.validation.annotation.UniqueValue;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import io.swagger.v3.oas.annotations.media.Schema;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Solicitud para crear un nuevo doctor vinculado a un usuario y una especialidad existente.")
public record DoctorCreateRequest(

        @Schema(description = "Identificador del usuario asociado al doctor.", example = "10", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de usuario es requerido")
        @Positive(message = "El ID de usuario debe ser positivo")
        @UniqueValue(entity = Doctor.class, field = "userId", message = "El usuario ya está asociado a un doctor")
        Long userId,

        @Schema(description = "Identificador de la especialidad vinculada al doctor.", example = "3", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El ID de especialidad es requerido")
        @Positive(message = "El ID de especialidad debe ser positivo")
        @Exists(entity = Specialty.class, field = "id", message = "La especialidad no existe")
        Long specialtyId
) {}
