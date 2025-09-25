package com.drtx.jdit.reportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para reportes de consultas agrupadas por médico.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por médico")
public class ConsultaMedicoDTO {

    @Schema(description = "Identificador del médico", example = "301")
    private Long id;

    @Schema(description = "Nombre del médico", example = "Dr. Luis Herrera")
    private String nombreMedico;

    @Schema(description = "Especialidad del médico", example = "Dermatología")
    private String especialidad;

    @Schema(description = "Listado de consultas realizadas por el médico")
    private List<ConsultaDetalle> consultas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de una consulta atendida por un médico")
    public static class ConsultaDetalle {
        @Schema(description = "Identificador de la consulta", example = "401")
        private Long consultaId;

        @Schema(description = "Nombre del paciente", example = "Pedro Jiménez")
        private String nombrePaciente;

        @Schema(description = "Fecha y hora de la consulta", example = "2025-09-20T11:15:00")
        private LocalDateTime fechaConsulta;

        @Schema(description = "Estado de la consulta", example = "CANCELADA")
        private String estado;
    }
}
