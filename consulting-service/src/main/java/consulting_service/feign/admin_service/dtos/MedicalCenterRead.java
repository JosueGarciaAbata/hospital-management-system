package consulting_service.feign.admin_service.dtos;

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