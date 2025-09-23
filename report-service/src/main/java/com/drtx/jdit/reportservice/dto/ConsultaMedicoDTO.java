package com.drtx.jdit.reportservice.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaMedicoDTO {
    private Long id;
    private String nombreMedico;
    private String especialidad;
    private List<ConsultaDetalle> consultas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultaDetalle {
        private Long consultaId;
        private String nombrePaciente;
        private LocalDateTime fechaConsulta;
        private String estado;
    }
}
