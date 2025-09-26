package com.drtx.jdit.reportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO para reportes de consultas agrupadas por mes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte mensual de consultas médicas")
public class ConsultaMensualDTO {

    @Schema(description = "Año de las consultas", example = "2025")
    private Integer anio;

    @Schema(description = "Mes de las consultas (1-12)", example = "9")
    private Integer mes;

    @Schema(description = "Cantidad total de consultas en el mes", example = "150")
    private Integer totalConsultas;

    @Schema(description = "Ingresos totales generados en el mes", example = "12500.75")
    private Double ingresosTotales;
    
    @Builder.Default
    @Schema(description = "Resumen de consultas agrupadas por especialidad")
    private List<ResumenEspecialidad> especialidades = new ArrayList<>();
    
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de consultas por especialidad en un mes específico")
    public static class ResumenEspecialidad {
        
        @Schema(description = "Nombre de la especialidad", example = "Neurología")
        private String nombreEspecialidad;
        
        @Schema(description = "Número de consultas realizadas en esa especialidad", example = "20")
        private Integer cantidadConsultas;
        
        @Schema(description = "Ingresos generados por la especialidad", example = "2500.50")
        private Double ingresosGenerados;
    }
}