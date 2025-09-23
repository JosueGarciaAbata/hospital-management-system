package consulting_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * DTO para reportes de consultas por especialidad
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaEspecialidadDTO {
    private Long id;
    private String especialidad;
    private String nombreMedico;
    private String nombrePaciente;
    private LocalDateTime fechaConsulta;
    private String estado;
}