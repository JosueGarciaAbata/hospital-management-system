package com.drtx.jdit.reportservice.controller;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import com.drtx.jdit.reportservice.service.ReportService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/reportes")
@RequiredArgsConstructor
@Tag(name = "Reportes", description = "API para generar reportes estadísticos del sistema hospitalario")
public class ReportController {

    private final ReportService reportService;
    private static final String ROLES_PERMITIDOS = "hasRole('ADMIN') or hasRole('DOCTOR') or hasRole('STAFF')";

    @GetMapping("/consultas-por-especialidad")
    @PreAuthorize(ROLES_PERMITIDOS)
    @Operation(summary = "Obtiene estadísticas de consultas agrupadas por especialidad")
    public ResponseEntity<List<ConsultaEspecialidadDTO>> getConsultasPorEspecialidad() {
        List<ConsultaEspecialidadDTO> consultas = reportService.getConsultasPorEspecialidad();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/consultas-por-medico")
    @PreAuthorize(ROLES_PERMITIDOS)
    @Operation(summary = "Obtiene estadísticas de consultas agrupadas por médico")
    public ResponseEntity<List<ConsultaMedicoDTO>> getConsultasPorMedico() {
        List<ConsultaMedicoDTO> consultas = reportService.getConsultasPorMedico();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/consultas-por-centro")
    @PreAuthorize(ROLES_PERMITIDOS)
    @Operation(summary = "Obtiene estadísticas de consultas agrupadas por centro médico")
    public ResponseEntity<List<ConsultaCentroMedicoDTO>> getConsultasPorCentro() {
        List<ConsultaCentroMedicoDTO> consultas = reportService.getConsultasPorCentro();
        return ResponseEntity.ok(consultas);
    }

    @GetMapping("/consultas-mensuales")
    @PreAuthorize(ROLES_PERMITIDOS)
    @Operation(summary = "Obtiene estadísticas de consultas agrupadas por mes")
    public ResponseEntity<List<ConsultaMensualDTO>> getConsultasMensuales() {
        List<ConsultaMensualDTO> consultas = reportService.getConsultasMensuales();
        return ResponseEntity.ok(consultas);
    }
}
