package com.drtx.jdit.reportservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Class for storing aggregated consultation summary data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportSummaryDTO {

    /**
     * Total number of consultations
     */
    private long totalConsultations;
    
    /**
     * Number of consultations by status
     */
    private Map<String, Long> consultationsByStatus;
    
    /**
     * Average consultations per period
     */
    private BigDecimal averageConsultationsPerPeriod;
    
    /**
     * Number of distinct doctors
     */
    private long totalDoctors;
    
    /**
     * Number of distinct patients
     */
    private long totalPatients;
    
    /**
     * Number of distinct specialties
     */
    private long totalSpecialties;
    
    /**
     * Number of distinct medical centers
     */
    private long totalMedicalCenters;
    
    /**
     * Custom additional statistics
     */
    @Builder.Default
    private Map<String, Object> additionalStatistics = new HashMap<>();
    
    /**
     * Date of the first consultation in the data set
     */
    private LocalDateTime firstConsultation;
    
    /**
     * Date of the last consultation in the data set
     */
    private LocalDateTime lastConsultation;
    
    /**
     * Get total consultations (compatibility method)
     */
    public long getTotalConsultas() {
        return totalConsultations;
    }
    
    /**
     * Set total consultations (compatibility method)
     */
    public void setTotalConsultas(long totalConsultas) {
        this.totalConsultations = totalConsultas;
    }
    
    /**
     * Get consultations by status (compatibility method)
     */
    public Map<String, Long> getConsultasPorEstado() {
        return consultationsByStatus;
    }
    
    /**
     * Set consultations by status (compatibility method)
     */
    public void setConsultasPorEstado(Map<String, Long> consultasPorEstado) {
        this.consultationsByStatus = consultasPorEstado;
    }
    
    /**
     * Get average consultations per period (compatibility method)
     */
    public BigDecimal getPromedioConsultasPorPeriodo() {
        return averageConsultationsPerPeriod;
    }
    
    /**
     * Set average consultations per period (compatibility method)
     */
    public void setPromedioConsultasPorPeriodo(BigDecimal promedioConsultasPorPeriodo) {
        this.averageConsultationsPerPeriod = promedioConsultasPorPeriodo;
    }
    
    /**
     * Get total doctors (compatibility method)
     */
    public long getTotalMedicos() {
        return totalDoctors;
    }
    
    /**
     * Set total doctors (compatibility method)
     */
    public void setTotalMedicos(long totalMedicos) {
        this.totalDoctors = totalMedicos;
    }
    
    /**
     * Get total patients (compatibility method)
     */
    public long getTotalPacientes() {
        return totalPatients;
    }
    
    /**
     * Set total patients (compatibility method)
     */
    public void setTotalPacientes(long totalPacientes) {
        this.totalPatients = totalPacientes;
    }
    
    /**
     * Get total specialties (compatibility method)
     */
    public long getTotalEspecialidades() {
        return totalSpecialties;
    }
    
    /**
     * Set total specialties (compatibility method)
     */
    public void setTotalEspecialidades(long totalEspecialidades) {
        this.totalSpecialties = totalEspecialidades;
    }
    
    /**
     * Get total medical centers (compatibility method)
     */
    public long getTotalCentrosMedicos() {
        return totalMedicalCenters;
    }
    
    /**
     * Set total medical centers (compatibility method)
     */
    public void setTotalCentrosMedicos(long totalCentrosMedicos) {
        this.totalMedicalCenters = totalCentrosMedicos;
    }
    
    /**
     * Get additional statistics (compatibility method)
     */
    public Map<String, Object> getEstadisticasAdicionales() {
        return additionalStatistics;
    }
    
    /**
     * Set additional statistics (compatibility method)
     */
    public void setEstadisticasAdicionales(Map<String, Object> estadisticasAdicionales) {
        this.additionalStatistics = estadisticasAdicionales;
    }
    
    /**
     * Get first consultation (compatibility method)
     */
    public LocalDateTime getPrimeraConsulta() {
        return firstConsultation;
    }
    
    /**
     * Set first consultation (compatibility method)
     */
    public void setPrimeraConsulta(LocalDateTime primeraConsulta) {
        this.firstConsultation = primeraConsulta;
    }
    
    /**
     * Get last consultation (compatibility method)
     */
    public LocalDateTime getUltimaConsulta() {
        return lastConsultation;
    }
    
    /**
     * Set last consultation (compatibility method)
     */
    public void setUltimaConsulta(LocalDateTime ultimaConsulta) {
        this.lastConsultation = ultimaConsulta;
    }
}