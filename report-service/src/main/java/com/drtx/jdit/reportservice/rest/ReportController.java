package com.drtx.jdit.reportservice.rest;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import com.drtx.jdit.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Controlador para los endpoints de reportes
 */
@RestController
@RequestMapping("/api/report")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "Endpoints para generar reportes de consultas")
public class ReportController {

    private final ReportService reportService;

    /**
     * Endpoint para obtener consultas por especialidad
     */
    @GetMapping("/consultas-por-especialidad")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('STAFF')")
    @Operation(
            summary = "Obtener consultas por especialidad",
            description = "Devuelve una lista de consultas agrupadas por especialidad médica."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<ConsultaEspecialidadDTO>> getConsultasPorEspecialidad() {
        List<ConsultaEspecialidadDTO> consultas = reportService.getConsultasPorEspecialidad();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Endpoint para obtener consultas por médico
     */
    @GetMapping("/consultas-por-medico")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('STAFF')")
    @Operation(
            summary = "Obtener consultas por médico",
            description = "Devuelve una lista de consultas agrupadas por médico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<ConsultaMedicoDTO>> getConsultasPorMedico() {
        List<ConsultaMedicoDTO> consultas = reportService.getConsultasPorMedico();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Endpoint para obtener consultas por centro médico
     */
    @GetMapping("/consultas-por-centro")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('STAFF')")
    @Operation(
            summary = "Obtener consultas por centro médico",
            description = "Devuelve una lista de consultas agrupadas por centro médico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<ConsultaCentroMedicoDTO>> getConsultasPorCentro() {
        List<ConsultaCentroMedicoDTO> consultas = reportService.getConsultasPorCentro();
        return ResponseEntity.ok(consultas);
    }

    /**
     * Endpoint para obtener consultas mensuales
     */
    @GetMapping("/consultas-mensuales")
    @PreAuthorize("hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('STAFF')")
    @Operation(
            summary = "Obtener consultas mensuales",
            description = "Devuelve un reporte de consultas agrupadas por mes y especialidad."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista obtenida con éxito"),
            @ApiResponse(responseCode = "403", description = "Acceso denegado")
    })
    public ResponseEntity<List<ConsultaMensualDTO>> getConsultasMensuales() {
        List<ConsultaMensualDTO> consultas = reportService.getConsultasMensuales();
        return ResponseEntity.ok(consultas);
    }
}
