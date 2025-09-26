package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO para consultas agrupadas por especialidad
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaEspecialidadDTO {
    private Long id;
    private String especialidad;
    private String nombreMedico;
    private String nombrePaciente;
    private LocalDateTime fechaConsulta;
    private String estado;
    private String notas;
    private Double costoConsulta;
    private String centroMedico;
}