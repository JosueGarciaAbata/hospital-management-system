package com.drtx.jdit.reportservice.external.feign;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

/**
 * Cliente Feign para comunicarse con el servicio de consultas médicas
 */
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