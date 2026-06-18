package com.drtx.jdit.reportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO para reportes de consultas agrupadas por médico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por médico")
public class ConsultaMedicoDTO {

    @Schema(description = "Identificador del médico", example = "301")
    private Long idMedico;

    @Schema(description = "Nombre del médico", example = "Dr. Luis Herrera")
    private String nombreMedico;

    @Schema(description = "Especialidad del médico", example = "Dermatología")
    private String especialidad;

    @Schema(description = "Total de consultas realizadas por el médico", example = "89")
    private Long totalConsultas;
    
    @Builder.Default
    @Schema(description = "Listado de consultas realizadas por el médico")
    private List<ConsultaDetalle> consultas = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de una consulta atendida por un médico")
    public static class ConsultaDetalle {
        
        @Schema(description = "Identificador de la consulta", example = "401")
        private Long id;
        
        @Schema(description = "Nombre del paciente", example = "Pedro Jiménez")
        private String nombrePaciente;
        
        @Schema(description = "Fecha y hora de la consulta", example = "2025-09-20T11:15:00")
        private LocalDateTime fechaConsulta;
        
        @Schema(description = "Estado de la consulta", example = "CANCELADA")
        private String estado;
        
        @Schema(description = "Notas adicionales de la consulta", example = "Paciente con alergia medicamentosa")
        private String notas;
        
        @Schema(description = "Costo de la consulta", example = "95.00")
        private Double costoConsulta;
        
        @Schema(description = "Nombre del centro médico", example = "Hospital General")
        private String centroMedico;
    }
}