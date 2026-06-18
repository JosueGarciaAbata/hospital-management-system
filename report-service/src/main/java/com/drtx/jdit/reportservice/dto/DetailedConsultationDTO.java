package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para detalles de consultas en los reportes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DetailedConsultationDTO {

    private Long consultationId;
    private String patientName;
    private String doctorName;
    private String specialty;
    private String centerName;
    private LocalDateTime consultationDate;
    private String status;
    private String diagnosis;
    private String treatment;
    private String notes;
}
