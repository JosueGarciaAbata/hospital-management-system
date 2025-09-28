package consulting_service.dtos;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;

/**
 * DTO para solicitar reportes mensuales
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MonthlyReportRequestDTO {
    
    /**
     * Fecha de inicio del rango de consulta
     */
    private LocalDate fechaInicio;
    
    /**
     * Fecha de fin del rango de consulta
     */
    private LocalDate fechaFin;
    
    /**
     * Lista de IDs de centros médicos para filtrar
     */
    private List<Long> centrosMedicos;
    
    /**
     * Lista de IDs de especialidades para filtrar
     */
    private List<Long> especialidades;
    
    /**
     * Lista de IDs de médicos para filtrar
     */
    private List<Long> medicos;
    
    // Campo estado eliminado
    
    /**
     * Campo por el cual ordenar
     */
    private String ordenarPor;
    
    /**
     * Dirección del ordenamiento (ASC, DESC)
     */
    private String direccionOrden;
    
    /**
     * Número de página
     */
    private Integer pagina = 0;
    
    /**
     * Tamaño de página
     */
    private Integer tamanio = 20;
}