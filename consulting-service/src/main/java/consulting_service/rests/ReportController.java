package consulting_service.rests;

import consulting_service.dtos.reports.*;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.reports.ReportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.util.List;

/**
 * Controlador para endpoints de reportes en el servicio de consultas
 */
@RestController
@RequestMapping("/api/consulting/reports")
@Tag(name = "Reportes de Consultas", description = "Generación de reportes estadísticos de consultas médicas")
public class ReportController {

    private static final Logger logger = LoggerFactory.getLogger(ReportController.class);
    private final ReportService reportService;

    public ReportController(ReportService reportService) {
        this.reportService = reportService;
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-specialty")
    @Operation(
            summary = "Reporte de consultas por especialidad",
            description = "Devuelve una lista de estadísticas de consultas agrupadas por especialidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Reporte generado correctamente"),
            @ApiResponse(responseCode = "500", description = "Error al generar el reporte")
    })
    public ResponseEntity<List<ConsultaEspecialidadDTO>> getConsultasBySpecialty() {
        logger.info("Recibida solicitud para obtener consultas por especialidad");
        List<ConsultaEspecialidadDTO> consultations = reportService.getConsultationsBySpecialty();
        logger.info("Consultas por especialidad obtenidas con éxito: {} registros", consultations.size());
        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-doctor")
    @Operation(
            summary = "Reporte de consultas por doctor",
            description = "Devuelve una lista de estadísticas de consultas agrupadas por cada doctor."
    )
    public ResponseEntity<List<ConsultaMedicoDTO>> getConsultationsByDoctor() {
        List<ConsultaMedicoDTO> consultations = reportService.getConsultationsByDoctor();
        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-center")
    @Operation(
            summary = "Reporte de consultas por centro médico",
            description = "Devuelve una lista de estadísticas de consultas agrupadas por centro médico."
    )
    public ResponseEntity<List<ConsultaCentroMedicoDTO>> getConsultationsByCenter() {
        List<ConsultaCentroMedicoDTO> consultations = reportService.getConsultationsByCenter();
        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-month")
    @Operation(
            summary = "Reporte de consultas por mes",
            description = "Devuelve una lista de estadísticas de consultas médicas agrupadas por mes."
    )
    public ResponseEntity<List<ConsultaMensualDTO>> getConsultationsByMonth() {
        List<ConsultaMensualDTO> consultations = reportService.getConsultationsByMonth();
        return ResponseEntity.ok(consultations);
    }
}
