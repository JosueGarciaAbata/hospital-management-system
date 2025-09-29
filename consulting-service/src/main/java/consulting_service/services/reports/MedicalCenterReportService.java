package consulting_service.services.reports;

import consulting_service.dtos.request.MedicalCenterReportRequestDTO;
import consulting_service.dtos.response.reports.MedicalCenterReportResponseDTO;
import consulting_service.dtos.response.reports.ReportKpisDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.repositories.MedicalConsultationsRepository;
import consulting_service.specifications.MedicalConsultationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes por centro médico
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicalCenterReportService {

    private final MedicalConsultationsRepository consultationsRepository;
    private final ReportDataService reportDataService;
    private final ReportUtilsService reportUtils;

    /**
     * Genera un reporte detallado por centro médico
     */
    public MedicalCenterReportResponseDTO generateReport(MedicalCenterReportRequestDTO request, Pageable pageable) {
        log.info("Generando reporte de centro médico: {}", request);

        LocalDateTime startDate = reportUtils.toStartOfDay(request.getStartDate());
        LocalDateTime endDate = reportUtils.toEndOfDay(request.getEndDate());

        // ✅ Ajuste: ahora enviamos Collections.emptyList() como specialtyIds
        Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                startDate,
                endDate,
                null,
                request.getMedicalCenters(),
                Collections.emptyList(),   // <-- specialtyIds vacías
                request.getDoctors()
        );

        Page<MedicalConsultation> consultationsPage = consultationsRepository.findAll(spec, pageable);
        List<MedicalConsultation> consultations = consultationsPage.getContent();

        if (consultations.isEmpty()) {
            return buildEmptyMedicalCenterResponse();
        }

        return MedicalCenterReportResponseDTO.builder()
                .executiveSummary(buildExecutiveSummary(consultations, startDate, endDate))
                .centerStatistics(buildCenterStatistics(consultations))
                .weeklyDistribution(reportUtils.buildWeeklyDistribution(consultations))
                .kpis(buildCenterKpis(consultations))
                .detailedConsultations(reportDataService.buildDetailedConsultations(consultations, 15))
                .paginationInfo(reportUtils.buildPaginationInfo(consultationsPage))
                .build();
    }

    private MedicalCenterReportResponseDTO.ExecutiveSummaryDTO buildExecutiveSummary(
            List<MedicalConsultation> consultations, LocalDateTime start, LocalDateTime end) {

        long uniqueCenters = consultations.stream()
                .map(MedicalConsultation::getCenterId)
                .distinct()
                .count();

        return MedicalCenterReportResponseDTO.ExecutiveSummaryDTO.builder()
                .totalConsultations(consultations.size())
                .uniqueCenters(uniqueCenters)
                .dateRangeStart(start != null ? start.toLocalDate() : null)
                .dateRangeEnd(end != null ? end.toLocalDate() : null)
                .reportGeneratedAt(LocalDateTime.now())
                .hasDateFilter(start != null || end != null)
                .reportType("MEDICAL_CENTER_ANALYSIS")
                .build();
    }

    private List<MedicalCenterReportResponseDTO.MedicalCenterStatisticDTO> buildCenterStatistics(List<MedicalConsultation> consultations) {
        Map<Long, CenterStats> statsMap = new HashMap<>();

        for (MedicalConsultation consultation : consultations) {
            CenterStats stats = statsMap.computeIfAbsent(consultation.getCenterId(), k -> new CenterStats());
            stats.totalConsultations++;
            stats.doctors.add(consultation.getDoctorId());
            stats.patients.add(consultation.getPatientId());
        }

        return statsMap.entrySet().stream()
                .map(entry -> {
                    Long centerId = entry.getKey();
                    CenterStats stats = entry.getValue();
                    String centerName = reportDataService.getCenterName(centerId);

                    double consultationsPerDoctor = !stats.doctors.isEmpty() ?
                            Math.round((stats.totalConsultations / (double) stats.doctors.size()) * 100.0) / 100.0 : 0.0;
                    double marketShare = !consultations.isEmpty() ?
                            Math.round((stats.totalConsultations / (double) consultations.size()) * 10000.0) / 100.0 : 0.0;

                    return MedicalCenterReportResponseDTO.MedicalCenterStatisticDTO.builder()
                            .centerId(centerId)
                            .centerName(centerName)
                            .totalConsultations(stats.totalConsultations)
                            .uniqueDoctors(stats.doctors.size())
                            .uniquePatients(stats.patients.size())
                            .consultationsPerDoctor(consultationsPerDoctor)
                            .marketShare(marketShare)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalConsultations(), a.getTotalConsultations()))
                .collect(Collectors.toList());
    }

    private ReportKpisDTO buildCenterKpis(List<MedicalConsultation> consultations) {
        ReportKpisDTO baseKpis = reportDataService.buildKpis(consultations);

        Map<Long, Long> consultationsPerCenter = consultations.stream()
                .collect(Collectors.groupingBy(MedicalConsultation::getCenterId, Collectors.counting()));

        if (!consultationsPerCenter.isEmpty()) {
            long max = Collections.max(consultationsPerCenter.values());

            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("busiestCenterConsultations", max);
            additionalMetrics.put("centerUtilizationRate", !consultations.isEmpty() ?
                    Math.round((max / (double) consultations.size()) * 10000.0) / 100.0 : 0.0);

            baseKpis.setAdditionalMetrics(additionalMetrics);
        }

        return baseKpis;
    }

    private MedicalCenterReportResponseDTO buildEmptyMedicalCenterResponse() {
        return MedicalCenterReportResponseDTO.builder()
                .executiveSummary(MedicalCenterReportResponseDTO.ExecutiveSummaryDTO.builder()
                        .totalConsultations(0)
                        .uniqueCenters(0L)
                        .reportGeneratedAt(LocalDateTime.now())
                        .hasDateFilter(false)
                        .reportType("MEDICAL_CENTER_ANALYSIS")
                        .build())
                .centerStatistics(Collections.emptyList())
                .weeklyDistribution(Collections.emptyMap())
                .kpis(reportDataService.buildEmptyKpis())
                .detailedConsultations(Collections.emptyList())
                .paginationInfo(reportDataService.buildEmptyPaginationInfo())
                .build();
    }

    /**
     * Clase auxiliar para estadísticas por centro médico
     */
    private static class CenterStats {
        int totalConsultations = 0;
        Set<Long> doctors = new HashSet<>();
        Set<Long> patients = new HashSet<>();
    }
}
