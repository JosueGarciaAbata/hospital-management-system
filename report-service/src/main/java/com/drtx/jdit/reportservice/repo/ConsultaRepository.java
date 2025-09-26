package com.drtx.jdit.reportservice.repo;

import com.drtx.jdit.reportservice.dto.*;

import java.util.List;
import java.util.Map;

/**
 * Interfaz para acceder a los datos de consultas
 */
public interface ConsultaRepository {
    
    /**
     * Obtiene las consultas médicas agrupadas por especialidad
     * @param token Token de autorización para acceder al servicio de consultas
     * @param request Parámetros de filtrado para la consulta
     * @return Lista de consultas por especialidad
     */
    List<Map<String, Object>> getConsultasPorEspecialidad(String token, SpecialtyReportRequestDTO request);
    
    /**
     * Obtiene las consultas médicas agrupadas por médico
     * @param token Token de autorización para acceder al servicio de consultas
     * @param request Parámetros de filtrado para la consulta
     * @return Lista de consultas por médico
     */
    List<Map<String, Object>> getConsultasPorMedico(String token, DoctorReportRequestDTO request);
    
    /**
     * Obtiene las consultas médicas agrupadas por centro médico
     * @param token Token de autorización para acceder al servicio de consultas
     * @param request Parámetros de filtrado para la consulta
     * @return Lista de consultas por centro médico
     */
    List<Map<String, Object>> getConsultasPorCentro(String token, MedicalCenterReportRequestDTO request);
    
    /**
     * Obtiene las consultas médicas agrupadas por mes
     * @param token Token de autorización para acceder al servicio de consultas
     * @param request Parámetros de filtrado para la consulta
     * @return Lista de consultas mensuales
     */
    List<Map<String, Object>> getConsultasMensuales(String token, MonthlyReportRequestDTO request);
    
    // ========== Métodos de compatibilidad (deprecated) ==========
    
    /**
     * @deprecated Usar getConsultasPorEspecialidad con request DTO
     */
    @Deprecated
    default List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad(String token) {
        SpecialtyReportRequestDTO defaultRequest = SpecialtyReportRequestDTO.builder().build();
        List<Map<String, Object>> rawData = getConsultasPorEspecialidad(token, defaultRequest);
        return rawData.stream()
            .map(data -> ConsultaEspecialidadDTO.builder()
                .id(convertToLong(data.get("id")))
                .especialidad((String) data.get("especialidad"))
                .nombreMedico((String) data.get("nombreMedico"))
                .nombrePaciente((String) data.get("nombrePaciente"))
                .fechaConsulta((java.time.LocalDateTime) data.get("fechaConsulta"))
                .estado((String) data.get("estado"))
                .build())
            .collect(java.util.stream.Collectors.toList());
    }
    
    /**
     * @deprecated Usar getConsultasPorMedico con request DTO
     */
    @Deprecated
    default List<ConsultaMedicoDTO> getConsultasPorMedico(String token) {
        DoctorReportRequestDTO defaultRequest = DoctorReportRequestDTO.builder().build();
        // Implementación básica - retorna lista vacía por compatibilidad
        return java.util.Collections.emptyList();
    }
    
    /**
     * @deprecated Usar getConsultasPorCentro con request DTO
     */
    @Deprecated
    default List<ConsultaCentroMedicoDTO> getConsultasPorCentro(String token) {
        MedicalCenterReportRequestDTO defaultRequest = MedicalCenterReportRequestDTO.builder().build();
        // Implementación básica - retorna lista vacía por compatibilidad
        return java.util.Collections.emptyList();
    }
    
    /**
     * @deprecated Usar getConsultasMensuales con request DTO
     */
    @Deprecated
    default List<ConsultaMensualDTO> getConsultasMensuales(String token) {
        MonthlyReportRequestDTO defaultRequest = MonthlyReportRequestDTO.builder().build();
        // Implementación básica - retorna lista vacía por compatibilidad
        return java.util.Collections.emptyList();
    }
    
    /**
     * Safely converts an Object to Long, handling Integer and Long types
     */
    default Long convertToLong(Object value) {
        if (value == null) {
            return null;
        }
        if (value instanceof Long) {
            return (Long) value;
        }
        if (value instanceof Integer) {
            return ((Integer) value).longValue();
        }
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        // Try to parse as string if it's a string representation
        if (value instanceof String) {
            try {
                return Long.parseLong((String) value);
            } catch (NumberFormatException e) {
                return null;
            }
        }
        return null;
    }
}