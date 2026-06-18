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
 * DTO para reportes de consultas agrupadas por centro médico.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por centro médico")
public class ConsultaCentroMedicoDTO {

    @Schema(description = "Identificador del centro médico", example = "1")
    private Long idCentro;

    @Schema(description = "Nombre del centro médico", example = "Hospital Central")
    private String nombreCentro;

    @Schema(description = "Dirección del centro médico", example = "Av. Amazonas 1234")
    private String direccion;

    @Schema(description = "Total de consultas en el centro médico", example = "150")
    private Long totalConsultas;
    
    @Builder.Default
    @Schema(description = "Listado de consultas asociadas al centro médico")
    private List<ConsultaDetalle> consultas = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de una consulta médica realizada en un centro")
    public static class ConsultaDetalle {
        
        @Schema(description = "Identificador de la consulta", example = "101")
        private Long id;
        
        @Schema(description = "Nombre del médico que atendió la consulta", example = "Dr. Juan Pérez")
        private String nombreMedico;
        
        @Schema(description = "Especialidad del médico", example = "Cardiología")
        private String especialidad;
        
        @Schema(description = "Nombre del paciente atendido", example = "María López")
        private String nombrePaciente;
        
        @Schema(description = "Fecha y hora de la consulta", example = "2025-09-24T15:30:00")
        private LocalDateTime fechaConsulta;
        
        @Schema(description = "Estado de la consulta", example = "FINALIZADA")
        private String estado;
        
        @Schema(description = "Costo de la consulta", example = "85.50")
        private Double costoConsulta;
    }
}