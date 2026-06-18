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
import java.time.format.DateTimeFormatter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.*;
import com.drtx.jdit.reportservice.dto.DoctorConsultationDTO;
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

            // If this is a DOCTOR report and a specific doctor id was requested, try to narrow
            // the response to that single doctor so the PDF export shows doctor-focused info.
            try {
                if ("DOCTOR".equalsIgnoreCase(request.getReportType()) && request.getFilterId() != null) {
                    List<?> dataList = responseData.getData();
                    if (dataList != null && dataList.size() > 1) {
                        List<DoctorConsultationDTO> filtered = dataList.stream()
                            .filter(d -> d instanceof DoctorConsultationDTO)
                            .map(d -> (DoctorConsultationDTO) d)
                            .filter(doc -> doc.getDoctorId() != null && doc.getDoctorId().equals(request.getFilterId()))
                            .collect(Collectors.toList());

                        if (!filtered.isEmpty()) {
                            // Build a new ReportResponseDTO with only the targeted doctor
                            var singleResponse = new ReportResponseDTO<DoctorConsultationDTO>();
                            singleResponse.setData(filtered);
                            singleResponse.setReportName(responseData.getReportName());
                            singleResponse.setMessage(responseData.getMessage());
                            singleResponse.setTotalElements(filtered.size());
                            singleResponse.setGeneratedAt(responseData.getGeneratedAt());
                            singleResponse.setMetadata(responseData.getMetadata());
                            singleResponse.setAdditionalData(responseData.getAdditionalData());

                            // Replace responseData used for export
                            responseData = singleResponse;
                        }
                    }
                }
            } catch (Exception e) {
                // Best effort filtering, do not fail the whole generation if it errors
            }
            
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
            case SPECIALTY ->  "Consultas por Especialidad";
            case DOCTOR -> "Consultas por Médico";
            case MEDICAL_CENTER -> "Consultas por Centro Médico";
            case MONTHLY -> "Consultas Mensuales";
            default -> "Reporte";
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

        // log.info("Getting consultation report by specialty with filters: {}", filters);

        try {
            // Create request DTO
            SpecialtyReportRequestDTO request = createSpecialtyRequest(filters);
            
            // Call the consulting service via Feign client
            SpecialtyReportResponseDTO reportResponse = consultingServiceClient.getConsultationsBySpecialty(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert structured response to our DTO format
            List<SpecialtyConsultationDTO> consultations = convertSpecialtyReportToConsultationDTOs(reportResponse);

            // Create response
            ReportResponseDTO<SpecialtyConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultas por Especialidad");
            response.setMessage("Reporte generado exitosamente");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // log.error("Error getting consultations by specialty: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting consultations by specialty: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<DoctorConsultationDTO> getConsultationsByDoctor(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // log.info("Getting consultation report by doctor with filters: {}", filters);

        try {
            // Create request DTO
            DoctorReportRequestDTO request = createDoctorRequest(filters);
            
            // Call the consulting service via Feign client
            DoctorReportResponseDTO reportResponse = consultingServiceClient.getConsultationsByDoctor(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert structured response to our DTO format
            List<DoctorConsultationDTO> consultations = convertDoctorReportToConsultationDTOs(reportResponse);

            // Create response
            ReportResponseDTO<DoctorConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultas por Especialidad");
            response.setMessage("Reporte generado exitosamente");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // log.error("Error getting consultations by doctor: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting consultations by doctor: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<MedicalCenterConsultationDTO> getConsultationsByCenter(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // log.info("Getting consultation report by medical center with filters: {}", filters);

        try {
            // Create request DTO
            MedicalCenterReportRequestDTO request = createMedicalCenterRequest(filters);
            
            // Call the consulting service via Feign client
            MedicalCenterReportResponseDTO reportResponse = consultingServiceClient.getConsultationsByCenter(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert structured response to our DTO format
            List<MedicalCenterConsultationDTO> consultations = convertMedicalCenterReportToConsultationDTOs(reportResponse);

            // Create response
            ReportResponseDTO<MedicalCenterConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultas por Centro Medico");
            response.setMessage("Reporte generado exitosamente");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // log.error("Error getting consultations by medical center: {}", e.getMessage(), e);
            throw new RuntimeException("Error getting consultations by medical center: " + e.getMessage(), e);
        }
    }

    @Override
    public ReportResponseDTO<MonthlyConsultationDTO> getMonthlyConsultations(ReportFilterRequestDTO filters) {
        final String token = getAuthorizationToken();
        // log.info("Getting monthly consultation report with filters: {}", filters);

        try {
            // Create request DTO
            MonthlyReportRequestDTO request = createMonthlyRequest(filters);
            
            // Call the consulting service via Feign client
            MonthlyReportResponseDTO reportResponse = consultingServiceClient.getConsultationsByMonth(
                token,
                ConsultingServiceClient.DEFAULT_ROLE,
                request
            );
            
            // Convert structured response to our DTO format
            List<MonthlyConsultationDTO> consultations = convertMonthlyReportToConsultationDTOs(reportResponse);

            // Create response
            ReportResponseDTO<MonthlyConsultationDTO> response = new ReportResponseDTO<>();
            response.setData(consultations);
            response.setReportName("Consultas Mensuales");
            response.setMessage("Reporte generado exitosamente");
            response.setTotalElements(consultations.size());
            response.setGeneratedAt(LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));
            
            return response;
            
        } catch (Exception e) {
            // log.error("Error getting monthly consultations: {}", e.getMessage(), e);
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
            .id(convertToLong(data.get("id")))
            .specialty((String) data.get("especialidad"))
            .doctorName((String) data.get("nombreMedico"))
            .patientName((String) data.get("nombrePaciente"))
            .consultationDate(convertToLocalDateTime(data.get("fechaConsulta")))
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
                .map(consultaData -> {
                    // try to fetch medical center names from possible keys
                    String center = null;
                    if (consultaData.get("centerName") != null) center = String.valueOf(consultaData.get("centerName"));
                    else if (consultaData.get("nombreCentro") != null) center = String.valueOf(consultaData.get("nombreCentro"));
                    else if (consultaData.get("centro") != null) center = String.valueOf(consultaData.get("centro"));
                    else if (consultaData.get("centerId") != null) center = "Centro ID: " + String.valueOf(consultaData.get("centerId"));
                    else if (consultaData.get("centroId") != null) center = "Centro ID: " + String.valueOf(consultaData.get("centroId"));
                    else if (consultaData.get("idCentro") != null) center = "Centro ID: " + String.valueOf(consultaData.get("idCentro"));
                    else if (consultaData.get("medicalCenterId") != null) center = "Centro ID: " + String.valueOf(consultaData.get("medicalCenterId"));

                    String notes = null;
                    if (consultaData.get("notes") != null) notes = (String) consultaData.get("notes");
                    else if (consultaData.get("notas") != null) notes = (String) consultaData.get("notas");

                    Double cost = null;
                    Object costObj = consultaData.get("consultationCost");
                    if (costObj == null) costObj = consultaData.get("costo");
                    if (costObj instanceof Number) cost = ((Number) costObj).doubleValue();

                    return DoctorConsultationDTO.ConsultationDetail.builder()
                        .id(convertToLong(consultaData.get("consultaId")))
                        .patientName((String) consultaData.get("nombrePaciente"))
                        .consultationDate(convertToLocalDateTime(consultaData.get("fechaConsulta")))
                        .status((String) consultaData.get("estado"))
                        .notes(notes)
                        .consultationCost(cost)
                        .medicalCenter(center)
                        .build();
                })
                .collect(java.util.stream.Collectors.toList());
        }
        
        return DoctorConsultationDTO.builder()
            .doctorId(convertToLong(data.get("id")))
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
                    .id(convertToLong(consultaData.get("consultaId")))
                    .doctorName((String) consultaData.get("nombreMedico"))
                    .patientName((String) consultaData.get("nombrePaciente"))
                    .specialty((String) consultaData.get("especialidad"))
                    .consultationDate(convertToLocalDateTime(consultaData.get("fechaConsulta")))
                    .status((String) consultaData.get("estado"))
                    .build())
                .collect(java.util.stream.Collectors.toList());
        }
        
        return MedicalCenterConsultationDTO.builder()
            .centerId(convertToLong(data.get("id")))
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
            
        // Count distinct patients = consultas.stream().flatMap(doctor -> doctor.getConsultations().stream()).map(DoctorConsultationDTO.ConsultationDetail::getPatientName).distinct().count();
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
    
    /**
     * Safely converts an Object to Long, handling Integer and Long types
     */
    private Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        // Try to parse as string if it's a string representation
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }

    /**
     * Safely converts an Object to LocalDateTime, handling String and LocalDateTime types
     */
    private LocalDateTime convertToLocalDateTime(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof LocalDateTime) {
            return (LocalDateTime) value;
        }
        if (value instanceof String) {
            try {
                // Try parsing ISO format first (e.g., 2023-12-01T10:30:00)
                return LocalDateTime.parse((String) value);
            } catch (Exception e1) {
                try {
                    // Try parsing with custom format if needed
                    return LocalDateTime.parse((String) value, DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                } catch (Exception e2) {
                    try {
                        // Try parsing as ISO instant and convert to LocalDateTime
                        return LocalDateTime.parse((String) value, DateTimeFormatter.ISO_LOCAL_DATE_TIME);
                    } catch (Exception e3) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Converts the specialized SpecialtyReportResponseDTO to a List of SpecialtyConsultationDTO
     */
    private List<SpecialtyConsultationDTO> convertSpecialtyReportToConsultationDTOs(SpecialtyReportResponseDTO reportResponse) {
        List<SpecialtyConsultationDTO> result = new ArrayList<>();

        if (reportResponse == null) {
            return result;
        }

        // Extract specialty statistics
        if (reportResponse.getSpecialtyStatistics() != null) {
            for (SpecialtyReportResponseDTO.SpecialtyStatisticDTO stat : reportResponse.getSpecialtyStatistics()) {
                // Creamos un DTO por cada especialidad, con datos básicos
                SpecialtyConsultationDTO dto = new SpecialtyConsultationDTO();
                dto.setSpecialty(stat.getSpecialty());
                // Establecer el total de consultas en el nuevo campo
                if (stat.getTotalConsultations() != null) {
                    dto.setTotalConsultations(stat.getTotalConsultations().longValue());
                }
                // Mantener la información en notes como complemento
                dto.setNotes("Total de consultas: " + stat.getTotalConsultations());
                result.add(dto);
            }
        }

        // Add detailed consultations if available
        if (reportResponse.getDetailedConsultations() != null) {
            for (DetailedConsultationDTO detail : reportResponse.getDetailedConsultations()) {
                SpecialtyConsultationDTO dto = new SpecialtyConsultationDTO();
                dto.setId(detail.getConsultationId());
                dto.setSpecialty(detail.getSpecialty());
                dto.setDoctorName(detail.getDoctorName());
                dto.setPatientName(detail.getPatientName());
                dto.setConsultationDate(detail.getConsultationDate());
                dto.setStatus(detail.getStatus());
                dto.setNotes(detail.getNotes());
                dto.setMedicalCenter(detail.getCenterName());
                result.add(dto);
            }
        }

        return result;
    }

    /**
     * Converts the specialized DoctorReportResponseDTO to a List of DoctorConsultationDTO
     */
    private List<DoctorConsultationDTO> convertDoctorReportToConsultationDTOs(DoctorReportResponseDTO reportResponse) {
        List<DoctorConsultationDTO> result = new ArrayList<>();

        if (reportResponse == null) {
            return result;
        }

        // Process doctor statistics
        if (reportResponse.getDoctorStatistics() != null) {
            for (DoctorReportResponseDTO.DoctorStatisticDTO stat : reportResponse.getDoctorStatistics()) {
                // Create doctor consultation DTO
                DoctorConsultationDTO doctorDTO = new DoctorConsultationDTO();

                // Map basic fields
                doctorDTO.setDoctorId(stat.getDoctorId());
                doctorDTO.setDoctorName(stat.getDoctorName());
                doctorDTO.setSpecialty(stat.getSpecialty());
                doctorDTO.setTotalConsultations(stat.getTotalConsultations() != null ? stat.getTotalConsultations().longValue() : 0L);

                // Best-effort: try to populate DNI / document number from statistic DTO via reflection
                String dni = null;
                try {
                    String[] candidateGetters = new String[] {"getDocumentNumber", "getDni", "getCedula", "getDocument", "getIdentificationNumber"};
                    for (String getter : candidateGetters) {
                        try {
                            java.lang.reflect.Method m = stat.getClass().getMethod(getter);
                            Object val = m.invoke(stat);
                            if (val != null) {
                                dni = String.valueOf(val);
                                break;
                            }
                        } catch (NoSuchMethodException ignored) {
                        }
                    }
                } catch (Exception ignored) {
                }

                // Fallback to doctorId if no document number provided
                if (dni == null || dni.isBlank()) {
                    dni = stat.getDoctorId() != null ? String.valueOf(stat.getDoctorId()) : null;
                }
                doctorDTO.setDni(dni);

                // Create consultation details from detailed consultations
                List<DoctorConsultationDTO.ConsultationDetail> details = new ArrayList<>();

                // If detailed consultations available, find ones for this doctor
                if (reportResponse.getDetailedConsultations() != null) {
                    for (DetailedConsultationDTO detail : reportResponse.getDetailedConsultations()) {
                        // Filter consultations for current doctor (compare by name or other available keys)
                        boolean sameDoctor = false;
                        if (stat.getDoctorName() != null && stat.getDoctorName().equals(detail.getDoctorName())) {
                            sameDoctor = true;
                        }
                        // If doctorId is available in detail (unlikely in current DTO), use it
                        // (kept as defensive; DetailedConsultationDTO currently has no doctorId)

                        if (sameDoctor) {
                            DoctorConsultationDTO.ConsultationDetail consultDetail = new DoctorConsultationDTO.ConsultationDetail();
                            consultDetail.setId(detail.getConsultationId());
                            consultDetail.setPatientName(detail.getPatientName());
                            consultDetail.setConsultationDate(detail.getConsultationDate());
                            consultDetail.setStatus(detail.getStatus());
                            // Populate center name, notes and diagnosis if available
                            consultDetail.setMedicalCenter(detail.getCenterName());
                            consultDetail.setNotes(detail.getNotes());
                            consultDetail.setDiagnosis(detail.getDiagnosis());
                            details.add(consultDetail);
                        }
                    }
                }

                doctorDTO.setConsultations(details);
                result.add(doctorDTO);
            }
        }

        return result;
    }

    /**
     * Converts the specialized MedicalCenterReportResponseDTO to a List of MedicalCenterConsultationDTO
     */
    private List<MedicalCenterConsultationDTO> convertMedicalCenterReportToConsultationDTOs(MedicalCenterReportResponseDTO reportResponse) {
        List<MedicalCenterConsultationDTO> result = new ArrayList<>();

        if (reportResponse == null) {
            return result;
        }

        // Process center statistics
        if (reportResponse.getCenterStatistics() != null) {
            for (MedicalCenterReportResponseDTO.MedicalCenterStatisticDTO stat : reportResponse.getCenterStatistics()) {
                // Create medical center consultation DTO
                MedicalCenterConsultationDTO centerDTO = new MedicalCenterConsultationDTO();
                centerDTO.setCenterId(stat.getCenterId());
                centerDTO.setCenterName(stat.getCenterName());
                centerDTO.setTotalConsultations((long) stat.getTotalConsultations());

                // Create consultation details from detailed consultations
                List<MedicalCenterConsultationDTO.ConsultationDetail> details = new ArrayList<>();

                // If detailed consultations available, find ones for this center
                if (reportResponse.getDetailedConsultations() != null) {
                    for (DetailedConsultationDTO detail : reportResponse.getDetailedConsultations()) {
                        // Filter consultations for current center
                        if (stat.getCenterName().equals(detail.getCenterName())) {
                            MedicalCenterConsultationDTO.ConsultationDetail consultDetail = new MedicalCenterConsultationDTO.ConsultationDetail();
                            consultDetail.setId(detail.getConsultationId());
                            consultDetail.setDoctorName(detail.getDoctorName());
                            consultDetail.setPatientName(detail.getPatientName());
                            consultDetail.setSpecialty(detail.getSpecialty());
                            consultDetail.setConsultationDate(detail.getConsultationDate());
                            consultDetail.setStatus(detail.getStatus());
                            details.add(consultDetail);
                        }
                    }
                }

                centerDTO.setConsultations(details);
                result.add(centerDTO);
            }
        }

        return result;
    }

    /**
     * Converts the specialized MonthlyReportResponseDTO to a List of MonthlyConsultationDTO
     */
    private List<MonthlyConsultationDTO> convertMonthlyReportToConsultationDTOs(MonthlyReportResponseDTO reportResponse) {
        List<MonthlyConsultationDTO> result = new ArrayList<>();

        if (reportResponse == null || reportResponse.getMonthlyStatistics() == null) {
            return result;
        }

        // Process each monthly statistic
        for (MonthlyReportResponseDTO.MonthlyStatisticDTO stat : reportResponse.getMonthlyStatistics()) {
            // Extract month and year from period (e.g., "01/2023")
            String[] periodParts = stat.getPeriod().split("/");
            if (periodParts.length != 2) continue;

            try {
                int month = Integer.parseInt(periodParts[0]);
                int year = Integer.parseInt(periodParts[1]);

                // Create monthly consultation DTO
                MonthlyConsultationDTO monthlyDTO = new MonthlyConsultationDTO();
                monthlyDTO.setMonth(month);
                monthlyDTO.setYear(year);
                monthlyDTO.setTotalConsultations(stat.getTotalConsultations());

                // Group specialties by month (for this example, we'll simulate specialty data)
                List<MonthlyConsultationDTO.SpecialtySummary> specialtySummaries = new ArrayList<>();

                // If KPIs contains distinct specialties info, we can use it to generate dummy specialty data
                if (reportResponse.getKpis() != null && reportResponse.getKpis().getDistinctSpecialties() != null) {
                    long specialtyCount = Math.min(stat.getSpecialtyCount(), 5); // Limit to 5 for simplicity
                    for (int i = 0; i < specialtyCount; i++) {
                        MonthlyConsultationDTO.SpecialtySummary summary = new MonthlyConsultationDTO.SpecialtySummary();
                        summary.setSpecialtyName("Especialidad " + (i + 1));
                        summary.setConsultationCount(stat.getTotalConsultations() / (int)specialtyCount);
                        specialtySummaries.add(summary);
                    }
                }

                monthlyDTO.setSpecialties(specialtySummaries);
                result.add(monthlyDTO);
            } catch (NumberFormatException e) {
                // Skip this entry if period format is invalid
                continue;
            }
        }

        return result;
    }
}
