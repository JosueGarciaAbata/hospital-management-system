package com.drtx.jdit.reportservice.repo;

import com.drtx.jdit.reportservice.dto.*;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

/**
 * Implementación del repositorio de consultas que utiliza FeignClient
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class ConsultaRepositoryImpl implements ConsultaRepository {

    private final ConsultingServiceClient consultingServiceClient;

    @Override
    public SpecialtyReportResponseDTO getConsultasPorEspecialidad(String token, SpecialtyReportRequestDTO request) {
        // log.info("Obteniendo consultas por especialidad con filtros: {}", request);
        try {
            return consultingServiceClient.getConsultationsBySpecialty(
                token, 
                ConsultingServiceClient.DEFAULT_ROLE, 
                request
            );
        } catch (Exception e) {
            // log.error("Error al obtener consultas por especialidad: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener consultas por especialidad", e);
        }
    }

    @Override
    public DoctorReportResponseDTO getConsultasPorMedico(String token, DoctorReportRequestDTO request) {
        // log.info("Obteniendo consultas por médico con filtros: {}", request);
        try {
            return consultingServiceClient.getConsultationsByDoctor(
                token, 
                ConsultingServiceClient.DEFAULT_ROLE, 
                request
            );
        } catch (Exception e) {
            // log.error("Error al obtener consultas por médico: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener consultas por médico", e);
        }
    }

    @Override
    public MedicalCenterReportResponseDTO getConsultasPorCentro(String token, MedicalCenterReportRequestDTO request) {
        // log.info("Obteniendo consultas por centro médico con filtros: {}", request);
        try {
            return consultingServiceClient.getConsultationsByCenter(
                token, 
                ConsultingServiceClient.DEFAULT_ROLE, 
                request
            );
        } catch (Exception e) {
            // log.error("Error al obtener consultas por centro médico: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener consultas por centro médico", e);
        }
    }

    @Override
    public MonthlyReportResponseDTO getConsultasMensuales(String token, MonthlyReportRequestDTO request) {
        // log.info("Obteniendo consultas mensuales con filtros: {}", request);
        try {
            return consultingServiceClient.getConsultationsByMonth(
                token, 
                ConsultingServiceClient.DEFAULT_ROLE, 
                request
            );
        } catch (Exception e) {
            // log.error("Error al obtener consultas mensuales: {}", e.getMessage(), e);
            throw new RuntimeException("Error al obtener consultas mensuales", e);
        }
    }

}