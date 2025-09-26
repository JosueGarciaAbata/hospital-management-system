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
        // log.info("Generando reporte tipo: {} en formato: {}", request.getReportType(), request.getExportFormat());
        
        byte[] reportData = reportService.generateReport(request);
        
        String filename = String.format("reporte_%s_%s.%s",
                request.getReportType().toLowerCase(),
                LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")),
                getFileExtension(request.getExportFormat()));
        
        return ResponseEntity
                .ok()
                .contentType(getMediaType(request.getExportFormat()))
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
        // log.info("Obteniendo datos para reporte tipo: {}", request.getReportType());
        
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
    public ResponseEntity<List<Map<String, Object>>> getConsultationsBySpecialty(
            @RequestBody SpecialtyReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        // log.info("Obteniendo consultas por especialidad con filtros: {}", request);
        
        List<Map<String, Object>> consultations = consultingServiceClient.getConsultationsBySpecialty(
                authorization,
                "ADMIN",
                request
        );
        
        return ResponseEntity.ok(consultations);
    }
    
    /**
     * Endpoint para obtener consultas por médico con estadísticas avanzadas
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con reporte estadístico completo de consultas por médico
     */
    @PostMapping("/consultation/doctor")
    public ResponseEntity<Map<String, Object>> getConsultationsByDoctor(
            @RequestBody DoctorReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        System.out.println("Generating enhanced doctor consultation report with filters: " + request);
        
        try {
            List<Map<String, Object>> consultations = consultingServiceClient.getConsultationsByDoctor(
                    authorization,
                    "ADMIN", 
                    request
            );
            
            // Crear reporte estadístico completo
            Map<String, Object> enhancedReport = createEnhancedDoctorReport(consultations, request);
            
            return ResponseEntity.ok(enhancedReport);
            
        } catch (Exception e) {
            System.err.println("Error generating doctor consultation report: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Error generating report", "details", e.getMessage()));
        }
    }
    
    /**
     * Endpoint para obtener consultas por centro médico
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con los datos de consultas agrupadas por centro médico
     */
    @PostMapping("/consultation/medical-center")
    public ResponseEntity<List<Map<String, Object>>> getConsultationsByMedicalCenter(
            @RequestBody MedicalCenterReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        // log.info("Obteniendo consultas por centro médico con filtros: {}", request);
        
        List<Map<String, Object>> consultations = consultingServiceClient.getConsultationsByCenter(
                authorization,
                "ADMIN",
                request
        );
        
        return ResponseEntity.ok(consultations);
    }
    
    /**
     * Endpoint para obtener consultas mensuales
     * @param request DTO con los parámetros para filtrar las consultas
     * @param authorization Token de autorización
     * @return ResponseEntity con los datos de consultas agrupadas por mes
     */
    @PostMapping("/consultation/monthly")
    public ResponseEntity<List<Map<String, Object>>> getConsultationsByMonth(
            @RequestBody MonthlyReportRequestDTO request,
            @RequestHeader("Authorization") String authorization) {
        // log.info("Obteniendo consultas mensuales con filtros: {}", request);
        
        List<Map<String, Object>> consultations = consultingServiceClient.getConsultationsByMonth(
                authorization,
                "ADMIN",
                request
        );
        
        return ResponseEntity.ok(consultations);
    }
    
    /**
     * Crea un reporte estadístico avanzado para consultas por médico
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> createEnhancedDoctorReport(List<Map<String, Object>> rawData, DoctorReportRequestDTO request) {
        Map<String, Object> report = new LinkedHashMap<>();
        
        // 1. RESUMEN EJECUTIVO
        Map<String, Object> executiveSummary = new LinkedHashMap<>();
        executiveSummary.put("reportTitle", "Doctor Performance & Consultation Analytics Report");
        executiveSummary.put("generatedAt", LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        executiveSummary.put("reportPeriod", "Filter Period Applied");
        executiveSummary.put("totalDoctorsAnalyzed", rawData.size());
        
        // 2. ESTADÍSTICAS GENERALES
        Map<String, Object> generalStats = new LinkedHashMap<>();
        int totalConsultations = 0;
        Set<String> uniquePatients = new HashSet<>();
        Set<String> specialties = new HashSet<>();
        Map<String, Integer> statusCount = new HashMap<>();
        Map<String, Integer> monthlyDistribution = new HashMap<>();
        
        for (Map<String, Object> doctor : rawData) {
            List<Map<String, Object>> consultations = (List<Map<String, Object>>) doctor.get("consultas");
            String specialty = (String) doctor.get("especialidad");
            
            if (specialty != null) {
                // Fix encoding issues
                specialty = specialty.replace("Ã­", "í").replace("Ã¡", "á").replace("Ã³", "ó");
                specialties.add(specialty);
            }
            
            for (Map<String, Object> consultation : consultations) {
                totalConsultations++;
                String patientName = (String) consultation.get("nombrePaciente");
                String status = (String) consultation.get("estado");
                String dateStr = (String) consultation.get("fechaConsulta");
                
                if (patientName != null) uniquePatients.add(patientName);
                if (status != null) statusCount.merge(status, 1, Integer::sum);
                
                // Extract month from date
                if (dateStr != null && dateStr.length() >= 7) {
                    String month = dateStr.substring(5, 7); // Get MM from YYYY-MM-DD
                    monthlyDistribution.merge("Month " + month, 1, Integer::sum);
                }
            }
        }
        
        generalStats.put("totalConsultations", totalConsultations);
        generalStats.put("uniquePatients", uniquePatients.size());
        generalStats.put("activeDoctors", rawData.size());
        generalStats.put("specialtiesInvolved", specialties.size());
        generalStats.put("consultationStatusBreakdown", statusCount);
        generalStats.put("averageConsultationsPerDoctor", rawData.isEmpty() ? 0 : Math.round((double) totalConsultations / rawData.size() * 100.0) / 100.0);
        
        // 3. ANÁLISIS POR ESPECIALIDAD
        Map<String, Map<String, Object>> specialtyStats = new HashMap<>();
        for (Map<String, Object> doctor : rawData) {
            String specialty = (String) doctor.get("especialidad");
            if (specialty != null) {
                specialty = specialty.replace("Ã­", "í").replace("Ã¡", "á").replace("Ã³", "ó");
                List<Map<String, Object>> consultations = (List<Map<String, Object>>) doctor.get("consultas");
                
                specialtyStats.computeIfAbsent(specialty, k -> new HashMap<>())
                    .merge("consultations", consultations.size(), (a, b) -> (Integer) a + (Integer) b);
                specialtyStats.get(specialty).merge("doctors", 1, (a, b) -> (Integer) a + (Integer) b);
            }
        }
        
        // 4. TOP PERFORMERS
        List<Map<String, Object>> doctorPerformance = new ArrayList<>();
        for (Map<String, Object> doctor : rawData) {
            Map<String, Object> performance = new LinkedHashMap<>();
            List<Map<String, Object>> consultations = (List<Map<String, Object>>) doctor.get("consultas");
            String specialty = (String) doctor.get("especialidad");
            
            performance.put("doctorName", doctor.get("nombreMedico"));
            performance.put("doctorId", doctor.get("id"));
            performance.put("specialty", specialty != null ? specialty.replace("Ã­", "í").replace("Ã¡", "á").replace("Ã³", "ó") : "Unknown");
            performance.put("totalConsultations", consultations.size());
            performance.put("consultationDetails", consultations);
            
            // Calcular estadísticas por doctor
            Set<String> doctorPatients = new HashSet<>();
            Map<String, Integer> doctorStatusCount = new HashMap<>();
            
            for (Map<String, Object> consultation : consultations) {
                String status = (String) consultation.get("estado");
                String patient = (String) consultation.get("nombrePaciente");
                
                if (status != null) doctorStatusCount.merge(status, 1, Integer::sum);
                if (patient != null) doctorPatients.add(patient);
            }
            
            performance.put("statusBreakdown", doctorStatusCount);
            performance.put("uniquePatientsServed", doctorPatients.size());
            performance.put("patientRetentionRate", 
                doctorPatients.isEmpty() ? 0 : Math.round((double) consultations.size() / doctorPatients.size() * 100.0) / 100.0);
            
            doctorPerformance.add(performance);
        }
        
        // Ordenar por número de consultas
        doctorPerformance.sort((a, b) -> Integer.compare(
            (Integer) b.get("totalConsultations"), 
            (Integer) a.get("totalConsultations")
        ));
        
        // 5. KPIs CORPORATIVOS
        Map<String, Object> kpis = new LinkedHashMap<>();
        kpis.put("doctorProductivityScore", rawData.isEmpty() ? 0 : Math.round((double) totalConsultations / rawData.size() * 10) / 10.0);
        kpis.put("patientDiversityIndex", totalConsultations == 0 ? 0 : Math.round((double) uniquePatients.size() / totalConsultations * 100.0));
        kpis.put("consultationSuccessRate", totalConsultations == 0 ? 0 : 
            Math.round((double) statusCount.getOrDefault("ACTIVA", 0) / totalConsultations * 100.0));
        kpis.put("specialtyDiversityScore", specialties.size() * 20); // Scale specialty count
        
        // Construir reporte final
        report.put("executiveSummary", executiveSummary);
        report.put("generalStatistics", generalStats);
        report.put("specialtyAnalysis", specialtyStats);
        report.put("monthlyDistribution", monthlyDistribution);
        report.put("doctorPerformanceRanking", doctorPerformance);
        report.put("keyPerformanceIndicators", kpis);
        report.put("topPerformer", doctorPerformance.isEmpty() ? null : doctorPerformance.get(0));
        report.put("rawData", rawData); // Keep original data for compatibility
        
        return report;
    }
    
    private MediaType getMediaType(String exportFormat) {
        ExportFormat format = ExportFormat.valueOf(exportFormat.toUpperCase());
        switch (format) {
            case EXCEL:
                return MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            case CSV:
                return MediaType.parseMediaType("text/csv");
            case PDF:
                return MediaType.APPLICATION_PDF;
            default:
                return MediaType.APPLICATION_OCTET_STREAM;
        }
    }
    
    private String getFileExtension(String exportFormat) {
        ExportFormat format = ExportFormat.valueOf(exportFormat.toUpperCase());
        switch (format) {
            case EXCEL:
                return "xlsx";
            case CSV:
                return "csv";
            case PDF:
                return "pdf";
            default:
                return "bin";
        }
    }
}