package com.drtx.jdit.reportservice.dto;

import com.drtx.jdit.reportservice.dto.response.ReportMetadataDTO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Generic DTO to encapsulate report responses
 * @param <T> Type of data contained in the results list
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReportResponseDTO<T> {
    
    private String reportName;
    private String generatedAt;
    private List<T> data;
    private int totalElements;
    private String message;
    private ReportMetadataDTO metadata;
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