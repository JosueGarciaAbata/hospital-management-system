package com.drtx.jdit.reportservice.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic wrapper for report responses
 * @param <T> The type of report data
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO<T> {
    
    /**
     * Report metadata
     */
    private ReportMetadataDTO metadata;
    
    /**
     * Report data
     */
    private List<T> data;
    
    /**
     * Additional report data, for example, totals or summaries
     */
    private Object additionalData;
    
    /**
     * Alias for getData() to maintain compatibility with the previous Spanish version
     * @return the data field value
     */
    public List<T> getDatos() {
        return this.data;
    }
    
    /**
     * Alias for getAdditionalData() to maintain compatibility with the previous Spanish version
     * @return the additionalData field value
     */
    public Object getDatosAdicionales() {
        return this.additionalData;
    }
}