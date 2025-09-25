package consulting_service.rests;

import consulting_service.dtos.reports.*;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.reports.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador para endpoints de reportes en el servicio de consultas
 */
@RestController
@RequestMapping("/api/consulting/reports")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-specialty")
    public ResponseEntity<List<ConsultaEspecialidadDTO>> getConsultasBySpecialty() {
        logger.info("Recibida solicitud para obtener consultas por especialidad");
        try {
            List<ConsultaEspecialidadDTO> consultations = reportService.getConsultationsBySpecialty();
            logger.info("Consultas por especialidad obtenidas con éxito: {} registros", consultations.size());
            return ResponseEntity.ok(consultations);
        } catch (Exception e) {
            logger.error("Error al obtener consultas por especialidad", e);
            throw e; // Relanzar para que Spring maneje la respuesta de error
        }
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-doctor")
    public ResponseEntity<List<ConsultaMedicoDTO>> getConsultationsByDoctor() {
        List<ConsultaMedicoDTO> consultations = reportService.getConsultationsByDoctor();
        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-center")
    public ResponseEntity<List<ConsultaCentroMedicoDTO>> getConsultationsByCenter() {
        List<ConsultaCentroMedicoDTO> consultations = reportService.getConsultationsByCenter();
        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-month")
    public ResponseEntity<List<ConsultaMensualDTO>> getConsultationsByMonth() {
        List<ConsultaMensualDTO> consultations = reportService.getConsultationsByMonth();
        return ResponseEntity.ok(consultations);
    }
}