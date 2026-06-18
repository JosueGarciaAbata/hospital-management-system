package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for consultations grouped by medical center
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MedicalCenterConsultationDTO {
    private Long centerId;
    private String centerName;
    private String address;
    private Long totalConsultations;
    
    @Builder.Default
    private List<ConsultationDetail> consultations = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationDetail {
        private Long id;
        private String doctorName;
        private String patientName;
        private String specialty;
        private java.time.LocalDateTime consultationDate;
        private String status;
        private Double consultationCost;
    }
}