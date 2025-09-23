package com.drtx.jdit.reportservice.repo;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import com.drtx.jdit.reportservice.external.feign.ConsultingServiceClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * Implementación del repositorio de consultas que utiliza FeignClient
 */
@Repository
@RequiredArgsConstructor
public class ConsultaRepositoryImpl implements ConsultaRepository {

    private final ConsultingServiceClient consultingServiceClient;

    @Override
    public List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad(String token) {
        return consultingServiceClient.getConsultasByEspecialidad(token);
    }

    @Override
    public List<ConsultaMedicoDTO> getConsultasPorMedico(String token) {
        return consultingServiceClient.getConsultasByMedico(token);
    }

    @Override
    public List<ConsultaCentroMedicoDTO> getConsultasPorCentro(String token) {
        return consultingServiceClient.getConsultasByCentro(token);
    }

    @Override
    public List<ConsultaMensualDTO> getConsultasMensuales(String token) {
        return consultingServiceClient.getConsultasByMes(token);
    }
}