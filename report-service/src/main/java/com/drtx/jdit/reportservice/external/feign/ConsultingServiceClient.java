package com.drtx.jdit.reportservice.external.feign;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "consulting-service")
public interface ConsultingServiceClient {

    // Rol por defecto quemado (igual que tu compañero)
    String DEFAULT_ROLE = "ADMIN";

    @GetMapping("/api/consulting/reports/by-specialty")
    List<ConsultaEspecialidadDTO> getConsultasByEspecialidad(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles
    );

    @GetMapping("/api/consulting/reports/by-doctor")
    List<ConsultaMedicoDTO> getConsultasByMedico(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles
    );

    @GetMapping("/api/consulting/reports/by-center")
    List<ConsultaCentroMedicoDTO> getConsultasByCentro(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles
    );

    @GetMapping("/api/consulting/reports/by-month")
    List<ConsultaMensualDTO> getConsultasByMes(
            @RequestHeader("Authorization") String token,
            @RequestHeader("X-Roles") String roles
    );
}