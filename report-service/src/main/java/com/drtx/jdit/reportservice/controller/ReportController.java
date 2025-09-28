package com.drtx.jdit.reportservice.controller;

import com.drtx.jdit.reportservice.dto.*;
import com.drtx.jdit.reportservice.enums.ExportFormat;
import com.drtx.jdit.reportservice.service.ReportService;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final ConsultingServiceClient consultingServiceClient;
    
    /**
     * Endpoint para generar reportes en diferentes formatos (Excel, CSV)
     * @param request DTO con los parámetros para la generación del reporte
     * @return ResponseEntity con el archivo del reporte generado
     */
    @PostMapping("/generate")
    public ResponseEntity<?> generateReport(@RequestBody ReportRequestDTO request) {
        log.info("Generando reporte tipo: {} en formato: {}", request.getReportType(), request.getExportFormat());

        byte[] reportData = reportService.generateReport(request);
        
        // Convertir exportFormat string a enum, con manejo de error
        ExportFormat formatEnum;
        try {
            formatEnum = ExportFormat.valueOf(request.getExportFormat().toUpperCase());
        } catch (Exception e) {
            log.warn("Formato de exportación inválido: {}. Usando PDF por defecto.", request.getExportFormat());
            formatEnum = ExportFormat.PDF;
        }

        String filename = String.format("reporte_%s_%s.%s",
                request.getReportType().toLowerCase(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                getFileExtension(formatEnum));

        return ResponseEntity
                .ok()
                .contentType(getMediaType(formatEnum))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(reportData);
    }
    
    /**
     * Endpoint para obtener los datos del reporte sin formato de exportación
     * @param request DTO con los parámetros para la generación del reporte
     * @return ResponseEntity con los datos del reporte en formato JSON
     */
    @PostMapping("/data")
    public ResponseEntity<ReportResponseDTO<?>> getReportData(@RequestBody ReportRequestDTO request) {
        log.info("Obteniendo datos para reporte tipo: {}", request.getReportType());

        ReportResponseDTO<?> response = reportService.getReportData(request);
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para obtener consultas por especialidad
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con los datos de consultas agrupadas por especialidad
     */
    @PostMapping("/consultation/specialty")
    public ResponseEntity<SpecialtyReportResponseDTO> getConsultationsBySpecialty(
            @RequestBody SpecialtyReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        log.info("Obteniendo consultas por especialidad con filtros: {}", request);

        SpecialtyReportResponseDTO response = consultingServiceClient.getConsultationsBySpecialty(
                authorization,
                "ADMIN",
                request
        );
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * Endpoint para obtener consultas por médico con estadísticas avanzadas
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con reporte estadístico completo de consultas por médico
     */
    @PostMapping("/consultation/doctor")
    public ResponseEntity<DoctorReportResponseDTO> getConsultationsByDoctor(
            @RequestBody DoctorReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        log.info("Generando reporte de consultas por médico con filtros: {}", request);

        try {
            DoctorReportResponseDTO response = consultingServiceClient.getConsultationsByDoctor(
                    authorization,
                    "ADMIN", 
                    request
            );
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener reporte por médico", e);
            throw e;
        }
    }
    
    /**
     * Endpoint para obtener consultas por centro médico
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con reporte estadístico completo por centro médico
     */
    @PostMapping("/consultation/center")
    public ResponseEntity<MedicalCenterReportResponseDTO> getConsultationsByCenter(
            @RequestBody MedicalCenterReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        log.info("Generando reporte de consultas por centro médico con filtros: {}", request);

        try {
            MedicalCenterReportResponseDTO response = consultingServiceClient.getConsultationsByCenter(
                    authorization,
                    "ADMIN",
                    request
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener reporte por centro médico", e);
            throw e;
        }
    }
    
    /**
     * Endpoint para obtener consultas por mes
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con reporte estadístico mensual
     */
    @PostMapping("/consultation/monthly")
    public ResponseEntity<MonthlyReportResponseDTO> getConsultationsByMonth(
            @RequestBody MonthlyReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        log.info("Generando reporte mensual con filtros: {}", request);

        try {
            MonthlyReportResponseDTO response = consultingServiceClient.getConsultationsByMonth(
                    authorization,
                    "ADMIN",
                    request
            );

            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error al obtener reporte mensual", e);
            throw e;
        }
    }
    
    // Helper methods
    private MediaType getMediaType(ExportFormat format) {
        return switch (format) {
            case EXCEL -> MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV -> MediaType.parseMediaType("text/csv");
            case PDF -> MediaType.parseMediaType("application/pdf");
        };
    }
    
    private String getFileExtension(ExportFormat format) {
        return switch (format) {
            case EXCEL -> "xlsx";
            case CSV -> "csv";
            case PDF -> "pdf";
        };
    }
}