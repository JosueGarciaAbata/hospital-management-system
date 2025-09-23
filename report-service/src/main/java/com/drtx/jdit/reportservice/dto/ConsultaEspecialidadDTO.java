package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaEspecialidadDTO {
    private String especialidad;
    private Long cantidadConsultas;
}
