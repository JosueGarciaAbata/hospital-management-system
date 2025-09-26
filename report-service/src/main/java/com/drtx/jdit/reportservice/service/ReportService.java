package com.drtx.jdit.reportservice.service;

import com.drtx.jdit.reportservice.dto.SpecialtyConsultationDTO;
import com.drtx.jdit.reportservice.dto.DoctorConsultationDTO;
import com.drtx.jdit.reportservice.dto.MedicalCenterConsultationDTO;
import com.drtx.jdit.reportservice.dto.MonthlyConsultationDTO;
import com.drtx.jdit.reportservice.dto.ReportRequestDTO;
import com.drtx.jdit.reportservice.dto.ReportResponseDTO;
import com.drtx.jdit.reportservice.dto.request.ReportFilterRequestDTO;

/**
 * NOTA: Este servicio usa la clase ReportResponseDTO del paquete dto, NO la del paquete dto.response
 * Para evitar conflictos, se debe tener cuidado de no mezclar las dos implementaciones de ReportResponseDTO
 */

import java.util.List;

public interface ReportService {
    /**
     * Generates a report in the requested format (Excel, CSV, PDF)
     * @param request DTO with the requested report information
     * @return byte array with the content of the generated report
     */
    byte[] generateReport(ReportRequestDTO request);
    
    /**
     * Gets the report data without applying export formatting
     * @param request DTO with the requested report information
     * @return ReportResponseDTO from dto package with the requested data
     */
    ReportResponseDTO<?> getReportData(ReportRequestDTO request);
    
    /**
     * Consultations by specialty with optional filters
     * @param filters the filters to apply to the report
     * @return ReportResponseDTO from dto package with specialty consultations
     */
    ReportResponseDTO<SpecialtyConsultationDTO> getConsultationsBySpecialty(ReportFilterRequestDTO filters);
    
    /**
     * Consultations by doctor with optional filters
     * @param filters the filters to apply to the report
     * @return ReportResponseDTO from dto package with doctor consultations
     */
    ReportResponseDTO<DoctorConsultationDTO> getConsultationsByDoctor(ReportFilterRequestDTO filters);
    
    /**
     * Consultations by medical center with optional filters
     * @param filters the filters to apply to the report
     * @return ReportResponseDTO from dto package with medical center consultations
     */
    ReportResponseDTO<MedicalCenterConsultationDTO> getConsultationsByCenter(ReportFilterRequestDTO filters);
    
    /**
     * Monthly consultations with optional filters
     * @param filters the filters to apply to the report
     * @return ReportResponseDTO from dto package with monthly consultations
     */
    ReportResponseDTO<MonthlyConsultationDTO> getMonthlyConsultations(ReportFilterRequestDTO filters);
    
    /**
     * Simplified method to get consultations by specialty without filters
     * @return simple list of consultations by specialty
     */
    default List<SpecialtyConsultationDTO> getConsultationsBySpecialty() {
        return getConsultationsBySpecialty(new ReportFilterRequestDTO()).getData();
    }
    
    /**
     * Simplified method to get consultations by doctor without filters
     * @return simple list of consultations by doctor
     */
    default List<DoctorConsultationDTO> getConsultationsByDoctor() {
        return getConsultationsByDoctor(new ReportFilterRequestDTO()).getData();
    }
    
    /**
     * Simplified method to get consultations by medical center without filters
     * @return simple list of consultations by medical center
     */
    default List<MedicalCenterConsultationDTO> getConsultationsByCenter() {
        return getConsultationsByCenter(new ReportFilterRequestDTO()).getData();
    }
    
    /**
     * Simplified method to get monthly consultations without filters
     * @return simple list of monthly consultations
     */
    default List<MonthlyConsultationDTO> getMonthlyConsultations() {
        return getMonthlyConsultations(new ReportFilterRequestDTO()).getData();
    }
}
