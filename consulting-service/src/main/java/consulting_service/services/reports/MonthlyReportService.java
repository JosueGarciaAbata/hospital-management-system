package consulting_service.services.reports;

import consulting_service.dtos.request.MonthlyReportRequestDTO;
import consulting_service.dtos.response.reports.MonthlyReportResponseDTO;
import consulting_service.dtos.response.reports.ReportKpisDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.repositories.MedicalConsultationsRepository;
import consulting_service.specifications.MedicalConsultationSpecifications;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para generar reportes mensuales de actividad
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MonthlyReportService {

    private final MedicalConsultationsRepository consultationsRepository;
    private final consulting_service.services.reports.ReportDataService reportDataService;
    private final consulting_service.services.reports.ReportUtilsService reportUtils;

    /**
     * Genera un reporte detallado de actividad mensual
     */
    public MonthlyReportResponseDTO generateReport(MonthlyReportRequestDTO request, Pageable pageable) {
        log.info("Generando reporte mensual: {}", request);

        LocalDateTime startDate = reportUtils.toStartOfDay(request.getStartDate());
        LocalDateTime endDate = reportUtils.toEndOfDay(request.getEndDate());

        Specification<MedicalConsultation> spec = MedicalConsultationSpecifications.withFilters(
                startDate,
                endDate,
                null,
                request.getMedicalCenters(),
                Collections.emptyList(), // <-- aquí
                request.getDoctors()
        );
        // Para reportes mensuales recuperamos todas las consultas sin paginación
        List<MedicalConsultation> consultations = consultationsRepository.findAll(spec);

        if (consultations.isEmpty()) {
            return buildEmptyMonthlyResponse();
        }

        return MonthlyReportResponseDTO.builder()
                .executiveSummary(buildExecutiveSummary(consultations, startDate, endDate))
                .monthlyStatistics(buildMonthlyStatistics(consultations))
                .kpis(buildMonthlyKpis(consultations))
                .build();
    }

    private MonthlyReportResponseDTO.ExecutiveSummaryDTO buildExecutiveSummary(
            List<MedicalConsultation> consultations, LocalDateTime start, LocalDateTime end) {

        long monthsCount = consultations.stream()
                .map(cons -> cons.getDate().getMonthValue() + "-" + cons.getDate().getYear())
                .distinct()
                .count();

        return MonthlyReportResponseDTO.ExecutiveSummaryDTO.builder()
                .totalConsultations(consultations.size())
                .monthsAnalyzed(monthsCount)
                .dateRangeStart(start != null ? start.toLocalDate() : null)
                .dateRangeEnd(end != null ? end.toLocalDate() : null)
                .reportGeneratedAt(LocalDateTime.now())
                .hasDateFilter(start != null || end != null)
                .reportType("MONTHLY_TRENDS")
                .build();
    }

    private List<MonthlyReportResponseDTO.MonthlyStatisticDTO> buildMonthlyStatistics(List<MedicalConsultation> consultations) {
        Map<String, MonthlyStats> statsMap = new TreeMap<>();

        // Agrupa estadísticas por mes
        for (MedicalConsultation consultation : consultations) {
            int year = consultation.getDate().getYear();
            int month = consultation.getDate().getMonthValue();
            String period = String.format("%d-%02d", year, month);
            String periodDisplay = String.format("%02d/%d", month, year);

            MonthlyStats stats = statsMap.computeIfAbsent(period, k -> new MonthlyStats(periodDisplay));
            stats.totalConsultations++;
            stats.doctors.add(consultation.getDoctorId());
            stats.patients.add(consultation.getPatientId());

            String specialty = reportDataService.getDoctorSpecialty(consultation.getDoctorId());
            if (!"Sin especialidad".equals(specialty)) {
                stats.specialties.add(specialty);
            }
        }

        // Convierte y calcula crecimiento mensual
        List<MonthlyReportResponseDTO.MonthlyStatisticDTO> monthlyList = new ArrayList<>();
        MonthlyReportResponseDTO.MonthlyStatisticDTO previousMonth = null;

        for (MonthlyStats stats : statsMap.values()) {
            double growth = 0.0;
            if (previousMonth != null) {
                int previousConsultations = previousMonth.getTotalConsultations();
                growth = previousConsultations > 0 ?
                        ((stats.totalConsultations - previousConsultations) / (double) previousConsultations) * 100 : 0.0;
            }

            MonthlyReportResponseDTO.MonthlyStatisticDTO monthDto = MonthlyReportResponseDTO.MonthlyStatisticDTO.builder()
                    .period(stats.periodDisplay)
                    .totalConsultations(stats.totalConsultations)
                    .uniquePatients(stats.patients.size())
                    .uniqueDoctors(stats.doctors.size())
                    .specialtyCount(stats.specialties.size())
                    .growth(Math.round(growth * 10.0) / 10.0)
                    .build();

            monthlyList.add(monthDto);
            previousMonth = monthDto;
        }

        return monthlyList;
    }

    private ReportKpisDTO buildMonthlyKpis(List<MedicalConsultation> consultations) {
        ReportKpisDTO baseKpis = reportDataService.buildKpis(consultations);

        Map<String, Long> consultationsPerMonth = consultations.stream()
                .collect(Collectors.groupingBy(
                        cons -> String.format("%d-%02d", cons.getDate().getYear(), cons.getDate().getMonthValue()),
                        Collectors.counting()
                ));

        if (consultationsPerMonth.size() > 1) {
            Map<String, Object> additionalMetrics = new HashMap<>();
            additionalMetrics.put("monthsAnalyzed", consultationsPerMonth.size());
            additionalMetrics.put("seasonalityPattern", reportUtils.analyzeSeasonality(consultations));

            baseKpis.setAdditionalMetrics(additionalMetrics);
        }

        return baseKpis;
    }

    private MonthlyReportResponseDTO buildEmptyMonthlyResponse() {
        return MonthlyReportResponseDTO.builder()
                .executiveSummary(MonthlyReportResponseDTO.ExecutiveSummaryDTO.builder()
                        .totalConsultations(0)
                        .monthsAnalyzed(0L)
                        .reportGeneratedAt(LocalDateTime.now())
                        .hasDateFilter(false)
                        .reportType("MONTHLY_TRENDS")
                        .build())
                .monthlyStatistics(Collections.emptyList())
                .kpis(reportDataService.buildEmptyKpis())
                .build();
    }

    /**
     * Clase auxiliar para estadísticas mensuales
     */
    private static class MonthlyStats {
        final String periodDisplay;
        int totalConsultations = 0;
        Set<Long> doctors = new HashSet<>();
        Set<Long> patients = new HashSet<>();
        Set<String> specialties = new HashSet<>();

        MonthlyStats(String periodDisplay) {
            this.periodDisplay = periodDisplay;
        }
    }
}
