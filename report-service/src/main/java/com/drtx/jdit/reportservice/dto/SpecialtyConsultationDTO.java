package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for consultations grouped by specialty
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SpecialtyConsultationDTO {
    private Long id;
    private String specialty;
    private String doctorName;
    private String patientName;
    private LocalDateTime consultationDate;
    private String status;
    private String notes;
    private Double consultationCost;
    private String medicalCenter;
    private Long totalConsultations; // Nuevo campo para almacenar el total de consultas por especialidad
}