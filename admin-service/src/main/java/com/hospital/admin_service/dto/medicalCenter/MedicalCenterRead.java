package com.hospital.admin_service.dto.medicalCenter;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record MedicalCenterRead(
        Long id,
        Long version,
        String name,
        String city,
        String address,
        Instant createdAt,
        Instant updatedAt
) {
}