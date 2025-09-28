package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para respuestas de reportes mensuales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportResponseDTO {

    private ExecutiveSummaryDTO executiveSummary;
    private List<MonthlyStatisticDTO> monthlyStatistics;
    private ReportKpisDTO kpis;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ExecutiveSummaryDTO {
        private Integer totalConsultations;
        private Long monthsAnalyzed;
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
    public static class MonthlyStatisticDTO {
        private String period;
        private Integer totalConsultations;
        private Integer uniquePatients;
        private Integer uniqueDoctors;
        private Integer specialtyCount;
        private Double growth;
    }
}
