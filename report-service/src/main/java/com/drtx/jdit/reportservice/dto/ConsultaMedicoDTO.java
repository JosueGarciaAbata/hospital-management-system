package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para consultas agrupadas por médico
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaMedicoDTO {
    private Long idMedico;
    private String nombreMedico;
    private String especialidad;
    private Long totalConsultas;
    
    @Builder.Default
    private List<ConsultaDetalle> consultas = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultaDetalle {
        private Long id;
        private String nombrePaciente;
        private java.time.LocalDateTime fechaConsulta;
        private String estado;
        private String notas;
        private Double costoConsulta;
        private String centroMedico;
    }
}