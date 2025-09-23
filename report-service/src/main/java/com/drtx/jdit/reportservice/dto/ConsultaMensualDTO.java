package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaMensualDTO {
    private int mes;
    private int anio;
    private int totalConsultas;
    private List<ResumenEspecialidad> especialidades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ResumenEspecialidad {
        private String nombreEspecialidad;
        private int cantidadConsultas;
    }
}
