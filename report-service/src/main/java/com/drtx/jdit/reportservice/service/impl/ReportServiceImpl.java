package com.drtx.jdit.reportservice.service.impl;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import com.drtx.jdit.reportservice.service.ReportService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportServiceImpl implements ReportService {

    private final ConsultingServiceClient consultingServiceClient;

    @Override
    public List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad() {
        String token = obtenerTokenAutorizacion();
        log.info("ObtenienRdo reporte de consultas por especialidad");
        return consultingServiceClient.getConsultasByEspecialidad(token);
    }

    @Override
    public List<ConsultaMedicoDTO> getConsultasPorMedico() {
        String token = obtenerTokenAutorizacion();
        log.info("Obteniendo reporte de consultas por médico");
        return consultingServiceClient.getConsultasByMedico(token);
    }

    @Override
    public List<ConsultaCentroMedicoDTO> getConsultasPorCentro() {
        String token = obtenerTokenAutorizacion();
        log.info("Obteniendo reporte de consultas por centro médico");
        return consultingServiceClient.getConsultasByCentro(token);
    }

    @Override
    public List<ConsultaMensualDTO> getConsultasMensuales() {
        String token = obtenerTokenAutorizacion();
        log.info("Obteniendo reporte de consultas mensuales");
        return consultingServiceClient.getConsultasByMes(token);
    }

    private String obtenerTokenAutorizacion() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getCredentials() != null) {
            return "Bearer " + authentication.getCredentials().toString();
        }
        log.warn("No se pudo obtener el token de autorización del contexto de seguridad");
        return "";
    }
}
