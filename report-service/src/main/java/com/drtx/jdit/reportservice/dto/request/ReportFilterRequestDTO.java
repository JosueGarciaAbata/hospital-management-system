package com.drtx.jdit.reportservice.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO for common report filters
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportFilterRequestDTO {
    
    /**
     * Start date for filtering consultations
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate startDate;
    
    /**
     * End date for filtering consultations
     */
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate endDate;
    
    /**
     * List of medical center IDs for filtering
     */
    private List<Long> medicalCenters;
    
    /**
     * List of specialty IDs for filtering
     */
    private List<Long> specialties;
    
    /**
     * List of doctor IDs for filtering
     */
    private List<Long> doctors;
    
    /**
     * Status of consultations for filtering
     */
    private String status;
    
    /**
     * Type of data grouping
     * (can be: "day", "week", "month", "year")
     */
    private String groupBy;
    
    /**
     * Field by which to sort results
     */
    private String orderBy;
    
    /**
     * Sorting direction ("asc" or "desc")
     */
    private String orderDirection;
    
    /**
     * Page number for pagination (starts at 0)
     */
    @Builder.Default
    private Integer page = 0;
    
    /**
     * Page size for pagination
     */
    @Builder.Default
    private Integer size = 20;
    
    /**
     * Flag to include additional data in the response
     */
    private Boolean includeAdditionalData;
    
    /**
     * Alias for getSortBy() to maintain compatibility with the previous Spanish version
     * @return the orderBy field value
     */
    public String getSortBy() {
        return this.orderBy;
    }
    
    /**
     * Alias for getSortDirection() to maintain compatibility with the previous Spanish version
     * @return the orderDirection field value
     */
    public String getSortDirection() {
        return this.orderDirection;
    }
}