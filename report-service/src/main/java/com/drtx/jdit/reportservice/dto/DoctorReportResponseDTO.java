package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * DTO para respuestas de reportes por doctor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorReportResponseDTO {

    private ExecutiveSummaryDTO executiveSummary;
    private List<DoctorStatisticDTO> doctorStatistics;
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
        private Long uniqueDoctors;
        private LocalDate dateRangeStart;
        private LocalDate dateRangeEnd;
        private LocalDateTime reportGeneratedAt;
        private Boolean hasDateFilter;
        private Boolean hasData;
        private String reportType;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DoctorStatisticDTO {
        private Long doctorId;
        private String doctorName;
        private String specialty;
        private Integer totalConsultations;
        private Integer uniquePatients;
        private Integer consultationsWithDiagnosis;
        private Integer consultationsWithTreatment;
        private Double efficiencyRate;
        private Double consultationShare;
    }
}
