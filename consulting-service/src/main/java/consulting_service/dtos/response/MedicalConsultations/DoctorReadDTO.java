package consulting_service.dtos.response.MedicalConsultations;

import lombok.AllArgsConstructor;
import lombok.Data;

@AllArgsConstructor
@Data
public class DoctorReadDTO {
    private Long id;
    private String firstName;
    private String lastName;
}