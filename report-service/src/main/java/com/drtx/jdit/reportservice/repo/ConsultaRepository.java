package com.drtx.jdit.reportservice.repo;

import com.drtx.jdit.reportservice.dto.*;

/**
 * Interfaz para acceder a los datos de consultas
 */
public interface ConsultaRepository {

    /**
     * Obtiene las consultas médicas agrupadas por especialidad
     */
    SpecialtyReportResponseDTO getConsultasPorEspecialidad(String token, SpecialtyReportRequestDTO request);

    /**
     * Obtiene las consultas médicas agrupadas por médico
     */
    DoctorReportResponseDTO getConsultasPorMedico(String token, DoctorReportRequestDTO request);

    /**
     * Obtiene las consultas médicas agrupadas por centro médico
     */
    MedicalCenterReportResponseDTO getConsultasPorCentro(String token, MedicalCenterReportRequestDTO request);

    /**
     * Obtiene las consultas médicas agrupadas por mes
     */
    MonthlyReportResponseDTO getConsultasMensuales(String token, MonthlyReportRequestDTO request);

    // ========== Métodos de compatibilidad (deprecated) ==========

    /**
     * @deprecated Usar getConsultasPorEspecialidad con request DTO
     */
    @Deprecated
    default java.util.List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad(String token) {
        return java.util.Collections.emptyList();
    }

    /**
     * @deprecated Usar getConsultasPorMedico con request DTO
     */
    @Deprecated
    default java.util.List<ConsultaMedicoDTO> getConsultasPorMedico(String token) {
        return java.util.Collections.emptyList();
    }

    /**
     * @deprecated Usar getConsultasPorCentro con request DTO
     */
    @Deprecated
    default java.util.List<ConsultaCentroMedicoDTO> getConsultasPorCentro(String token) {
        return java.util.Collections.emptyList();
    }

    /**
     * @deprecated Usar getConsultasMensuales con request DTO
     */
    @Deprecated
    default java.util.List<ConsultaMensualDTO> getConsultasMensuales(String token) {
        return java.util.Collections.emptyList();
    }
}