package com.drtx.jdit.reportservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para reportes de consultas agrupadas por mes.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte mensual de consultas médicas")
public class ConsultaMensualDTO {

    @Schema(description = "Mes de las consultas (1-12)", example = "9")
    private int mes;

    @Schema(description = "Año de las consultas", example = "2025")
    private int anio;

    @Schema(description = "Cantidad total de consultas en el mes", example = "150")
    private int totalConsultas;

    @Schema(description = "Resumen de consultas agrupadas por especialidad")
    private List<ResumenEspecialidad> especialidades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de consultas por especialidad en un mes específico")
    public static class ResumenEspecialidad {
        @Schema(description = "Nombre de la especialidad", example = "Neurología")
        private String nombreEspecialidad;

        @Schema(description = "Número de consultas realizadas en esa especialidad", example = "20")
        private int cantidadConsultas;
    }
}
