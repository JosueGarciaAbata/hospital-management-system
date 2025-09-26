package com.drtx.jdit.reportservice.dto;

/**
 * DTO para solicitar la generación de un reporte - version simple sin Lombok
 */
public class ReportRequestDTO {
    
    /**
     * Tipo de reporte a generar
     * Valores posibles: ESPECIALIDAD, MEDICO, CENTRO_MEDICO, MENSUAL
     */
    private String reportType;
    
    /**
     * Formato de exportación del reporte
     * Valores posibles: EXCEL, CSV, PDF
     */
    private String exportFormat;
    
    /**
     * Filtro por ID (especialidad, médico o centro médico dependiendo del reportType)
     */
    private Long filterId;
    
    /**
     * Filtro por fecha de inicio en formato yyyy-MM-dd
     */
    private String startDate;
    
    /**
     * Filtro por fecha de fin en formato yyyy-MM-dd
     */
    private String endDate;
    
    /**
     * Filtro por mes en formato yyyy-MM (usado para reportes mensuales)
     */
    private String month;

    // Constructors
    public ReportRequestDTO() {}

    public ReportRequestDTO(String reportType, String exportFormat, Long filterId, 
                           String startDate, String endDate, String month) {
        this.reportType = reportType;
        this.exportFormat = exportFormat;
        this.filterId = filterId;
        this.startDate = startDate;
        this.endDate = endDate;
        this.month = month;
    }

    // Getters
    public String getReportType() {
        return reportType;
    }

    public String getExportFormat() {
        return exportFormat;
    }

    public Long getFilterId() {
        return filterId;
    }

    public String getStartDate() {
        return startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public String getMonth() {
        return month;
    }

    // Setters
    public void setReportType(String reportType) {
        this.reportType = reportType;
    }

    public void setExportFormat(String exportFormat) {
        this.exportFormat = exportFormat;
    }

    public void setFilterId(Long filterId) {
        this.filterId = filterId;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public void setMonth(String month) {
        this.month = month;
    }

    @Override
    public String toString() {
        return "ReportRequestDTO{" +
                "reportType='" + reportType + '\'' +
                ", exportFormat='" + exportFormat + '\'' +
                ", filterId=" + filterId +
                ", startDate='" + startDate + '\'' +
                ", endDate='" + endDate + '\'' +
                ", month='" + month + '\'' +
                '}';
    }
}