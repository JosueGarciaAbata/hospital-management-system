package consulting_service.services.reports;

import consulting_service.dtos.request.SpecialtyReportRequestDTO;
import consulting_service.dtos.response.reports.DetailedConsultationDTO;
import consulting_service.dtos.response.reports.PaginationInfoDTO;
import consulting_service.dtos.response.reports.ReportKpisDTO;
import consulting_service.dtos.response.reports.SpecialtyReportResponseDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.feign.admin_service.dtos.DoctorRead;
import consulting_service.repositories.MedicalConsultationsRepository;
import consulting_service.specifications.MedicalConsultationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes por especialidad médica
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SpecialtyReportService {

    private final MedicalConsultationsRepository consultationsRepository;
    private final consulting_service.services.reports.ReportEnrichmentService reportDataService;
    private final consulting_service.services.reports.ReportUtilsService reportUtils;

    /**
     * Genera un reporte detallado por especialidad médica - VERSIÓN CORREGIDA
     */
    public SpecialtyReportResponseDTO generateReport(SpecialtyReportRequestDTO request, Pageable pageable) {
        log.info("Generando reporte de especialidad: {}", request);

        LocalDateTime startDate = reportUtils.toStartOfDay(request.getStartDate());
        LocalDateTime endDate = reportUtils.toEndOfDay(request.getEndDate());

        // Primero obtener todas las consultas sin filtrar por especialidad
        Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                startDate,
                endDate,
                null, // deleted
                request.getMedicalCenters(),
                request.getDoctors(),
                request.getSpecialties() // ahora sí
        );

        Page<MedicalConsultation> consultationsPage = consultationsRepository.findAll(spec, pageable);
        List<MedicalConsultation> allConsultations = consultationsPage.getContent();

        // Filtrar por especialidad en memoria
        List<MedicalConsultation> filteredConsultations = filterBySpecialty(allConsultations, request.getSpecialties());

        log.info("Consultas después de filtrar por especialidad {}: {}", request.getSpecialties(), filteredConsultations.size());

        if (filteredConsultations.isEmpty()) {
            return buildEmptyResponse();
        }

        return SpecialtyReportResponseDTO.builder()
                .executiveSummary(buildExecutiveSummary(filteredConsultations, startDate, endDate))
                .specialtyStatistics(buildSpecialtyStatistics(filteredConsultations))
                .weeklyDistribution(reportUtils.buildWeeklyDistribution(filteredConsultations))
                .topActiveDoctors(buildTopDoctors(filteredConsultations, 10))
                .kpis(buildKpis(filteredConsultations))
                .detailedConsultations(buildDetailedConsultations(filteredConsultations, 20))
                .paginationInfo(buildCustomPaginationInfo(consultationsPage, filteredConsultations.size()))
                .build();
    }

    /**
     * Filtra las consultas por especialidad
     */
    private List<MedicalConsultation> filterBySpecialty(List<MedicalConsultation> consultations, List<Long> specialtyIds) {
        if (specialtyIds == null || specialtyIds.isEmpty()) {
            return consultations;
        }

        return consultations.stream()
                .filter(consultation -> {
                    try {
                        String doctorSpecialty = reportDataService.getDoctorSpecialty(consultation.getDoctorId());
                        // Convertir la especialidad del doctor a Long para comparar
                        Long doctorSpecialtyId = extractSpecialtyId(doctorSpecialty);
                        return specialtyIds.contains(doctorSpecialtyId);
                    } catch (Exception e) {
                        log.warn("Error al obtener especialidad del doctor {}: {}", consultation.getDoctorId(), e.getMessage());
                        return false;
                    }
                })
                .collect(Collectors.toList());
    }

    /**
     * Extrae el ID de especialidad del string (puede necesitar ajustes según tu implementación)
     */
    private Long extractSpecialtyId(String specialty) {
        try {
            // Si la especialidad es un ID numérico
            return Long.parseLong(specialty);
        } catch (NumberFormatException e) {
            // Si es un nombre, podrías mapearlo a un ID
            // Esto depende de cómo esté implementado getDoctorSpecialty
            log.warn("No se pudo convertir especialidad a ID: {}", specialty);
            return -1L; // Valor que no coincidirá con ningún ID real
        }
    }

    /**
     * Construye información de paginación personalizada para los resultados filtrados
     */
    private PaginationInfoDTO buildCustomPaginationInfo(Page<MedicalConsultation> originalPage, int filteredSize) {
        return PaginationInfoDTO.builder()
                .currentPage(originalPage.getNumber())
                .totalPages((int) Math.ceil((double) filteredSize / originalPage.getSize()))
                .totalElements((long) filteredSize)
                .pageSize(originalPage.getSize())
                .hasNext(originalPage.hasNext())
                .hasPrevious(originalPage.hasPrevious())
                .build();
    }

    // Los demás métodos permanecen igual...
    private SpecialtyReportResponseDTO.ExecutiveSummaryDTO buildExecutiveSummary(
            List<MedicalConsultation> consultations, LocalDateTime start, LocalDateTime end) {

        return SpecialtyReportResponseDTO.ExecutiveSummaryDTO.builder()
                .totalConsultations(consultations.size())
                .dateRangeStart(start != null ? start.toLocalDate() : null)
                .dateRangeEnd(end != null ? end.toLocalDate() : null)
                .reportGeneratedAt(LocalDateTime.now())
                .hasDateFilter(start != null || end != null)
                .reportType("SPECIALTY")
                .build();
    }

    private List<SpecialtyReportResponseDTO.SpecialtyStatisticDTO> buildSpecialtyStatistics(
            List<MedicalConsultation> consultations) {

        Map<String, SpecialtyStats> statsMap = new HashMap<>();

        for (MedicalConsultation consultation : consultations) {
            String specialty = reportDataService.getDoctorSpecialty(consultation.getDoctorId());

            SpecialtyStats stats = statsMap.computeIfAbsent(specialty, k -> new SpecialtyStats());
            stats.totalConsultations++;
            stats.doctors.add(consultation.getDoctorId());
            stats.patients.add(consultation.getPatientId());
        }

        return statsMap.entrySet().stream()
                .map(entry -> {
                    SpecialtyStats stats = entry.getValue();
                    double avgPerDoctor = !stats.doctors.isEmpty() ?
                            Math.round((stats.totalConsultations / (double) stats.doctors.size()) * 100.0) / 100.0 : 0.0;

                    return SpecialtyReportResponseDTO.SpecialtyStatisticDTO.builder()
                            .specialty(entry.getKey())
                            .totalConsultations(stats.totalConsultations)
                            .uniqueDoctors(stats.doctors.size())
                            .uniquePatients(stats.patients.size())
                            .avgConsultationsPerDoctor(avgPerDoctor)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalConsultations(), a.getTotalConsultations()))
                .collect(Collectors.toList());
    }

    private List<SpecialtyReportResponseDTO.TopDoctorDTO> buildTopDoctors(
            List<MedicalConsultation> consultations, int limit) {

        Map<Long, Long> consultationsPerDoctor = consultations.stream()
                .collect(Collectors.groupingBy(MedicalConsultation::getDoctorId, Collectors.counting()));

        return consultationsPerDoctor.entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(limit)
                .map(entry -> {
                    Long doctorId = entry.getKey();
                    DoctorRead doctor = reportDataService.getDoctorInfo(doctorId);
                    double share = !consultations.isEmpty() ?
                            Math.round((entry.getValue() / (double) consultations.size()) * 10000.0) / 100.0 : 0.0;

                    return SpecialtyReportResponseDTO.TopDoctorDTO.builder()
                            .doctorId(doctorId)
                            .doctorName(reportDataService.formatDoctorName(doctor, doctorId))
                            .totalConsultations(entry.getValue().intValue())
                            .consultationShare(share)
                            .build();
                })
                .collect(Collectors.toList());
    }

    private ReportKpisDTO buildKpis(List<MedicalConsultation> consultations) {
        long totalConsultations = consultations.size();
        long uniqueDoctors = consultations.stream().map(MedicalConsultation::getDoctorId).distinct().count();
        long uniquePatients = consultations.stream().map(MedicalConsultation::getPatientId).distinct().count();
        long uniqueCenters = consultations.stream().map(MedicalConsultation::getCenterId).distinct().count();

        long withDiagnosis = consultations.stream()
                .filter(cons -> cons.getDiagnosis() != null && !cons.getDiagnosis().trim().isEmpty())
                .count();
        long withTreatment = consultations.stream()
                .filter(cons -> cons.getTreatment() != null && !cons.getTreatment().trim().isEmpty())
                .count();

        double dataCompleteness = totalConsultations > 0 ?
                Math.round(((withDiagnosis + withTreatment) / (double) (2 * totalConsultations)) * 10000.0) / 100.0 : 0.0;

        return ReportKpisDTO.builder()
                .distinctSpecialties((long) consultations.stream()
                        .map(cons -> reportDataService.getDoctorSpecialty(cons.getDoctorId()))
                        .distinct()
                        .count())
                .doctorsInvolved(uniqueDoctors)
                .medicalCentersInvolved(uniqueCenters)
                .uniquePatientsTotal(uniquePatients)
                .avgConsultationsPerDoctor(Math.round((totalConsultations / (double) Math.max(uniqueDoctors, 1)) * 100.0) / 100.0)
                .dataQuality(ReportKpisDTO.DataQualityDTO.builder()
                        .consultationsWithDiagnosis(withDiagnosis)
                        .consultationsWithTreatment(withTreatment)
                        .dataCompletenessPercentage(dataCompleteness)
                        .build())
                .build();
    }

    private List<DetailedConsultationDTO> buildDetailedConsultations(
            List<MedicalConsultation> consultations, int limit) {

        return consultations.stream()
                .limit(limit)
                .map(consultation -> {
                    DoctorRead doctor = reportDataService.getDoctorInfo(consultation.getDoctorId());

                    return DetailedConsultationDTO.builder()
                            .consultationId(consultation.getId())
                            .patientName(reportDataService.getPatientName(consultation.getPatientId()))
                            .doctorName(reportDataService.formatDoctorName(doctor, consultation.getDoctorId()))
                            .specialty(reportDataService.getDoctorSpecialty(consultation.getDoctorId()))
                            .centerName(reportDataService.getCenterInfo(consultation.getCenterId()) != null ?
                                    reportDataService.getCenterInfo(consultation.getCenterId()).name() :
                                    "Centro ID: " + consultation.getCenterId())
                            .consultationDate(consultation.getDate())
                            .status(Boolean.TRUE.equals(consultation.getDeleted()) ? "CANCELLED" : "ACTIVE")
                            .diagnosis(consultation.getDiagnosis())
                            .treatment(consultation.getTreatment())
                            .notes(consultation.getNotes())
                            .build();
                })
                .collect(Collectors.toList());
    }

    private SpecialtyReportResponseDTO buildEmptyResponse() {
        Map<String, Object> emptyMetrics = new HashMap<>();

        return SpecialtyReportResponseDTO.builder()
                .executiveSummary(SpecialtyReportResponseDTO.ExecutiveSummaryDTO.builder()
                        .totalConsultations(0)
                        .dateRangeStart(LocalDate.now())
                        .dateRangeEnd(LocalDate.now())
                        .reportGeneratedAt(LocalDateTime.now())
                        .hasDateFilter(false)
                        .reportType("SPECIALTY")
                        .build())
                .specialtyStatistics(Collections.emptyList())
                .weeklyDistribution(Collections.emptyMap())
                .topActiveDoctors(Collections.emptyList())
                .kpis(ReportKpisDTO.builder()
                        .distinctSpecialties(0L)
                        .doctorsInvolved(0L)
                        .medicalCentersInvolved(0L)
                        .uniquePatientsTotal(0L)
                        .avgConsultationsPerDoctor(0.0)
                        .dataQuality(ReportKpisDTO.DataQualityDTO.builder()
                                .consultationsWithDiagnosis(0L)
                                .consultationsWithTreatment(0L)
                                .dataCompletenessPercentage(0.0)
                                .build())
                        .additionalMetrics(emptyMetrics)
                        .build())
                .detailedConsultations(Collections.emptyList())
                .paginationInfo(PaginationInfoDTO.builder()
                        .currentPage(0)
                        .totalPages(0)
                        .totalElements(0L)
                        .pageSize(0)
                        .hasNext(false)
                        .hasPrevious(false)
                        .build())
                .build();
    }

    /**
     * Clase auxiliar para estadísticas por especialidad
     */
    private static class SpecialtyStats {
        int totalConsultations = 0;
        Set<Long> doctors = new HashSet<>();
        Set<Long> patients = new HashSet<>();
    }
}