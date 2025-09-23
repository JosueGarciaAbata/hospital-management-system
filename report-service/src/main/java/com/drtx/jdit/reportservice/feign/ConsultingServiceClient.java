package com.drtx.jdit.reportservice.feign;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(name = "consulting-service")
public interface ConsultingServiceClient {

    @GetMapping("/api/consultas/especialidad")
    List<ConsultaEspecialidadDTO> getConsultasByEspecialidad(@RequestHeader("Authorization") String token);

    @GetMapping("/api/consultas/medico")
    List<ConsultaMedicoDTO> getConsultasByMedico(@RequestHeader("Authorization") String token);

    @GetMapping("/api/consultas/centro")
    List<ConsultaCentroMedicoDTO> getConsultasByCentro(@RequestHeader("Authorization") String token);

    @GetMapping("/api/consultas/mensuales")
    List<ConsultaMensualDTO> getConsultasByMes(@RequestHeader("Authorization") String token);
}
