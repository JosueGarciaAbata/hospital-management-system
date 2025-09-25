package consulting_service.dtos.reports;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para reportes de consultas por centro médico
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ConsultaCentroMedicoDTO {
    private Long id;
    private String nombreCentro;
    private String direccion;
    private List<ConsultaDetalle> consultas;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ConsultaDetalle {
        private Long consultaId;
        private String nombreMedico;
        private String especialidad;
        private String nombrePaciente;
        private LocalDateTime fechaConsulta;
        private String estado;
    }
}