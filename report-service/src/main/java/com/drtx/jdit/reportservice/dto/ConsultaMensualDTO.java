package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para consultas agrupadas por mes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaMensualDTO {
    private Integer anio;
    private Integer mes;
    private Integer totalConsultas;
    private Double ingresosTotales;
    
    @Builder.Default
    private List<ResumenEspecialidad> especialidades = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenEspecialidad {
        private String nombreEspecialidad;
        private Integer cantidadConsultas;
        private Double ingresosGenerados;
    }
}