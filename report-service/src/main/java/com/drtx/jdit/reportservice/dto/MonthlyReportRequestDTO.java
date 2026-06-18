package com.drtx.jdit.reportservice.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para solicitar reportes mensuales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportRequestDTO {
    
    /**
     * Fecha de inicio del rango de consulta
     */
    @JsonProperty("startDate")
    private LocalDate fechaInicio;
    
    /**
     * Fecha de fin del rango de consulta
     */
    @JsonProperty("endDate")
    private LocalDate fechaFin;
    
    /**
     * Lista de IDs de centros médicos para filtrar
     */
    @JsonProperty("medicalCenters")
    private List<Long> centrosMedicos;
    
    /**
     * Lista de IDs de especialidades para filtrar
     */
    @JsonProperty("specialties")
    private List<Long> especialidades;
    
    /**
     * Lista de IDs de médicos para filtrar
     */
    @JsonProperty("doctors")
    private List<Long> medicos;
    
    /**
     * Estado de las consultas (ACTIVA, CANCELADA)
     */
    @JsonProperty("status")
    private String estado;
    
    /**
     * Campo por el cual ordenar
     */
    @JsonProperty("sortBy")
    private String ordenarPor;
    
    /**
     * Dirección del ordenamiento (ASC, DESC)
     */
    @JsonProperty("sortDirection")
    private String direccionOrden;
    
    /**
     * Número de página
     */
    @JsonProperty("page")
    private Integer pagina = 0;
    
    /**
     * Tamaño de página
     */
    @JsonProperty("size")
    private Integer tamanio = 20;
}