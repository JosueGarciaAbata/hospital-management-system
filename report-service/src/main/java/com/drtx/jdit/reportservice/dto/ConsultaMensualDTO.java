package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaMensualDTO {
    private String mes;
    private Long cantidadConsultas;
}
