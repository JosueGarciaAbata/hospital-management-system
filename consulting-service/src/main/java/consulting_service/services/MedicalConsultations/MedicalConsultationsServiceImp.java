package consulting_service.services.MedicalConsultations;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.dtos.response.MedicalConsultations.MedicalCenterReadDTO;
import consulting_service.exceptions.NotFoundException;
import consulting_service.feign.admin_service.services.MedicalCenterServiceClient;
import consulting_service.mappers.MedicalConsultationMapper;
import consulting_service.repositories.MedicalConsultationsRepository;
import consulting_service.services.Patient.PatientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalConsultationsServiceImp implements MedicalConsultationsService {

    private final MedicalCenterServiceClient medicalCenterServiceClient;

    private final PatientService patientService;
    private final MedicalConsultationsRepository repository;

    private final MedicalConsultationMapper mapper;


    public MedicalConsultationsServiceImp(MedicalCenterServiceClient medicalCenterServiceClient, MedicalConsultationsRepository repository, PatientService patientService, MedicalConsultationMapper mapper) {
        this.medicalCenterServiceClient = medicalCenterServiceClient;
        this.repository = repository;
        this.patientService = patientService;
        this.mapper = mapper;
    }

    @Override
    public List<MedicalConsultationResponseDTO> getMedicalConsultations(Long doctorId) {


        //TODO: Validar que doctor no este de baja


        List<MedicalConsultation> medicalConsultations = repository.findByDoctorIdAndDeletedFalse(doctorId);

        if (medicalConsultations.isEmpty()) {
            throw new NotFoundException("No se han encontrado consultas médicas para el doctor");
        }

        Long centerId = medicalConsultations.get(0).getCenterId();

        MedicalCenterReadDTO center = medicalCenterServiceClient.getName(centerId);

        return medicalConsultations.stream()
                .map(mc -> {

                    MedicalConsultationResponseDTO response = mapper.toDTO(mc);

                    PatientResponseDTO patient = patientService.getPatientTC(mc.getPatientId());
                    response.setPatient(patient);

                    //TODO: Usar el servicio real
                    DoctorReadDTO doctor = new DoctorReadDTO(
                            mc.getDoctorId(),
                            "John",
                            "Doe"
                    );
                    response.setDoctor(doctor);


                    response.setCenter(center);

                    return response;
                })
                .toList();

    }

    @Override
    public MedicalConsultationResponseDTO getMedicalConsultationById(Long id) {

        MedicalConsultation medicalConsultation = repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException("Consulta no encontrada"));


        PatientResponseDTO patient = patientService.getPatientTC(medicalConsultation.getPatientId());

        MedicalCenterReadDTO center = medicalCenterServiceClient.getName(medicalConsultation.getCenterId());

        //Cuando este el endpoint de doctor uso ese
        DoctorReadDTO doctor = new DoctorReadDTO(
                1L,
                "John",
                "Doe"
        );

        MedicalConsultationResponseDTO response = this.mapper.toDTO(medicalConsultation);

        response.setPatient(patient);

        response.setDoctor(doctor);

        response.setCenter(center);

        return response;
    }

    @Override
    public MedicalConsultationResponseDTO addMedicalConsultation(MedicalConsultationRequestDTO request) {


        PatientResponseDTO patient = patientService.getPatientTC(request.getPatientId());
        MedicalCenterReadDTO center = medicalCenterServiceClient.getName(request.getCenterId());
        DoctorReadDTO doctor = new DoctorReadDTO(request.getDoctorId(), "John", "Doe");


        MedicalConsultation record = this.mapper.toEntity(request);

        record = repository.save(record);

        MedicalConsultationResponseDTO response = this.mapper.toDTO(record);

        response.setPatient(patient);
        response.setDoctor(doctor);
        response.setCenter(center);

        return response;
    }

    @Override
    public MedicalConsultationResponseDTO updateMedicalConsultation(Long id, MedicalConsultationRequestDTO request) {


        MedicalConsultation record = repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Consulta médica no encontrada"));


        mapper.updateEntityFromDto(request, record);


        PatientResponseDTO patient = patientService.getPatientTC(request.getPatientId());
        MedicalCenterReadDTO center = medicalCenterServiceClient.getName(request.getCenterId());
        DoctorReadDTO doctor = new DoctorReadDTO(request.getDoctorId(), "John", "Doe");


        repository.save(record);

        MedicalConsultationResponseDTO response = this.mapper.toDTO(record);
        response.setPatient(patient);
        response.setDoctor(doctor);
        response.setCenter(center);

        return response;
    }

    @Override
    public void deleteMedicalConsultation(Long id) {
        MedicalConsultation record = repository.findByIdAndDeletedFalse(id).orElseThrow(() -> new NotFoundException("Consulta no encontrada"));
        record.setDeleted(true);
        repository.save(record);

    }
}
