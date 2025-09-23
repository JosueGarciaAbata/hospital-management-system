package com.drtx.jdit.reportservice.service;

import com.drtx.jdit.reportservice.dto.ConsultaEspecialidadDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaCentroMedicoDTO;
import com.drtx.jdit.reportservice.dto.ConsultaMensualDTO;

import java.util.List;

public interface ReportService {
    List<ConsultaEspecialidadDTO> getConsultasPorEspecialidad();
    List<ConsultaMedicoDTO> getConsultasPorMedico();
    List<ConsultaCentroMedicoDTO> getConsultasPorCentro();
    List<ConsultaMensualDTO> getConsultasMensuales();
}
