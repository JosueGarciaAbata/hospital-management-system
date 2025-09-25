package consulting_service.dtos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por centro médico")
public class ConsultaCentroMedicoDTO {

    @Schema(description = "Identificador único del centro médico", example = "5")
    private Long id;

    @Schema(description = "Nombre del centro médico", example = "Clínica Central")
    private String nombreCentro;

    @Schema(description = "Dirección del centro médico", example = "Av. Amazonas N34-125")
    private String direccion;

    @Schema(description = "Lista de consultas realizadas en este centro médico")
    private List<ConsultaDetalle> consultas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    @Schema(description = "Detalle de una consulta en el reporte por centro")
    public static class ConsultaDetalle {

        @Schema(description = "Identificador de la consulta", example = "1001")
        private Long consultaId;

        @Schema(description = "Nombre completo del médico", example = "Dra. María López")
        private String nombreMedico;

        @Schema(description = "Especialidad del médico", example = "Cardiología")
        private String especialidad;

        @Schema(description = "Nombre del paciente", example = "Juan Pérez")
        private String nombrePaciente;

        @Schema(description = "Fecha y hora de la consulta", example = "2025-09-24T10:30:00")
        private LocalDateTime fechaConsulta;

        @Schema(description = "Estado de la consulta", example = "FINALIZADA")
        private String estado;
    }
}
