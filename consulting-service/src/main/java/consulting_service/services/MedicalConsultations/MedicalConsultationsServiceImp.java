package consulting_service.services.MedicalConsultations;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.DoctorReadDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.MedicalConsultation;
import consulting_service.dtos.response.MedicalConsultations.MedicalCenterReadDTO;
import consulting_service.exceptions.NotFoundException;
import consulting_service.feign.admin_service.services.DoctorServiceClient;
import consulting_service.feign.admin_service.services.MedicalCenterServiceClient;
import consulting_service.feign.auth_service.services.UserServiceClient;
import consulting_service.mappers.MedicalConsultationMapper;
import consulting_service.repositories.MedicalConsultationsRepository;
import consulting_service.services.Patient.PatientService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MedicalConsultationsServiceImp implements MedicalConsultationsService {

    private final MedicalCenterServiceClient medicalCenterServiceClient;

    private final DoctorServiceClient doctorServiceClient;

    private final UserServiceClient userServiceClient;

    private final PatientService patientService;
    private final MedicalConsultationsRepository repository;

    private final MedicalConsultationMapper mapper;


    public MedicalConsultationsServiceImp(MedicalCenterServiceClient medicalCenterServiceClient, DoctorServiceClient doctorServiceClient, UserServiceClient userServiceClient, PatientService patientService, MedicalConsultationsRepository repository, MedicalConsultationMapper mapper) {
        this.medicalCenterServiceClient = medicalCenterServiceClient;
        this.doctorServiceClient = doctorServiceClient;
        this.userServiceClient = userServiceClient;
        this.patientService = patientService;
        this.repository = repository;
        this.mapper = mapper;
    }

    @Override
    public List<MedicalConsultationResponseDTO> getMedicalConsultations(Long doctorId) {

        Long userId = doctorServiceClient.getUserId(doctorId);

        DoctorReadDTO doctor = userServiceClient.getDoctorByUserId(userId);

        doctor.setId(doctorId);

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

        Long userId = doctorServiceClient.getUserId(medicalConsultation.getDoctorId());

        DoctorReadDTO doctor = userServiceClient.getDoctorByUserId(userId);

        doctor.setId(medicalConsultation.getDoctorId());

        MedicalConsultationResponseDTO response = this.mapper.toDTO(medicalConsultation);

        response.setId(userId);
        response.setPatient(patient);

        response.setDoctor(doctor);

        response.setCenter(center);

        return response;
    }

    @Override
    public MedicalConsultationResponseDTO addMedicalConsultation(MedicalConsultationRequestDTO request) {


        PatientResponseDTO patient = patientService.getPatientTC(request.getPatientId());
        MedicalCenterReadDTO center = medicalCenterServiceClient.getName(request.getCenterId());

        MedicalConsultation record = this.mapper.toEntity(request);

        Long userId = doctorServiceClient.getUserId(record.getDoctorId());

        DoctorReadDTO doctor = userServiceClient.getDoctorByUserId(userId);

        doctor.setId(record.getDoctorId());

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

        Long userId = doctorServiceClient.getUserId(record.getDoctorId());

        DoctorReadDTO doctor = userServiceClient.getDoctorByUserId(userId);

        doctor.setId(record.getDoctorId());

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

    @Override
    public boolean centerHasConsultations(Long centerId) {
        return repository.existsByCenterIdAndDeletedFalse(centerId);
    }

    @Override
    public boolean doctorHasConsultations(Long doctorId) {
        return repository.existsByDoctorIdAndDeletedFalse(doctorId);
    }
}
