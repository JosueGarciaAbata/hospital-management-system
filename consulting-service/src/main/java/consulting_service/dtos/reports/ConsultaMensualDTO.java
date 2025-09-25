package consulting_service.dtos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por mes")
public class ConsultaMensualDTO {

    @Schema(description = "Mes numérico de las consultas (1=enero, 12=diciembre)", example = "9")
    private int mes;

    @Schema(description = "Año de las consultas", example = "2025")
    private int anio;

    @Schema(description = "Total de consultas realizadas en el mes", example = "120")
    private int totalConsultas;

    @Schema(description = "Lista de especialidades con su total de consultas en el mes")
    private List<ResumenEspecialidad> especialidades;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Resumen de consultas por especialidad en un mes")
    public static class ResumenEspecialidad {

        @Schema(description = "Nombre de la especialidad", example = "Ginecología")
        private String nombreEspecialidad;

        @Schema(description = "Cantidad de consultas en la especialidad", example = "35")
        private int cantidadConsultas;
    }
}
