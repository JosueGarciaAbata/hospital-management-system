package com.drtx.jdit.reportservice.repo;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;

import java.util.List;

/**
 * Interfaz para acceder a los datos de consultas
 */
public interface ConsultaRepository {
    
    /**
     * Obtiene todas las consultas médicas agrupadas por especialidad
     * @param token Token de autorización para acceder al servicio de consultas
     * @return Lista de consultas por especialidad
     */
    List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad(String token);
    
    /**
     * Obtiene todas las consultas médicas agrupadas por médico
     * @param token Token de autorización para acceder al servicio de consultas
     * @return Lista de consultas por médico
     */
    List<ConsultaMedicoDTO> getConsultasPorMedico(String token);
    
    /**
     * Obtiene todas las consultas médicas agrupadas por centro médico
     * @param token Token de autorización para acceder al servicio de consultas
     * @return Lista de consultas por centro médico
     */
    List<ConsultaCentroMedicoDTO> getConsultasPorCentro(String token);
    
    /**
     * Obtiene todas las consultas médicas agrupadas por mes
     * @param token Token de autorización para acceder al servicio de consultas
     * @return Lista de consultas mensuales
     */
    List<ConsultaMensualDTO> getConsultasMensuales(String token);
}