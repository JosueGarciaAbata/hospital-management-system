package consulting_service.services.reports;

import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.feign.admin_service.dtos.DoctorRead;
import consulting_service.feign.admin_service.dtos.MedicalCenterRead;
import consulting_service.feign.admin_service.services.DoctorServiceClient;
import consulting_service.feign.admin_service.services.MedicalCenterServiceClient;
import consulting_service.feign.auth_service.services.UserServiceClient;
import consulting_service.repositories.PatientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * Servicio para obtener datos complementarios para los reportes
 * Centraliza el acceso a datos de doctores, centros médicos y pacientes
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ReportEnrichmentService {

    private final DoctorServiceClient doctorServiceClient;
    private final MedicalCenterServiceClient centerServiceClient;
    private final UserServiceClient userServiceClient;
    private final PatientRepository patientRepository;

    /**
     * Obtiene información del doctor por ID
     */
    public DoctorRead getDoctorInfo(Long doctorId) {
        try {
            return doctorServiceClient.getOne(doctorId, false, "ADMIN").getBody();
        } catch (Exception e) {
            log.warn("Error al obtener información del doctor {}: {}", doctorId, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene información del centro médico por ID
     */
    public MedicalCenterRead getCenterInfo(Long centerId) {
        try {
            return centerServiceClient.getOne(centerId, false, "ADMIN").getBody();
        } catch (Exception e) {
            log.warn("Error al obtener información del centro médico {}: {}", centerId, e.getMessage());
            return null;
        }
    }

    /**
     * Obtiene el nombre del paciente
     */
    public String getPatientName(Long patientId) {
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
     * Obtiene la especialidad del doctor
     */
    public String getDoctorSpecialty(Long doctorId) {
        DoctorRead doctor = getDoctorInfo(doctorId);
        return doctor != null && doctor.specialtyName() != null ?
            doctor.specialtyName() : "Sin especialidad";
    }

    /**
     * Formatea el nombre del doctor para presentación
     */
    public String formatDoctorName(DoctorRead doctor, Long doctorId) {
        // Si tenemos el doctor y su userId, podemos intentar obtener más información
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
                log.warn("Error al obtener nombre completo del doctor {}: {}", doctorId, e.getMessage());
            }
        }

        // Si no podemos obtener el nombre del usuario, devolver formato genérico
        return "Dr. ID:" + doctorId;
    }
}
