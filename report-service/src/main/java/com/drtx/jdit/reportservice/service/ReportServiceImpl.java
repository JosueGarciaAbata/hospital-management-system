package com.drtx.jdit.reportservice.service;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementación del servicio de reportes
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class ReportServiceImpl implements ReportService {

    private final ConsultingServiceClient consultingServiceClient;

    @Override
    @CircuitBreaker(name = "consultingService", fallbackMethod = "fallbackGetConsultasPorEspecialidad")
    public List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad() {
        String token = extractAuthToken();
        return consultingServiceClient.getConsultasByEspecialidad(token);
    }

    @Override
    @CircuitBreaker(name = "consultingService", fallbackMethod = "fallbackGetConsultasPorMedico")
    public List<ConsultaMedicoDTO> getConsultasPorMedico() {
        String token = extractAuthToken();
        return consultingServiceClient.getConsultasByMedico(token);
    }

    @Override
    @CircuitBreaker(name = "consultingService", fallbackMethod = "fallbackGetConsultasPorCentro")
    public List<ConsultaCentroMedicoDTO> getConsultasPorCentro() {
        String token = extractAuthToken();
        return consultingServiceClient.getConsultasByCentro(token);
    }

    @Override
    @CircuitBreaker(name = "consultingService", fallbackMethod = "fallbackGetConsultasMensuales")
    public List<ConsultaMensualDTO> getConsultasMensuales() {
        String token = extractAuthToken();
        return consultingServiceClient.getConsultasByMes(token);
    }

    /**
     * Extrae el token de autorización de la solicitud HTTP actual
     */
    private String extractAuthToken() {
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        return request.getHeader("Authorization");
    }

    /**
     * Métodos de fallback para el circuit breaker
     */
    public List<ConsultaEspecialidadDTO> fallbackGetConsultasPorEspecialidad(Exception e) {
        log.error("Fallback: Error al obtener consultas por especialidad: {}", e.getMessage());
        return new ArrayList<>();
    }

    public List<ConsultaMedicoDTO> fallbackGetConsultasPorMedico(Exception e) {
        log.error("Fallback: Error al obtener consultas por médico: {}", e.getMessage());
        return new ArrayList<>();
    }

    public List<ConsultaCentroMedicoDTO> fallbackGetConsultasPorCentro(Exception e) {
        log.error("Fallback: Error al obtener consultas por centro médico: {}", e.getMessage());
        return new ArrayList<>();
    }

    public List<ConsultaMensualDTO> fallbackGetConsultasMensuales(Exception e) {
        log.error("Fallback: Error al obtener consultas mensuales: {}", e.getMessage());
        return new ArrayList<>();
    }
}