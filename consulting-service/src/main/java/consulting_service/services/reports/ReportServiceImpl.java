package consulting_service.services.reports;

import consulting_service.dtos.reports.ConsultaCentroMedicoDTO;
import consulting_service.dtos.reports.ConsultaEspecialidadDTO;
import consulting_service.dtos.reports.ConsultaMedicoDTO;
import consulting_service.dtos.reports.ConsultaMensualDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.repositories.MedicalConsultationsRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de reportes
 */
@Service
public class ReportServiceImpl implements ReportService {

    private final MedicalConsultationsRepository consultationsRepository;

    public ReportServiceImpl(MedicalConsultationsRepository consultationsRepository) {
        this.consultationsRepository = consultationsRepository;
    }

    @Override
    public List<ConsultaEspecialidadDTO> getConsultationsBySpecialty() {
        try {
            List<MedicalConsultation> consultations = consultationsRepository.findAll();
            
            return consultations.stream()
                    .filter(consultation -> consultation != null) // Filtrar consultas nulas
                    .map(consultation -> {
                        try {
                            // Crear el DTO básico con los datos disponibles
                            ConsultaEspecialidadDTO dto = new ConsultaEspecialidadDTO();
                            dto.setId(consultation.getId());
                            
                            // Simulamos datos para demostración - en producción deberías obtener estos datos
                            Long doctorId = consultation.getDoctorId() != null ? consultation.getDoctorId() : 0L;
                            Long patientId = consultation.getPatientId() != null ? consultation.getPatientId() : 0L;
                            
                            dto.setEspecialidad("Especialidad " + doctorId);
                            dto.setNombreMedico("Doctor ID: " + doctorId);
                            dto.setNombrePaciente("Paciente ID: " + patientId);
                            dto.setFechaConsulta(consultation.getDate());
                            dto.setEstado(consultation.getDeleted() != null && consultation.getDeleted() ? "CANCELADA" : "ACTIVA");
                            return dto;
                        } catch (Exception e) {
                            // Log error pero continuar con otras consultas
                            System.err.println("Error procesando consulta ID: " + 
                                (consultation.getId() != null ? consultation.getId() : "desconocido") +
                                " - " + e.getMessage());
                            return null;
                        }
                    })
                    .filter(dto -> dto != null) // Filtrar DTOs nulos
                    .collect(Collectors.toList());
        } catch (Exception e) {
            System.err.println("Error obteniendo consultas por especialidad: " + e.getMessage());
            e.printStackTrace();
            // Retornar lista vacía en caso de error
            return Collections.emptyList();
        }
    }

    @Override
    public List<ConsultaMedicoDTO> getConsultationsByDoctor() {
        List<MedicalConsultation> consultations = consultationsRepository.findAll();
        
        // Agrupar consultas por médico
        Map<Long, ConsultaMedicoDTO> doctorMap = new HashMap<>();
        
        consultations.forEach(consultation -> {
            Long doctorId = consultation.getDoctorId();
            
            if (!doctorMap.containsKey(doctorId)) {
                ConsultaMedicoDTO dto = new ConsultaMedicoDTO();
                dto.setId(doctorId);
                dto.setNombreMedico("Doctor ID: " + doctorId);
                dto.setEspecialidad("Especialidad del doctor " + doctorId);
                dto.setConsultas(new ArrayList<>());
                doctorMap.put(doctorId, dto);
            }
            
            ConsultaMedicoDTO.ConsultaDetalle detalle = new ConsultaMedicoDTO.ConsultaDetalle();
            detalle.setConsultaId(consultation.getId());
            detalle.setNombrePaciente("Paciente ID: " + consultation.getPatientId());
            detalle.setFechaConsulta(consultation.getDate());
            detalle.setEstado(consultation.getDeleted() ? "CANCELADA" : "ACTIVA");
            
            doctorMap.get(doctorId).getConsultas().add(detalle);
        });
        
        return new ArrayList<>(doctorMap.values());
    }

    @Override
    public List<ConsultaCentroMedicoDTO> getConsultationsByCenter() {
        List<MedicalConsultation> consultations = consultationsRepository.findAll();
        
        // Agrupar consultas por centro médico
        Map<Long, ConsultaCentroMedicoDTO> centerMap = new HashMap<>();
        
        consultations.forEach(consultation -> {
            Long centerId = consultation.getCenterId();
            
            if (!centerMap.containsKey(centerId)) {
                ConsultaCentroMedicoDTO dto = new ConsultaCentroMedicoDTO();
                dto.setId(centerId);
                dto.setNombreCentro("Centro Médico " + centerId);
                dto.setDireccion("Dirección del centro " + centerId);
                dto.setConsultas(new ArrayList<>());
                centerMap.put(centerId, dto);
            }
            
            ConsultaCentroMedicoDTO.ConsultaDetalle detalle = new ConsultaCentroMedicoDTO.ConsultaDetalle();
            detalle.setConsultaId(consultation.getId());
            detalle.setNombreMedico("Doctor ID: " + consultation.getDoctorId());
            detalle.setEspecialidad("Especialidad del doctor " + consultation.getDoctorId());
            detalle.setNombrePaciente("Paciente ID: " + consultation.getPatientId());
            detalle.setFechaConsulta(consultation.getDate());
            detalle.setEstado(consultation.getDeleted() ? "CANCELADA" : "ACTIVA");
            
            centerMap.get(centerId).getConsultas().add(detalle);
        });
        
        return new ArrayList<>(centerMap.values());
    }

    @Override
    public List<ConsultaMensualDTO> getConsultationsByMonth() {
        List<MedicalConsultation> consultations = consultationsRepository.findAll();
        
        // Agrupar consultas por mes y año
        Map<String, ConsultaMensualDTO> monthMap = new HashMap<>();
        
        consultations.forEach(consultation -> {
            LocalDateTime date = consultation.getDate();
            int month = date.getMonthValue();
            int year = date.getYear();
            String key = year + "-" + month;
            
            if (!monthMap.containsKey(key)) {
                ConsultaMensualDTO dto = new ConsultaMensualDTO();
                dto.setMes(month);
                dto.setAnio(year);
                dto.setTotalConsultas(0);
                dto.setEspecialidades(new ArrayList<>());
                monthMap.put(key, dto);
            }
            
            // Incrementar contador de consultas
            ConsultaMensualDTO monthDto = monthMap.get(key);
            monthDto.setTotalConsultas(monthDto.getTotalConsultas() + 1);
            
            // Actualizar contador por especialidad (simulado)
            String specialty = "Especialidad " + consultation.getDoctorId();
            boolean found = false;
            
            for (ConsultaMensualDTO.ResumenEspecialidad esp : monthDto.getEspecialidades()) {
                if (esp.getNombreEspecialidad().equals(specialty)) {
                    esp.setCantidadConsultas(esp.getCantidadConsultas() + 1);
                    found = true;
                    break;
                }
            }
            
            if (!found) {
                ConsultaMensualDTO.ResumenEspecialidad resumen = new ConsultaMensualDTO.ResumenEspecialidad();
                resumen.setNombreEspecialidad(specialty);
                resumen.setCantidadConsultas(1);
                monthDto.getEspecialidades().add(resumen);
            }
        });
        
        return new ArrayList<>(monthMap.values());
    }
}