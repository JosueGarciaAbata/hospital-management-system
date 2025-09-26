package com.drtx.jdit.reportservice.service.impl;

import com.drtx.jdit.reportservice.dto.*;
import com.drtx.jdit.reportservice.dto.request.ReportFilterRequestDTO;
import com.drtx.jdit.reportservice.dto.response.ReportMetadataDTO;
import com.drtx.jdit.reportservice.dto.response.ReportSummaryDTO;
import com.drtx.jdit.reportservice.enums.ExportFormat;
import com.drtx.jdit.reportservice.enums.ReportType;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import com.drtx.jdit.reportservice.service.ReportService;
import com.drtx.jdit.reportservice.utils.ReportExportUtil;
// import lombok.RequiredArgsConstructor;
// import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReportServiceImpl implements ReportService {
    
    private final ConsultingServiceClient consultingServiceClient;
    private final ReportExportUtil reportExportUtil;

    public ReportServiceImpl(ConsultingServiceClient consultingServiceClient, 
                           ReportExportUtil reportExportUtil) {
        this.consultingServiceClient = consultingServiceClient;
        this.reportExportUtil = reportExportUtil;
    }

    @Override
    public byte[] generateReport(ReportRequestDTO request) {
                System.out.println("Generando reporte mejorado de consultas para médico: " + 
                request.getReportType() + " con formato: " + request.getExportFormat());
        
        try {
            // Validate the requested format
            ExportFormat format = ExportFormat.valueOf(request.getExportFormat().toUpperCase());
            String reportName = getReportName(request.getReportType());
            
            // Get report data
            var responseData = getReportData(request);
            
            // Convert to the format expected by ReportExportUtil
            var exportFormat = transformResponse(responseData);
            
            // Export according to the requested format
            return switch (format) {
                case EXCEL -> reportExportUtil.exportToExcel(exportFormat, reportName);
                case CSV -> reportExportUtil.exportToCsv(exportFormat).getBytes(StandardCharsets.UTF_8);
                case PDF -> reportExportUtil.exportToPdf(exportFormat, reportName);
                default -> throw new IllegalArgumentException("Unsupported report format: " + format);
            };
        } catch (IllegalArgumentException e) {
            // // log.error("Error validating report format: {}", e.getMessage());
            throw new IllegalArgumentException("Invalid report format: " + request.getExportFormat());
        } catch (Exception e) {
            // // log.error("Error generating report: {}", e.getMessage(), e);
            throw new RuntimeException("Error generating report: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<?> getReportData(ReportRequestDTO request) {
        // Convert report type to enum
        ReportType reportType = ReportType.valueOf(request.getReportType().toUpperCase());
        
        // Create filters for the query
        ReportFilterRequestDTO filters = createFilterRequest(request);
        
        // Get data according to report type
        return switch (reportType) {
            case SPECIALTY -> getConsultationsBySpecialty(filters);
            case DOCTOR -> getConsultationsByDoctor(filters);
            case MEDICAL_CENTER -> getConsultationsByCenter(filters);
            case MONTHLY -> getMonthlyConsultations(filters);
            default -> throw new IllegalArgumentException("Unsupported report type: " + reportType);
        };
    }
    
    /**
     * Converts a ReportRequestDTO to ReportFilterRequestDTO to call existing methods
     */
    private ReportFilterRequestDTO createFilterRequest(ReportRequestDTO request) {
        ReportFilterRequestDTO filters = new ReportFilterRequestDTO();
        
        // Set ID filter according to report type
        if (request.getFilterId() != null) {
            switch (ReportType.valueOf(request.getReportType().toUpperCase())) {
                case SPECIALTY:
                    filters.setSpecialties(List.of(request.getFilterId()));
                    break;
                case DOCTOR:
                    filters.setDoctors(List.of(request.getFilterId()));
                    break;
                case MEDICAL_CENTER:
                    filters.setMedicalCenters(List.of(request.getFilterId()));
                    break;
                default:
                    // Don't apply ID filter for monthly reports
                    break;
            }
        }
        
        // Set date filters
        if (request.getStartDate() != null && !request.getStartDate().isEmpty()) {
            try {
                LocalDate startDate = LocalDate.parse(request.getStartDate());
                filters.setStartDate(startDate);
            } catch (Exception e) {
                // // log.warn("Error parsing start date: {}", request.getStartDate());
            }
        }
        
        if (request.getEndDate() != null && !request.getEndDate().isEmpty()) {
            try {
                LocalDate endDate = LocalDate.parse(request.getEndDate());
                filters.setEndDate(endDate);
            } catch (Exception e) {
                // // log.warn("Error parsing end date: {}", request.getEndDate());
            }
        }
        
        // For monthly reports, if a specific month is provided
        if (request.getMonth() != null && !request.getMonth().isEmpty() && 
            ReportType.valueOf(request.getReportType().toUpperCase()) == ReportType.MONTHLY) {
            
            try {
                // Expected format: yyyy-MM
                String[] parts = request.getMonth().split("-");
                int year = Integer.parseInt(parts[0]);
                int month = Integer.parseInt(parts[1]);
                
                LocalDate startOfMonth = LocalDate.of(year, month, 1);
                LocalDate endOfMonth = startOfMonth.plusMonths(1).minusDays(1);
                
                filters.setStartDate(startOfMonth);
                filters.setEndDate(endOfMonth);
            } catch (Exception e) {
                // // log.warn("Error processing specific month: {}", request.getMonth());
            }
        }
        
        // Include additional data for statistics
        filters.setIncludeAdditionalData(true);
        
        return filters;
    }
    
    /**
     * Get friendly name for the report
     */
    private String getReportName(String reportType) {
        ReportType type = ReportType.valueOf(reportType.toUpperCase());
        return switch (type) {
            case SPECIALTY -> "Consultations by Specialty";
            case DOCTOR -> "Consultations by Doctor";
            case MEDICAL_CENTER -> "Consultations by Medical Center";
            case MONTHLY -> "Monthly Consultations";
            default -> "Report";
        };
    }
    
    /**
     * Transforms a response from our new format to the format expected by ReportExportUtil
     * @param responseData our response format
     * @return format compatible with exporter
     */
    private <T> ReportResponseDTO<T> transformResponse(ReportResponseDTO<T> responseData) {
        // We are already using the same ReportResponseDTO class, so we just need to ensure
        // all required fields are properly set for the export
        
        // Ensure metadata is set up properly
        if (responseData.getMetadata() == null) {
            ReportMetadataDTO metadata = new ReportMetadataDTO();
            metadata.setReportName(responseData.getReportName());
            metadata.setTotalRecords(responseData.getTotalElements());
            metadata.setGenerationDate(LocalDateTime.now());
            responseData.setMetadata(metadata);
        }
        
        return responseData;
    }
    
    /**
     * Transforms a response from the old format to the new format
     * This method facilitates conversion between different response types
     */
    private <T> ReportResponseDTO<T> transformToNewResponse(ReportResponseDTO<T> oldResponse) {
        var newFormat = new ReportResponseDTO<T>();
        
        if (oldResponse.getMetadata() != null) {
            newFormat.setReportName(oldResponse.getMetadata().getReportName());
            
            // Convert date correctly
            LocalDateTime generationDate = oldResponse.getMetadata().getGenerationDate();
            if (generationDate != null) {
                newFormat.setGeneratedAt(generationDate.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            } else {
                newFormat.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            }
            
            // Add metadata - safely get total records
            Long totalRecords = oldResponse.getMetadata().getTotalRecords();
            newFormat.setTotalElements(totalRecords != null ? totalRecords.intValue() : 0);
        } else {
            // Set default values if metadata is null
            newFormat.setReportName("Report");
            newFormat.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            // Use the size of the data if available, otherwise 0
            newFormat.setTotalElements(oldResponse.getData() != null ? oldResponse.getData().size() : 0);
        }
        
        // Transfer data
        newFormat.setData(oldResponse.getData());
        newFormat.setMessage("Report generated successfully");
        
        return newFormat;
    }

    @Override
    public ReportResponseDTO<SpecialtyConsultationDTO> getConsultationsBySpecialty(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // // log.info("Getting consultation report by specialty with filters: {}", filters);
        
        try {
            // Create request DTO
            SpecialtyReportRequestDTO request = createSpecialtyRequest(filters);
            
            // Call the consulting service via Feign client
            List<Map<String, Object>> rawData = consultingServiceClient.getConsultationsBySpecialty(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert raw data to DTOs
            List<SpecialtyConsultationDTO> consultations = rawData.stream()
                .map(this::mapToSpecialtyConsultationDTO)
                .collect(java.util.stream.Collectors.toList());
            
            // Create response
            ReportResponseDTO<SpecialtyConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultations by Specialty");
            response.setMessage("Report generated successfully");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // // log.error("Error getting consultations by specialty: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting consultations by specialty: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<DoctorConsultationDTO> getConsultationsByDoctor(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // // log.info("Getting consultation report by doctor with filters: {}", filters);
        
        try {
            // Create request DTO
            DoctorReportRequestDTO request = createDoctorRequest(filters);
            
            // Call the consulting service via Feign client
            List<Map<String, Object>> rawData = consultingServiceClient.getConsultationsByDoctor(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert raw data to DTOs
            List<DoctorConsultationDTO> consultations = rawData.stream()
                .map(this::mapToDoctorConsultationDTO)
                .collect(java.util.stream.Collectors.toList());
            
            // Create response
            ReportResponseDTO<DoctorConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultations by Doctor");
            response.setMessage("Report generated successfully");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // // log.error("Error getting consultations by doctor: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting consultations by doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<MedicalCenterConsultationDTO> getConsultationsByCenter(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // // log.info("Getting consultation report by medical center with filters: {}", filters);
        
        try {
            // Create request DTO
            MedicalCenterReportRequestDTO request = createMedicalCenterRequest(filters);
            
            // Call the consulting service via Feign client
            List<Map<String, Object>> rawData = consultingServiceClient.getConsultationsByCenter(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert raw data to DTOs
            List<MedicalCenterConsultationDTO> consultations = rawData.stream()
                .map(this::mapToMedicalCenterConsultationDTO)
                .collect(java.util.stream.Collectors.toList());
            
            // Create response
            ReportResponseDTO<MedicalCenterConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultations by Medical Center");
            response.setMessage("Report generated successfully");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // // log.error("Error getting consultations by medical center: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting consultations by medical center: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<MonthlyConsultationDTO> getMonthlyConsultations(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // // log.info("Getting monthly consultation report with filters: {}", filters);
        
        try {
            // Create request DTO
            MonthlyReportRequestDTO request = createMonthlyRequest(filters);
            
            // Call the consulting service via Feign client
            List<Map<String, Object>> rawData = consultingServiceClient.getConsultationsByMonth(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert raw data to DTOs
            List<MonthlyConsultationDTO> consultations = rawData.stream()
                .map(this::mapToMonthlyConsultationDTO)
                .collect(java.util.stream.Collectors.toList());
            
            // Create response
            ReportResponseDTO<MonthlyConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Monthly Consultations");
            response.setMessage("Report generated successfully");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // // log.error("Error getting monthly consultations: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting monthly consultations: " + e.getMessage(), e);
        }
    }

    /**
     * Gets the current JWT token from the security context
     */
    private String getAuthorizationToken() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            return "Bearer " + authentication.getCredentials().toString();
        }
        // // log.warn("Could not get authorization token from security context");
        return "";
    }
    
    /**
     * Creates a SpecialtyReportRequestDTO from ReportFilterRequestDTO
     */
    private SpecialtyReportRequestDTO createSpecialtyRequest(ReportFilterRequestDTO filters) {
        return SpecialtyReportRequestDTO.builder()
            .fechaInicio(filters.getStartDate())
            .fechaFin(filters.getEndDate())
            .centrosMedicos(filters.getMedicalCenters())
            .especialidades(filters.getSpecialties())
            .medicos(filters.getDoctors())
            .estado(filters.getStatus())
            .ordenarPor(filters.getSortBy())
            .direccionOrden(filters.getSortDirection())
            .pagina(filters.getPage())
            .tamanio(filters.getSize())
            .build();
    }
    
    /**
     * Creates a DoctorReportRequestDTO from ReportFilterRequestDTO
     */
    private DoctorReportRequestDTO createDoctorRequest(ReportFilterRequestDTO filters) {
        return DoctorReportRequestDTO.builder()
            .fechaInicio(filters.getStartDate())
            .fechaFin(filters.getEndDate())
            .centrosMedicos(filters.getMedicalCenters())
            .especialidades(filters.getSpecialties())
            .medicos(filters.getDoctors())
            .estado(filters.getStatus())
            .ordenarPor(filters.getSortBy())
            .direccionOrden(filters.getSortDirection())
            .pagina(filters.getPage())
            .tamanio(filters.getSize())
            .build();
    }
    
    /**
     * Creates a MedicalCenterReportRequestDTO from ReportFilterRequestDTO
     */
    private MedicalCenterReportRequestDTO createMedicalCenterRequest(ReportFilterRequestDTO filters) {
        return MedicalCenterReportRequestDTO.builder()
            .fechaInicio(filters.getStartDate())
            .fechaFin(filters.getEndDate())
            .centrosMedicos(filters.getMedicalCenters())
            .especialidades(filters.getSpecialties())
            .medicos(filters.getDoctors())
            .estado(filters.getStatus())
            .ordenarPor(filters.getSortBy())
            .direccionOrden(filters.getSortDirection())
            .pagina(filters.getPage())
            .tamanio(filters.getSize())
            .build();
    }
    
    /**
     * Creates a MonthlyReportRequestDTO from ReportFilterRequestDTO
     */
    private MonthlyReportRequestDTO createMonthlyRequest(ReportFilterRequestDTO filters) {
        return MonthlyReportRequestDTO.builder()
            .fechaInicio(filters.getStartDate())
            .fechaFin(filters.getEndDate())
            .centrosMedicos(filters.getMedicalCenters())
            .especialidades(filters.getSpecialties())
            .medicos(filters.getDoctors())
            .estado(filters.getStatus())
            .ordenarPor(filters.getSortBy())
            .direccionOrden(filters.getSortDirection())
            .pagina(filters.getPage())
            .tamanio(filters.getSize())
            .build();
    }
    
    /**
     * Maps a Map<String, Object> to SpecialtyConsultationDTO
     */
    private SpecialtyConsultationDTO mapToSpecialtyConsultationDTO(Map<String, Object> data) {
        return SpecialtyConsultationDTO.builder()
            .id((Long) data.get("id"))
            .specialty((String) data.get("especialidad"))
            .doctorName((String) data.get("nombreMedico"))
            .patientName((String) data.get("nombrePaciente"))
            .consultationDate((LocalDateTime) data.get("fechaConsulta"))
            .status((String) data.get("estado"))
            .build();
    }
    
    /**
     * Maps a Map<String, Object> to DoctorConsultationDTO
     */
    @SuppressWarnings("unchecked")
    private DoctorConsultationDTO mapToDoctorConsultationDTO(Map<String, Object> data) {
        List<Map<String, Object>> consultasData = (List<Map<String, Object>>) data.get("consultas");
        List<DoctorConsultationDTO.ConsultationDetail> consultations = new ArrayList<>();
        
        if (consultasData != null) {
            consultations = consultasData.stream()
                .map(consultaData -> DoctorConsultationDTO.ConsultationDetail.builder()
                    .id((Long) consultaData.get("consultaId"))
                    .patientName((String) consultaData.get("nombrePaciente"))
                    .consultationDate((LocalDateTime) consultaData.get("fechaConsulta"))
                    .status((String) consultaData.get("estado"))
                    .build())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return DoctorConsultationDTO.builder()
            .doctorId((Long) data.get("id"))
            .doctorName((String) data.get("nombreMedico"))
            .specialty((String) data.get("especialidad"))
            .totalConsultations((long) consultations.size())
            .consultations(consultations)
            .build();
    }
    
    /**
     * Maps a Map<String, Object> to MedicalCenterConsultationDTO
     */
    @SuppressWarnings("unchecked")
    private MedicalCenterConsultationDTO mapToMedicalCenterConsultationDTO(Map<String, Object> data) {
        List<Map<String, Object>> consultasData = (List<Map<String, Object>>) data.get("consultas");
        List<MedicalCenterConsultationDTO.ConsultationDetail> consultations = new ArrayList<>();
        
        if (consultasData != null) {
            consultations = consultasData.stream()
                .map(consultaData -> MedicalCenterConsultationDTO.ConsultationDetail.builder()
                    .id((Long) consultaData.get("consultaId"))
                    .doctorName((String) consultaData.get("nombreMedico"))
                    .patientName((String) consultaData.get("nombrePaciente"))
                    .specialty((String) consultaData.get("especialidad"))
                    .consultationDate((LocalDateTime) consultaData.get("fechaConsulta"))
                    .status((String) consultaData.get("estado"))
                    .build())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return MedicalCenterConsultationDTO.builder()
            .centerId((Long) data.get("id"))
            .centerName((String) data.get("nombreCentro"))
            .address((String) data.get("direccion"))
            .totalConsultations((long) consultations.size())
            .consultations(consultations)
            .build();
    }
    
    /**
     * Maps a Map<String, Object> to MonthlyConsultationDTO
     */
    @SuppressWarnings("unchecked")
    private MonthlyConsultationDTO mapToMonthlyConsultationDTO(Map<String, Object> data) {
        List<Map<String, Object>> especialidadesData = (List<Map<String, Object>>) data.get("especialidades");
        List<MonthlyConsultationDTO.SpecialtySummary> specialties = new ArrayList<>();
        
        if (especialidadesData != null) {
            specialties = especialidadesData.stream()
                .map(espData -> MonthlyConsultationDTO.SpecialtySummary.builder()
                    .specialtyName((String) espData.get("nombreEspecialidad"))
                    .consultationCount((Integer) espData.get("cantidadConsultas"))
                    .build())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return MonthlyConsultationDTO.builder()
            .year((Integer) data.get("anio"))
            .month((Integer) data.get("mes"))
            .totalConsultations((Integer) data.get("totalConsultas"))
            .specialties(specialties)
            .build();
    }
    
    /**
     * Generates a data summary for specialty consultations
     */
    private ReportSummaryDTO generateSpecialtyConsultationsSummary(List<SpecialtyConsultationDTO> consultations) {
        if (consultations == null || consultations.isEmpty()) {
            return new ReportSummaryDTO();
        }
        
        // Count consultations by status
        Map<String, Long> consultationsByStatus = consultations.stream()
            .collect(Collectors.groupingBy(SpecialtyConsultationDTO::getStatus, Collectors.counting()));
        
        // Find first and last consultation
        LocalDateTime firstConsultation = consultations.stream()
            .map(SpecialtyConsultationDTO::getConsultationDate)
            .min(LocalDateTime::compareTo)
            .orElse(null);
            
        LocalDateTime lastConsultation = consultations.stream()
            .map(SpecialtyConsultationDTO::getConsultationDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        // Count distinct specialties
        long totalSpecialties = consultations.stream()
            .map(SpecialtyConsultationDTO::getSpecialty)
            .distinct()
            .count();
            
        // Count distinct doctors
        long totalDoctors = consultations.stream()
            .map(SpecialtyConsultationDTO::getDoctorName)
            .distinct()
            .count();
            
        // Count distinct patients
        long totalPatients = consultations.stream()
            .map(SpecialtyConsultationDTO::getPatientName)
            .distinct()
            .count();
        
        // Calculate average consultations per day if there's enough data
        BigDecimal averageConsultations = BigDecimal.ZERO;
        if (firstConsultation != null && lastConsultation != null) {
            long daysDifference = ChronoUnit.DAYS.between(
                firstConsultation.toLocalDate(), 
                lastConsultation.toLocalDate()
            ) + 1; // +1 to include the current day
            
            if (daysDifference > 0) {
                averageConsultations = BigDecimal.valueOf(consultations.size())
                    .divide(BigDecimal.valueOf(daysDifference), 2, RoundingMode.HALF_UP);
            }
        }
        
        // Create additional statistics
        Map<String, Object> additionalStatistics = new HashMap<>();
        
        // Consultations by specialty
        Map<String, Long> consultationsBySpecialty = consultations.stream()
            .collect(Collectors.groupingBy(SpecialtyConsultationDTO::getSpecialty, Collectors.counting()));
        additionalStatistics.put("consultationsBySpecialty", consultationsBySpecialty);
        
        // Build and return summary
        return ReportSummaryDTO.builder()
            .totalConsultations(consultations.size())
            .consultationsByStatus(consultationsByStatus)
            .averageConsultationsPerPeriod(averageConsultations)
            .totalDoctors(totalDoctors)
            .totalPatients(totalPatients)
            .totalSpecialties(totalSpecialties)
            .firstConsultation(firstConsultation)
            .lastConsultation(lastConsultation)
            .additionalStatistics(additionalStatistics)
            .build();
    }
    
    /**
     * Generates a data summary for doctor consultations
     */
    private ReportSummaryDTO generateDoctorConsultationsSummary(List<DoctorConsultationDTO> consultations) {
        if (consultations == null || consultations.isEmpty()) {
            return new ReportSummaryDTO();
        }
        
        // Count total consultations
        long totalConsultations = consultations.stream()
            .mapToLong(doctor -> doctor.getConsultations().size())
            .sum();
            
        // Count consultations by status
        Map<String, Long> consultationsByStatus = new HashMap<>();
        consultations.forEach(doctor -> 
            doctor.getConsultations().forEach(consultation -> {
                String status = consultation.getStatus();
                consultationsByStatus.put(status, 
                    consultationsByStatus.getOrDefault(status, 0L) + 1);
            })
        );
        
        // Find first and last consultation
        LocalDateTime firstConsultation = consultations.stream()
            .flatMap(doctor -> doctor.getConsultations().stream())
            .map(DoctorConsultationDTO.ConsultationDetail::getConsultationDate)
            .min(LocalDateTime::compareTo)
            .orElse(null);
            
        LocalDateTime lastConsultation = consultations.stream()
            .flatMap(doctor -> doctor.getConsultations().stream())
            .map(DoctorConsultationDTO.ConsultationDetail::getConsultationDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        // Count distinct specialties
        long totalSpecialties = consultations.stream()
            .map(DoctorConsultationDTO::getSpecialty)
            .distinct()
            .count();
            
        // Count distinct patients
        long totalPatients = consultations.stream()
            .flatMap(doctor -> doctor.getConsultations().stream())
            .map(DoctorConsultationDTO.ConsultationDetail::getPatientName)
            .distinct()
            .count();
        
        // Calculate average consultations per doctor
        BigDecimal averageConsultations = BigDecimal.ZERO;
        if (!consultations.isEmpty()) {
            averageConsultations = BigDecimal.valueOf(totalConsultations)
                .divide(BigDecimal.valueOf(consultations.size()), 2, RoundingMode.HALF_UP);
        }
        
        // Create additional statistics
        Map<String, Object> additionalStatistics = new HashMap<>();
        
        // Top doctors with most consultations
        Map<String, Long> consultationsByDoctor = consultations.stream()
            .collect(Collectors.toMap(
                DoctorConsultationDTO::getDoctorName,
                doctor -> (long) doctor.getConsultations().size()
            ));
        additionalStatistics.put("consultationsByDoctor", consultationsByDoctor);
        
        // Build and return summary
        return ReportSummaryDTO.builder()
            .totalConsultations(totalConsultations)
            .consultationsByStatus(consultationsByStatus)
            .averageConsultationsPerPeriod(averageConsultations)
            .totalDoctors(consultations.size())
            .totalPatients(totalPatients)
            .totalSpecialties(totalSpecialties)
            .firstConsultation(firstConsultation)
            .lastConsultation(lastConsultation)
            .additionalStatistics(additionalStatistics)
            .build();
    }
    
    /**
     * Generates a data summary for medical center consultations
     */
    private ReportSummaryDTO generateMedicalCenterConsultationsSummary(List<MedicalCenterConsultationDTO> consultations) {
        if (consultations == null || consultations.isEmpty()) {
            return new ReportSummaryDTO();
        }
        
        // Count total consultations
        long totalConsultations = consultations.stream()
            .mapToLong(center -> center.getConsultations().size())
            .sum();
            
        // Count consultations by status
        Map<String, Long> consultationsByStatus = new HashMap<>();
        consultations.forEach(center -> 
            center.getConsultations().forEach(consultation -> {
                String status = consultation.getStatus();
                consultationsByStatus.put(status, 
                    consultationsByStatus.getOrDefault(status, 0L) + 1);
            })
        );
        
        // Find first and last consultation
        LocalDateTime firstConsultation = consultations.stream()
            .flatMap(center -> center.getConsultations().stream())
            .map(MedicalCenterConsultationDTO.ConsultationDetail::getConsultationDate)
            .min(LocalDateTime::compareTo)
            .orElse(null);
            
        LocalDateTime lastConsultation = consultations.stream()
            .flatMap(center -> center.getConsultations().stream())
            .map(MedicalCenterConsultationDTO.ConsultationDetail::getConsultationDate)
            .max(LocalDateTime::compareTo)
            .orElse(null);
        
        // Count distinct specialties
        long totalSpecialties = consultations.stream()
            .flatMap(center -> center.getConsultations().stream())
            .map(MedicalCenterConsultationDTO.ConsultationDetail::getSpecialty)
            .distinct()
            .count();
            
        // Count distinct doctors
        long totalDoctors = consultations.stream()
            .flatMap(center -> center.getConsultations().stream())
            .map(MedicalCenterConsultationDTO.ConsultationDetail::getDoctorName)
            .distinct()
            .count();
            
        // Count distinct patients
        long totalPatients = consultations.stream()
            .flatMap(center -> center.getConsultations().stream())
            .map(MedicalCenterConsultationDTO.ConsultationDetail::getPatientName)
            .distinct()
            .count();
        
        // Calculate average consultations per center
        BigDecimal averageConsultations = BigDecimal.ZERO;
        if (!consultations.isEmpty()) {
            averageConsultations = BigDecimal.valueOf(totalConsultations)
                .divide(BigDecimal.valueOf(consultations.size()), 2, RoundingMode.HALF_UP);
        }
        
        // Create additional statistics
        Map<String, Object> additionalStatistics = new HashMap<>();
        
        // Consultations by medical center
        Map<String, Long> consultationsByCenter = consultations.stream()
            .collect(Collectors.toMap(
                MedicalCenterConsultationDTO::getCenterName,
                center -> (long) center.getConsultations().size()
            ));
        additionalStatistics.put("consultationsByCenter", consultationsByCenter);
        
        // Build and return summary
        return ReportSummaryDTO.builder()
            .totalConsultations(totalConsultations)
            .consultationsByStatus(consultationsByStatus)
            .averageConsultationsPerPeriod(averageConsultations)
            .totalDoctors(totalDoctors)
            .totalPatients(totalPatients)
            .totalSpecialties(totalSpecialties)
            .totalMedicalCenters(consultations.size())
            .firstConsultation(firstConsultation)
            .lastConsultation(lastConsultation)
            .additionalStatistics(additionalStatistics)
            .build();
    }
    
    /**
     * Generates a data summary for monthly consultations
     */
    private ReportSummaryDTO generateMonthlyConsultationsSummary(List<MonthlyConsultationDTO> consultations) {
        if (consultations == null || consultations.isEmpty()) {
            return new ReportSummaryDTO();
        }
        
        // Count total consultations
        long totalConsultations = consultations.stream()
            .mapToLong(MonthlyConsultationDTO::getTotalConsultations)
            .sum();
            
        // Count distinct specialties
        long totalSpecialties = consultations.stream()
            .flatMap(month -> month.getSpecialties().stream())
            .map(MonthlyConsultationDTO.SpecialtySummary::getSpecialtyName)
            .distinct()
            .count();
            
        // Calculate average consultations per month
        BigDecimal averageConsultations = BigDecimal.ZERO;
        if (!consultations.isEmpty()) {
            averageConsultations = BigDecimal.valueOf(totalConsultations)
                .divide(BigDecimal.valueOf(consultations.size()), 2, RoundingMode.HALF_UP);
        }
        
        // Create additional statistics
        Map<String, Object> additionalStatistics = new HashMap<>();
        
        // Consultations by month
        Map<String, Integer> consultationsByMonth = consultations.stream()
            .collect(Collectors.toMap(
                month -> month.getYear() + "-" + String.format("%02d", month.getMonth()),
                MonthlyConsultationDTO::getTotalConsultations
            ));
        additionalStatistics.put("consultationsByMonth", consultationsByMonth);
        
        // Specialties with most consultations
        Map<String, Long> consultationsBySpecialty = new HashMap<>();
        consultations.forEach(month -> 
            month.getSpecialties().forEach(spec -> {
                String name = spec.getSpecialtyName();
                consultationsBySpecialty.put(name, 
                    consultationsBySpecialty.getOrDefault(name, 0L) + spec.getConsultationCount());
            })
        );
        additionalStatistics.put("consultationsBySpecialty", consultationsBySpecialty);
        
        // Build and return summary
        return ReportSummaryDTO.builder()
            .totalConsultations(totalConsultations)
            .averageConsultationsPerPeriod(averageConsultations)
            .totalSpecialties(totalSpecialties)
            .additionalStatistics(additionalStatistics)
            .build();
    }
}
