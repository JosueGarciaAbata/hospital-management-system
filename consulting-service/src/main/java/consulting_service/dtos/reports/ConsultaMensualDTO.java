package consulting_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * DTO para reportes de consultas mensuales
 */
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