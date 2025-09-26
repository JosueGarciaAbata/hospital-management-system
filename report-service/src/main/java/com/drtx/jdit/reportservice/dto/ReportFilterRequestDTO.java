package com.drtx.jdit.reportservice.dto;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para filtros de reportes - version simple sin Lombok
 */
public class ReportFilterRequestDTO {
    
    private LocalDate startDate;
    private LocalDate endDate;
    private List<Long> specialtyIds;
    private List<Long> doctorIds;
    private List<Long> medicalCenterIds;
    private int page = 0;
    private int size = 10;
    private String sortBy = "consultationDate";
    private String sortDirection = "DESC";
    private boolean includeAdditionalData = false;

    // Constructors
    public ReportFilterRequestDTO() {}

    public ReportFilterRequestDTO(LocalDate startDate, LocalDate endDate, 
                                 List<Long> specialtyIds, List<Long> doctorIds, 
                                 List<Long> medicalCenterIds, int page, int size, 
                                 String sortBy, String sortDirection, boolean includeAdditionalData) {
        this.startDate = startDate;
        this.endDate = endDate;
        this.specialtyIds = specialtyIds;
        this.doctorIds = doctorIds;
        this.medicalCenterIds = medicalCenterIds;
        this.page = page;
        this.size = size;
        this.sortBy = sortBy;
        this.sortDirection = sortDirection;
        this.includeAdditionalData = includeAdditionalData;
    }

    // Getters
    public LocalDate getStartDate() {
        return startDate;
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public List<Long> getSpecialtyIds() {
        return specialtyIds;
    }

    public List<Long> getDoctorIds() {
        return doctorIds;
    }

    public List<Long> getMedicalCenterIds() {
        return medicalCenterIds;
    }

    public int getPage() {
        return page;
    }

    public int getSize() {
        return size;
    }

    public String getSortBy() {
        return sortBy;
    }

    public String getSortDirection() {
        return sortDirection;
    }

    // Setters
    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }

    public void setEndDate(LocalDate endDate) {
        this.endDate = endDate;
    }

    public void setSpecialtyIds(List<Long> specialtyIds) {
        this.specialtyIds = specialtyIds;
    }

    public void setDoctorIds(List<Long> doctorIds) {
        this.doctorIds = doctorIds;
    }

    public void setMedicalCenterIds(List<Long> medicalCenterIds) {
        this.medicalCenterIds = medicalCenterIds;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public void setSortBy(String sortBy) {
        this.sortBy = sortBy;
    }

    public void setSortDirection(String sortDirection) {
        this.sortDirection = sortDirection;
    }

    public boolean isIncludeAdditionalData() {
        return includeAdditionalData;
    }

    public void setIncludeAdditionalData(boolean includeAdditionalData) {
        this.includeAdditionalData = includeAdditionalData;
    }

    // Métodos adicionales que necesita ReportServiceImpl
    public List<Long> getMedicalCenters() {
        return medicalCenterIds;
    }

    public void setMedicalCenters(List<Long> medicalCenters) {
        this.medicalCenterIds = medicalCenters;
    }

    public List<Long> getSpecialties() {
        return specialtyIds;
    }

    public void setSpecialties(List<Long> specialties) {
        this.specialtyIds = specialties;
    }

    public List<Long> getDoctors() {
        return doctorIds;
    }

    public void setDoctors(List<Long> doctors) {
        this.doctorIds = doctors;
    }

    public String getStatus() {
        return "ACTIVE"; // valor por defecto
    }

    public void setStatus(String status) {
        // Se puede implementar si se necesita un campo status
    }

    @Override
    public String toString() {
        return "ReportFilterRequestDTO{" +
                "startDate=" + startDate +
                ", endDate=" + endDate +
                ", specialtyIds=" + specialtyIds +
                ", doctorIds=" + doctorIds +
                ", medicalCenterIds=" + medicalCenterIds +
                ", page=" + page +
                ", size=" + size +
                ", sortBy='" + sortBy + '\'' +
                ", sortDirection='" + sortDirection + '\'' +
                ", includeAdditionalData=" + includeAdditionalData +
                '}';
    }
}