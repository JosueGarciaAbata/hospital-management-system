package consulting_service.dtos.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO for medical center report responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCenterReportResponseDTO {

    private ExecutiveSummaryDTO executiveSummary;
    private List<MedicalCenterStatisticDTO> centerStatistics;
    private Map<String, Integer> weeklyDistribution;
    private ReportKpisDTO kpis;
    private List<DetailedConsultationDTO> detailedConsultations;
    private PaginationInfoDTO paginationInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutiveSummaryDTO {
        private Integer totalConsultations;
        private Long uniqueCenters;
        private LocalDate dateRangeStart;
        private LocalDate dateRangeEnd;
        private LocalDateTime reportGeneratedAt;
        private Boolean hasDateFilter;
        private String reportType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class MedicalCenterStatisticDTO {
        private Long centerId;
        private String centerName;
        private Integer totalConsultations;
        private Integer uniqueDoctors;
        private Integer uniquePatients;
        private Double consultationsPerDoctor;
        private Double marketShare;
    }
}