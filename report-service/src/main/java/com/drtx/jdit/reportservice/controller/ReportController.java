package com.drtx.jdit.reportservice.controller;

import com.drtx.jdit.reportservice.dto.*;
import com.drtx.jdit.reportservice.enums.ExportFormat;
import com.drtx.jdit.reportservice.service.ReportService;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

@Tag(name = "Reportes", description = "Operaciones para generar y obtener reportes estadísticos del sistema")
@RestController
@RequestMapping("/api/reports")
@RequiredArgsConstructor
@Slf4j
public class ReportController {

    private final ReportService reportService;
    private final ConsultingServiceClient consultingServiceClient;
    
    @Operation(
            summary = "Genera un reporte en formato PDF, Excel o CSV",
            description = "Permite generar reportes basados en parámetros específicos y obtener el archivo resultante"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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

    @Operation(
            summary = "Obtiene los datos de un reporte sin formato de exportación",
            description = "Devuelve los datos del reporte en formato JSON para su procesamiento posterior"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Datos del reporte obtenidos correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
    @PostMapping("/data")
    public ResponseEntity<ReportResponseDTO<?>> getReportData(@RequestBody ReportRequestDTO request) {
        log.info("Obteniendo datos para reporte tipo: {}", request.getReportType());

        ReportResponseDTO<?> response = reportService.getReportData(request);
        return ResponseEntity.ok(response);
    }

    @Operation(
            summary = "Obtiene consultas médicas agrupadas por especialidad",
            description = "Genera un reporte de consultas por especialidad usando filtros proporcionados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte por especialidad obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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

    @Operation(
            summary = "Genera reporte estadístico de consultas por médico",
            description = "Obtiene información detallada de consultas agrupadas por médico con estadísticas avanzadas"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte por médico generado correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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

    @Operation(
            summary = "Genera reporte de consultas por centro médico",
            description = "Obtiene reportes estadísticos agrupados por centro médico según los filtros proporcionados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte por centro médico obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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

    @Operation(
            summary = "Genera reporte mensual de consultas médicas",
            description = "Obtiene reportes estadísticos agrupados por mes según los filtros proporcionados"
    )
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Reporte mensual obtenido correctamente"),
            @ApiResponse(responseCode = "400", description = "Parámetros inválidos"),
            @ApiResponse(responseCode = "401", description = "No autorizado"),
            @ApiResponse(responseCode = "500", description = "Error interno del servidor")
    })
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