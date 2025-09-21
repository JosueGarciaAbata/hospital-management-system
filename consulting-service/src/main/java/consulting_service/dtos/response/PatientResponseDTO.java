package consulting_service.dtos.response;

import java.time.LocalDate;

public record PatientResponseDTO(
        Long id,
        String dni,
        String firstName,
        String lastName,
        LocalDate birthDate,
        String gender,
        Long centerId
) {}

