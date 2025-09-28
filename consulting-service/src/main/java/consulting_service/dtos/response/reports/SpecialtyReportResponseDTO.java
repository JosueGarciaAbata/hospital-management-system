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
 * DTO for specialty report responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyReportResponseDTO {

    private ExecutiveSummaryDTO executiveSummary;
    private List<SpecialtyStatisticDTO> specialtyStatistics;
    private Map<String, Integer> weeklyDistribution;
    private List<TopDoctorDTO> topActiveDoctors;
    private ReportKpisDTO kpis;
    private List<DetailedConsultationDTO> detailedConsultations;
    private PaginationInfoDTO paginationInfo;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutiveSummaryDTO {
        private Integer totalConsultations;
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
    public static class SpecialtyStatisticDTO {
        private String specialty;
        private Integer totalConsultations;
        private Integer uniqueDoctors;
        private Integer uniquePatients;
        private Double avgConsultationsPerDoctor;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TopDoctorDTO {
        private Long doctorId;
        private String doctorName;
        private Integer totalConsultations;
        private Double consultationShare;
    }
}