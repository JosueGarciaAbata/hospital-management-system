package com.drtx.jdit.reportservice.dto.response;

import com.drtx.jdit.reportservice.dto.request.ReportFilterRequestDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Class to encapsulate metadata for report responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportMetadataDTO {
    
    /**
     * Filters applied in the query
     */
    private ReportFilterRequestDTO appliedFilters;
    
    /**
     * Total number of records found
     */
    private long totalRecords;
    
    /**
     * Total number of pages (if pagination applies)
     */
    private int totalPages;
    
    /**
     * Current page (if pagination applies)
     */
    private int currentPage;
    
    /**
     * Page size (if pagination applies)
     */
    private int pageSize;
    
    /**
     * Date and time of report generation
     */
    private LocalDateTime generationDate;
    
    /**
     * Execution time in milliseconds
     */
    private long executionTime;
    
    /**
     * Report name
     */
    private String reportName;
    
    /**
     * Report description
     */
    private String reportDescription;
}