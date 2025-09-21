package com.hospital.admin_service.DTO.medicalCenter;

import java.time.Instant;

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