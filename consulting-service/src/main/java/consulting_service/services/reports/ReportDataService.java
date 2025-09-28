package consulting_service.services.reports;

import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.dtos.response.reports.DetailedConsultationDTO;
import consulting_service.dtos.response.reports.PaginationInfoDTO;
import consulting_service.dtos.response.reports.ReportKpisDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.feign.admin_service.dtos.DoctorRead;
import consulting_service.feign.admin_service.dtos.MedicalCenterRead;
import consulting_service.feign.admin_service.services.DoctorServiceClient;
import consulting_service.feign.admin_service.services.MedicalCenterServiceClient;
import consulting_service.feign.auth_service.services.UserServiceClient;
import consulting_service.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio encargado de obtener y preparar datos para los reportes
 * Centraliza acceso a datos de doctores, centros y pacientes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportDataService {

    private final DoctorServiceClient doctorServiceClient;
    private final MedicalCenterServiceClient centerServiceClient;
    private final UserServiceClient userServiceClient;
    private final PatientRepository patientRepository;

    /**
     * Construye el detalle de KPIs para los reportes
     */
    public ReportKpisDTO buildKpis(List<MedicalConsultation> consultations) {
        if (consultations.isEmpty()) {
            return buildEmptyKpis();
        }

        long uniqueDoctors = consultations.stream().map(MedicalConsultation::getDoctorId).distinct().count();
        long uniquePatients = consultations.stream().map(MedicalConsultation::getPatientId).distinct().count();
        long uniqueCenters = consultations.stream().map(MedicalConsultation::getCenterId).distinct().count();

        long withDiagnosis = consultations.stream()
                .filter(cons -> cons.getDiagnosis() != null && !cons.getDiagnosis().trim().isEmpty())
                .count();
        long withTreatment = consultations.stream()
                .filter(cons -> cons.getTreatment() != null && !cons.getTreatment().trim().isEmpty())
                .count();

        double dataCompleteness = consultations.size() > 0 ?
                Math.round(((withDiagnosis + withTreatment) / (double) (2 * consultations.size())) * 10000.0) / 100.0 : 0.0;

        return ReportKpisDTO.builder()
                .distinctSpecialties((long) consultations.stream()
                        .map(cons -> getDoctorSpecialty(cons.getDoctorId()))
                        .distinct()
                        .count())
                .doctorsInvolved(uniqueDoctors)
                .medicalCentersInvolved(uniqueCenters)
                .uniquePatientsTotal(uniquePatients)
                .avgConsultationsPerDoctor(Math.round((consultations.size() / (double) Math.max(uniqueDoctors, 1)) * 100.0) / 100.0)
                .dataQuality(ReportKpisDTO.DataQualityDTO.builder()
                        .consultationsWithDiagnosis(withDiagnosis)
                        .consultationsWithTreatment(withTreatment)
                        .dataCompletenessPercentage(dataCompleteness)
                        .build())
                .build();
    }

    /**
     * Construye lista de consultas detalladas para los reportes
     */
    public List<DetailedConsultationDTO> buildDetailedConsultations(List<MedicalConsultation> consultations, int limit) {
        if (consultations.isEmpty()) {
            return Collections.emptyList();
        }

        return consultations.stream()
                .limit(limit)
                .map(consultation -> {
                    String doctorName = getDoctorName(consultation.getDoctorId());
                    String centerName = getCenterName(consultation.getCenterId());
                    String patientName = getPatientName(consultation.getPatientId());

                    return DetailedConsultationDTO.builder()
                            .consultationId(consultation.getId())
                            .patientName(patientName)
                            .doctorName(doctorName)
                            .specialty(getDoctorSpecialty(consultation.getDoctorId()))
                            .centerName(centerName)
                            .consultationDate(consultation.getDate())
                            .status(Boolean.TRUE.equals(consultation.getDeleted()) ? "CANCELLED" : "ACTIVE")
                            .diagnosis(consultation.getDiagnosis())
                            .treatment(consultation.getTreatment())
                            .notes(consultation.getNotes())
                            .build();
                })
                .collect(Collectors.toList());
    }

    /**
     * Obtiene información del doctor por ID
     */
    public DoctorRead getDoctorInfo(Long doctorId) {
        if (doctorId == null) {
            return null;
        }

        try {
            return doctorServiceClient.getOne(doctorId, false, "ADMIN").getBody();
        } catch (Exception e) {
            log.warn("Error al obtener información del doctor {}: {}", doctorId, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el nombre formateado del doctor
     */
    public String getDoctorName(Long doctorId) {
        if (doctorId == null) {
            return "Doctor desconocido";
        }

        try {
            DoctorRead doctor = getDoctorInfo(doctorId);
            if (doctor != null && doctor.userId() != null) {
                try {
                    DoctorReadDTO userDoctor = userServiceClient.getDoctorByUserId(doctor.userId());
                    if (userDoctor != null) {
                        String firstName = userDoctor.getFirstName();
                        String lastName = userDoctor.getLastName();

                        if (firstName != null && !firstName.isEmpty()) {
                            return "Dr. " + firstName + (lastName != null ? " " + lastName : "");
                        }
                    }
                } catch (Exception e) {
                    log.warn("Error al obtener información del usuario para doctor {}: {}", doctorId, e.getMessage());
                }
            }
            return "Dr. ID:" + doctorId;
        } catch (Exception e) {
            log.warn("Error al obtener nombre del doctor {}: {}", doctorId, e.getMessage());
            return "Doctor " + doctorId;
        }
    }

    /**
     * Obtiene la especialidad del doctor
     */
    public String getDoctorSpecialty(Long doctorId) {
        if (doctorId == null) {
            return "Sin especialidad";
        }

        try {
            DoctorRead doctor = getDoctorInfo(doctorId);
            return doctor != null && doctor.specialtyName() != null ?
                doctor.specialtyName() : "Sin especialidad";
        } catch (Exception e) {
            log.warn("Error al obtener especialidad del doctor {}: {}", doctorId, e.getMessage());
            return "Sin especialidad";
        }
    }

    /**
     * Obtiene información del centro médico por ID
     */
    public MedicalCenterRead getCenterInfo(Long centerId) {
        if (centerId == null) {
            return null;
        }

        try {
            return centerServiceClient.getOne(centerId, false, "ADMIN").getBody();
        } catch (Exception e) {
            log.warn("Error al obtener información del centro médico {}: {}", centerId, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el nombre del centro médico
     */
    public String getCenterName(Long centerId) {
        if (centerId == null) {
            return "Centro desconocido";
        }

        try {
            MedicalCenterRead center = getCenterInfo(centerId);
            return center != null && center.name() != null ?
                center.name() : "Centro ID: " + centerId;
        } catch (Exception e) {
            log.warn("Error al obtener nombre del centro médico {}: {}", centerId, e.getMessage());
            return "Centro ID: " + centerId;
        }
    }

    /**
     * Obtiene el nombre del paciente
     */
    public String getPatientName(Long patientId) {
        if (patientId == null) {
            return "Paciente desconocido";
        }

        try {
            return patientRepository.findById(patientId)
                    .map(p -> p.getFirstName() + " " + p.getLastName())
                    .orElse("Paciente ID: " + patientId);
        } catch (Exception e) {
            log.warn("Error al obtener nombre del paciente {}: {}", patientId, e.getMessage());
            return "Paciente ID: " + patientId;
        }
    }

    /**
     * Crea un objeto de KPIs vacío
     */
    public ReportKpisDTO buildEmptyKpis() {
        return ReportKpisDTO.builder()
                .distinctSpecialties(0L)
                .doctorsInvolved(0L)
                .medicalCentersInvolved(0L)
                .uniquePatientsTotal(0L)
                .avgConsultationsPerDoctor(0.0)
                .dataQuality(ReportKpisDTO.DataQualityDTO.builder()
                        .consultationsWithDiagnosis(0L)
                        .consultationsWithTreatment(0L)
                        .dataCompletenessPercentage(0.0)
                        .build())
                .build();
    }

    /**
     * Crea un objeto de paginación vacío
     */
    public PaginationInfoDTO buildEmptyPaginationInfo() {
        return PaginationInfoDTO.builder()
                .currentPage(0)
                .totalPages(0)
                .totalElements(0L)
                .pageSize(0)
                .hasNext(false)
                .hasPrevious(false)
                .build();
    }
}
