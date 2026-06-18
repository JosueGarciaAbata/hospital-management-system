package consulting_service.rests;

import consulting_service.dtos.request.*;
import consulting_service.dtos.response.reports.DoctorReportResponseDTO;
import consulting_service.dtos.response.reports.MedicalCenterReportResponseDTO;
import consulting_service.dtos.response.reports.MonthlyReportResponseDTO;
import consulting_service.dtos.response.reports.SpecialtyReportResponseDTO;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.reports.ReportGenerationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Refactored controller for reports with proper DTOs and service separation
 */
@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/consulting/reports")
public class ReportController {

    private final ReportGenerationService reportService;

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-specialty")
    public ResponseEntity<SpecialtyReportResponseDTO> getConsultationsBySpecialty(
            @RequestBody SpecialtyReportRequestDTO request) {

        log.info("Received request for specialty report: {}", request);
        try {
            // Build Pageable from request DTO so Feign clients that only send body will control pagination
            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 20;
            Sort sort = Sort.by("date");
            if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
                sort = Sort.by(request.getSortBy());
            }
            if (request.getSortDirection() != null && request.getSortDirection().equalsIgnoreCase("asc")) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }

            Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

            SpecialtyReportResponseDTO response = reportService.generateSpecialtyReport(request, pageable);
            return ResponseEntity.ok()
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .body(response);
        } catch (Exception e) {
            log.error("Error generating specialty report", e);
            throw e;
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-doctor")
    public ResponseEntity<DoctorReportResponseDTO> getConsultationsByDoctor(
            @RequestBody DoctorReportRequestDTO request) {

        log.info("Received request for doctor report: {}", request);
        try {
            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 20;
            Sort sort = Sort.by("date");
            if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
                sort = Sort.by(request.getSortBy());
            }
            if (request.getSortDirection() != null && request.getSortDirection().equalsIgnoreCase("asc")) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }

            Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

            DoctorReportResponseDTO response = reportService.generateDoctorReport(request, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating doctor report", e);
            throw e;
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-center")
    public ResponseEntity<MedicalCenterReportResponseDTO> getConsultationsByMedicalCenter(
            @RequestBody MedicalCenterReportRequestDTO request) {

        log.info("Received request for medical center report: {}", request);
        try {
            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 20;
            Sort sort = Sort.by("date");
            if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
                sort = Sort.by(request.getSortBy());
            }
            if (request.getSortDirection() != null && request.getSortDirection().equalsIgnoreCase("asc")) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }

            Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

            MedicalCenterReportResponseDTO response = reportService.generateMedicalCenterReport(request, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating medical center report", e);
            throw e;
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @PostMapping("/by-month")
    public ResponseEntity<MonthlyReportResponseDTO> getConsultationsByMonth(
            @RequestBody MonthlyReportRequestDTO request) {

        log.info("Received request for monthly report: {}", request);
        try {
            int page = request.getPage() != null ? request.getPage() : 0;
            int size = request.getSize() != null ? request.getSize() : 20;
            Sort sort = Sort.by("date");
            if (request.getSortBy() != null && !request.getSortBy().isEmpty()) {
                sort = Sort.by(request.getSortBy());
            }
            if (request.getSortDirection() != null && request.getSortDirection().equalsIgnoreCase("asc")) {
                sort = sort.ascending();
            } else {
                sort = sort.descending();
            }

            Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);

            MonthlyReportResponseDTO response = reportService.generateMonthlyReport(request, pageable);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("Error generating monthly report", e);
            throw e;
        }
    }
}