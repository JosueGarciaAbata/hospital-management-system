package com.drtx.jdit.reportservice.utils;

import com.drtx.jdit.reportservice.dto.request.ReportFilterRequestDTO;
import com.drtx.jdit.reportservice.dto.response.ReportMetadataDTO;
import com.drtx.jdit.reportservice.dto.ReportResponseDTO;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.function.Function;

/**
 * Utility to simplify query calls and avoid code duplication
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ReportQueryHelper {

    /**
     * Generic method for executing queries and building responses
     * @param <T> Type of data in the response
     * @param filters Filters for the query
     * @param queryFunction Function that makes the call to consulting-service
     * @param reportName Descriptive name of the report
     * @param reportDescription Description of the report
     * @param additionalDataFunction Function to generate additional data (optional)
     * @return Formatted report response
     */
    public <T> ReportResponseDTO<T> executeQuery(
            ReportFilterRequestDTO filters,
            Function<ReportFilterRequestDTO, List<T>> queryFunction,
            String reportName,
            String reportDescription,
            Function<List<T>, Object> additionalDataFunction) {
        
        long startTime = System.currentTimeMillis();
        
        try {
            // Execute the query
            List<T> result = queryFunction.apply(filters);
            // log.info("{} successfully obtained: {} records", reportName, result != null ? result.size() : 0);
            
            // Calculate execution time
            long executionTime = System.currentTimeMillis() - startTime;
            
            // Generate data summary if requested
            Object additionalData = null;
            if (Boolean.TRUE.equals(filters.getIncludeAdditionalData()) && additionalDataFunction != null) {
                additionalData = additionalDataFunction.apply(result);
            }
            
            // Build and return the response
            return ReportResponseDTO.<T>builder()
                .data(result)
                .metadata(ReportMetadataDTO.builder()
                    .appliedFilters(filters)
                    .totalRecords(result != null ? result.size() : 0)
                    .currentPage(filters.getPage())
                    .pageSize(filters.getSize())
                    .totalPages(calculateTotalPages(result != null ? result.size() : 0, filters.getSize()))
                    .generationDate(LocalDateTime.now())
                    .executionTime(executionTime)
                    .reportName(reportName)
                    .reportDescription(reportDescription)
                    .build())
                .additionalData(additionalData)
                .build();
        } catch (Exception e) {
            // log.error("Error obtaining {} from consulting-service: {}", reportName.toLowerCase(), e.getMessage(), e);
            throw e;
        }
    }
    
    /**
     * Calculates the total number of pages for pagination
     */
    private int calculateTotalPages(long totalItems, int pageSize) {
        if (pageSize <= 0) {
            return 0;
        }
        return (int) Math.ceil((double) totalItems / (double) pageSize);
    }
}