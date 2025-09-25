package com.drtx.jdit.reportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO para reportes de consultas agrupadas por especialidad.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por especialidad")
public class ConsultaEspecialidadDTO {

    @Schema(description = "Identificador de la consulta", example = "201")
    private Long id;

    @Schema(description = "Nombre de la especialidad", example = "Pediatría")
    private String especialidad;

    @Schema(description = "Nombre del médico", example = "Dra. Ana Morales")
    private String nombreMedico;

    @Schema(description = "Nombre del paciente", example = "Carlos Sánchez")
    private String nombrePaciente;

    @Schema(description = "Fecha y hora de la consulta", example = "2025-09-24T09:00:00")
    private LocalDateTime fechaConsulta;

    @Schema(description = "Estado de la consulta", example = "PENDIENTE")
    private String estado;
}
