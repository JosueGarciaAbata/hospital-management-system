package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para consultas agrupadas por centro médico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaCentroMedicoDTO {
    private Long idCentro;
    private String nombreCentro;
    private String direccion;
    private Long totalConsultas;
    
    @Builder.Default
    private List<ConsultaDetalle> consultas = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultaDetalle {
        private Long id;
        private String nombreMedico;
        private String nombrePaciente;
        private String especialidad;
        private java.time.LocalDateTime fechaConsulta;
        private String estado;
        private Double costoConsulta;
    }
}