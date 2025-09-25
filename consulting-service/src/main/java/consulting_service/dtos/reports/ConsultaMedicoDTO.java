package consulting_service.dtos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por médico")
public class ConsultaMedicoDTO {

    @Schema(description = "Identificador del médico", example = "301")
    private Long id;

    @Schema(description = "Nombre completo del médico", example = "Dr. Carlos Pérez")
    private String nombreMedico;

    @Schema(description = "Especialidad del médico", example = "Dermatología")
    private String especialidad;

    @Schema(description = "Lista de consultas realizadas por el médico")
    private List<ConsultaDetalle> consultas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de una consulta en el reporte por médico")
    public static class ConsultaDetalle {

        @Schema(description = "Identificador de la consulta", example = "3005")
        private Long consultaId;

        @Schema(description = "Nombre completo del paciente", example = "Andrea Torres")
        private String nombrePaciente;

        @Schema(description = "Fecha y hora de la consulta", example = "2025-09-21T09:15:00")
        private LocalDateTime fechaConsulta;

        @Schema(description = "Estado de la consulta", example = "EN_PROCESO")
        private String estado;
    }
}
