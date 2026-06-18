package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO para información de paginación en los reportes
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaginationInfoDTO {

    private Integer currentPage;
    private Integer totalPages;
    private Long totalElements;
    private Integer pageSize;
    private Boolean hasNext;
    private Boolean hasPrevious;
}
