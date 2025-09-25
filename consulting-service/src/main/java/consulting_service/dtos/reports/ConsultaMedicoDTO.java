package consulting_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para reportes de consultas por médico
 */
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