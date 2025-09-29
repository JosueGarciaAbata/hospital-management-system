package consulting_service.dtos.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for requesting doctor reports
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorReportRequestDTO {

    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> medicalCenters;
    private List<Long> specialties;
    private List<Long> doctors;
    private String sortBy;
    private String sortDirection;
    private Integer page = 0;
    private Integer size = 20;
}