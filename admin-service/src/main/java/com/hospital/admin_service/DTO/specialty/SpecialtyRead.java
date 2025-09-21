package com.hospital.admin_service.DTO.specialty;

import java.time.Instant;

public record SpecialtyRead(
        Long id,
        Long version,
        String name,
        String description,
        Instant createdAt,
        Instant updatedAt
) {}
