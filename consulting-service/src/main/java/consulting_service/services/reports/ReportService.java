package consulting_service.services.reports;

import consulting_service.dtos.reports.ConsultaCentroMedicoDTO;
import consulting_service.dtos.reports.ConsultaEspecialidadDTO;
import consulting_service.dtos.reports.ConsultaMedicoDTO;
import consulting_service.dtos.reports.ConsultaMensualDTO;

import java.util.List;

/**
 * Interfaz para el servicio de reportes
 */
public interface ReportService {
    
    /**
     * Obtiene un reporte de consultas agrupadas por especialidad
     */
    List<ConsultaEspecialidadDTO> getConsultationsBySpecialty();
    
    /**
     * Obtiene un reporte de consultas agrupadas por médico
     */
    List<ConsultaMedicoDTO> getConsultationsByDoctor();
    
    /**
     * Obtiene un reporte de consultas agrupadas por centro médico
     */
    List<ConsultaCentroMedicoDTO> getConsultationsByCenter();
    
    /**
     * Obtiene un reporte de consultas agrupadas por mes
     */
    List<ConsultaMensualDTO> getConsultationsByMonth();
}