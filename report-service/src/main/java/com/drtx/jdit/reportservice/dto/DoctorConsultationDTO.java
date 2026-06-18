package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for consultations grouped by doctor
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorConsultationDTO {
    private Long doctorId;
    private String doctorName;
    private String specialty;
    private Long totalConsultations;
    private String dni;
    
    @Builder.Default
    private List<ConsultationDetail> consultations = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultationDetail {
        private Long id;
        private String patientName;
        private java.time.LocalDateTime consultationDate;
        private String status;
        private String notes;
        private Double consultationCost;
        private String medicalCenter;
        private String diagnosis;
    }
}