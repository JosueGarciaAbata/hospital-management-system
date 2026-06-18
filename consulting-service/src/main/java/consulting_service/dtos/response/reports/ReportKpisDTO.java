package consulting_service.dtos.response.reports;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * DTO for report KPIs
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportKpisDTO {

    private Long distinctSpecialties;
    private Long doctorsInvolved;
    private Long medicalCentersInvolved;
    private Long uniquePatientsTotal;
    private Double avgConsultationsPerDoctor;
    private DataQualityDTO dataQuality;
    private Map<String, Object> additionalMetrics;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DataQualityDTO {
        private Long consultationsWithDiagnosis;
        private Long consultationsWithTreatment;
        private Double dataCompletenessPercentage;
    }
}