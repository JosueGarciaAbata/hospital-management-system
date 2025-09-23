package com.hospital.admin_service.dto.specialty;

import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import java.time.Instant;

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record SpecialtyRead(
        Long id,
        Long version,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
