package consulting_service.dtos.reports;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Reporte de consultas agrupadas por especialidad")
public class ConsultaEspecialidadDTO {

    @Schema(description = "Identificador de la consulta", example = "2001")
    private Long id;

    @Schema(description = "Especialidad médica", example = "Pediatría")
    private String especialidad;

    @Schema(description = "Nombre completo del médico", example = "Dr. José Martínez")
    private String nombreMedico;

    @Schema(description = "Nombre completo del paciente", example = "Lucía Ramírez")
    private String nombrePaciente;

    @Schema(description = "Fecha y hora de la consulta", example = "2025-09-23T14:00:00")
    private LocalDateTime fechaConsulta;

    @Schema(description = "Estado de la consulta", example = "PROGRAMADA")
    private String estado;
}
