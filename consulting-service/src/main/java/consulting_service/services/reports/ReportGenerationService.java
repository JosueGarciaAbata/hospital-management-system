package consulting_service.services.reports;

import consulting_service.dtos.request.*;
import consulting_service.dtos.response.reports.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Fachada para acceder a los distintos servicios de reportes
 * Simplifica el uso de reportes delegando a los servicios especializados
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReportGenerationService {

    private final consulting_service.services.reports.SpecialtyReportService specialtyReportService;
    private final consulting_service.services.reports.DoctorReportService doctorReportService;
    private final MedicalCenterReportService centerReportService;
    private final MonthlyReportService monthlyReportService;

    /**
     * Genera un reporte por especialidades médicas
     */
    public SpecialtyReportResponseDTO generateSpecialtyReport(SpecialtyReportRequestDTO request, Pageable pageable) {
        log.info("Delegando generación de reporte de especialidades");
        return specialtyReportService.generateReport(request, pageable);
    }

    /**
     * Genera un reporte de desempeño de doctores
     */
    public DoctorReportResponseDTO generateDoctorReport(DoctorReportRequestDTO request, Pageable pageable) {
        log.info("Delegando generación de reporte de doctores");
        return doctorReportService.generateReport(request, pageable);
    }

    /**
     * Genera un reporte de centros médicos
     */
    public MedicalCenterReportResponseDTO generateMedicalCenterReport(MedicalCenterReportRequestDTO request, Pageable pageable) {
        log.info("Delegando generación de reporte de centros médicos");
        return centerReportService.generateReport(request, pageable);
    }

    /**
     * Genera un reporte mensual de actividad
     */
    public MonthlyReportResponseDTO generateMonthlyReport(MonthlyReportRequestDTO request, Pageable pageable) {
        log.info("Delegando generación de reporte mensual");
        return monthlyReportService.generateReport(request, pageable);
    }
}
