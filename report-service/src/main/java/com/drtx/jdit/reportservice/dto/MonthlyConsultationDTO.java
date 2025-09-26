package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for consultations grouped by month
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyConsultationDTO {
    private Integer year;
    private Integer month;
    private Integer totalConsultations;
    private Double totalRevenue;
    
    @Builder.Default
    private List<SpecialtySummary> specialties = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SpecialtySummary {
        private String specialtyName;
        private Integer consultationCount;
        private Double revenue;
    }
}