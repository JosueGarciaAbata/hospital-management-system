package com.hospital.admin_service.dto.doctor;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record DoctorRead(
        Long id,
        Long version,
        Long userId,
        Long specialtyId,
        String specialtyName,
        Instant createdAt,
        Instant updatedAt
) {}
