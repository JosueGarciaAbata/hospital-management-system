package consulting_service.services.reports;

import consulting_service.dtos.request.DoctorReportRequestDTO;
import consulting_service.dtos.response.reports.DoctorReportResponseDTO;
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

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes por doctor
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorReportService {

    private final MedicalConsultationsRepository consultationsRepository;
    private final ReportDataService reportDataService;
    private final ReportUtilsService reportUtils;

    /**
     * Genera un reporte detallado por doctor
     */
    public DoctorReportResponseDTO generateReport(DoctorReportRequestDTO request, Pageable pageable) {
        log.info("Generando reporte de doctor: {}", request);

        LocalDateTime startDate = reportUtils.toStartOfDay(request.getStartDate());
        LocalDateTime endDate = reportUtils.toEndOfDay(request.getEndDate());

        Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                startDate,
                endDate,
                null,
                request.getMedicalCenters(),
                Collections.emptyList(),  // <-- corregido
                request.getDoctors()
        );

        Page<MedicalConsultation> consultationsPage = consultationsRepository.findAll(spec, pageable);
        List<MedicalConsultation> consultations = consultationsPage.getContent();

        if (consultations.isEmpty()) {
            return buildEmptyDoctorResponse();
        }

        return DoctorReportResponseDTO.builder()
                .executiveSummary(buildExecutiveSummary(consultations, startDate, endDate))
                .doctorStatistics(buildDoctorStatistics(consultations))
                .weeklyDistribution(reportUtils.buildWeeklyDistribution(consultations))
                .kpis(buildDoctorKpis(consultations))
                .detailedConsultations(reportDataService.buildDetailedConsultations(consultations, 15))
                .paginationInfo(reportUtils.buildPaginationInfo(consultationsPage))
                .build();
    }

    private DoctorReportResponseDTO.ExecutiveSummaryDTO buildExecutiveSummary(
            List<MedicalConsultation> consultations, LocalDateTime start, LocalDateTime end) {

        long uniqueDoctors = consultations.stream()
                .map(MedicalConsultation::getDoctorId)
                .distinct()
                .count();

        return DoctorReportResponseDTO.ExecutiveSummaryDTO.builder()
                .totalConsultations(consultations.size())
                .uniqueDoctors(uniqueDoctors)
                .dateRangeStart(start != null ? start.toLocalDate() : null)
                .dateRangeEnd(end != null ? end.toLocalDate() : null)
                .reportGeneratedAt(LocalDateTime.now())
                .hasDateFilter(start != null || end != null)
                .reportType("DOCTOR_PERFORMANCE")
                .build();
    }

    private List<DoctorReportResponseDTO.DoctorStatisticDTO> buildDoctorStatistics(List<MedicalConsultation> consultations) {
        Map<Long, DoctorStats> statsMap = new HashMap<>();

        for (MedicalConsultation consultation : consultations) {
            DoctorStats stats = statsMap.computeIfAbsent(consultation.getDoctorId(), k -> new DoctorStats());
            stats.totalConsultations++;
            stats.patients.add(consultation.getPatientId());

            if (consultation.getDiagnosis() != null && !consultation.getDiagnosis().trim().isEmpty()) {
                stats.consultationsWithDiagnosis++;
            }
            if (consultation.getTreatment() != null && !consultation.getTreatment().trim().isEmpty()) {
                stats.consultationsWithTreatment++;
            }
        }

        return statsMap.entrySet().stream()
                .map(entry -> {
                    Long doctorId = entry.getKey();
                    DoctorStats stats = entry.getValue();
                    DoctorRead doctor = reportDataService.getDoctorInfo(doctorId);

                    double efficiencyRate = stats.totalConsultations > 0 ?
                            Math.round((stats.consultationsWithDiagnosis / (double) stats.totalConsultations) * 10000.0) / 100.0 : 0.0;
                    double consultationShare = !consultations.isEmpty() ?
                            Math.round((stats.totalConsultations / (double) consultations.size()) * 10000.0) / 100.0 : 0.0;

                    return DoctorReportResponseDTO.DoctorStatisticDTO.builder()
                            .doctorId(doctorId)
                            .doctorName(reportDataService.getDoctorName(doctorId))
                            .specialty(doctor != null ? doctor.specialtyName() : "Sin especialidad")
                            .totalConsultations(stats.totalConsultations)
                            .uniquePatients(stats.patients.size())
                            .consultationsWithDiagnosis(stats.consultationsWithDiagnosis)
                            .consultationsWithTreatment(stats.consultationsWithTreatment)
                            .efficiencyRate(efficiencyRate)
                            .consultationShare(consultationShare)
                            .build();
                })
                .sorted((a, b) -> Integer.compare(b.getTotalConsultations(), a.getTotalConsultations()))
                .collect(Collectors.toList());
    }

    private consulting_service.dtos.response.reports.ReportKpisDTO buildDoctorKpis(List<MedicalConsultation> consultations) {
        consulting_service.dtos.response.reports.ReportKpisDTO baseKpis = reportDataService.buildKpis(consultations);

        Map<Long, Long> consultationsPerDoctor = consultations.stream()
                .collect(Collectors.groupingBy(MedicalConsultation::getDoctorId, Collectors.counting()));

        if (!consultationsPerDoctor.isEmpty()) {
            long max = Collections.max(consultationsPerDoctor.values());
            long min = Collections.min(consultationsPerDoctor.values());
            double avg = consultationsPerDoctor.values().stream().mapToLong(Long::longValue).average().orElse(0.0);

            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("maxConsultationsPerDoctor", max);
            additionalMetrics.put("minConsultationsPerDoctor", min);
            additionalMetrics.put("avgConsultationsPerDoctor", Math.round(avg * 100.0) / 100.0);
            additionalMetrics.put("performanceGap", max - min);

            baseKpis.setAdditionalMetrics(additionalMetrics);
        }

        return baseKpis;
    }

    private DoctorReportResponseDTO buildEmptyDoctorResponse() {
        return DoctorReportResponseDTO.builder()
                .executiveSummary(DoctorReportResponseDTO.ExecutiveSummaryDTO.builder()
                        .totalConsultations(0)
                        .uniqueDoctors(0L)
                        .reportGeneratedAt(LocalDateTime.now())
                        .hasDateFilter(false)
                        .reportType("DOCTOR_PERFORMANCE")
                        .build())
                .doctorStatistics(Collections.emptyList())
                .weeklyDistribution(Collections.emptyMap())
                .kpis(reportDataService.buildEmptyKpis())
                .detailedConsultations(Collections.emptyList())
                .paginationInfo(reportDataService.buildEmptyPaginationInfo())
                .build();
    }

    /**
     * Clase auxiliar para estadísticas por doctor
     */
    private static class DoctorStats {
        int totalConsultations = 0;
        int consultationsWithDiagnosis = 0;
        int consultationsWithTreatment = 0;
        Set<Long> patients = new HashSet<>();
    }
}
