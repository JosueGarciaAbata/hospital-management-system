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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
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

    public MedicalConsultationsServiceImp(
            MedicalCenterServiceClient medicalCenterServiceClient,
            DoctorServiceClient doctorServiceClient,
            UserServiceClient userServiceClient,
            PatientService patientService,
            MedicalConsultationsRepository repository,
            MedicalConsultationMapper mapper) {
        this.medicalCenterServiceClient = medicalCenterServiceClient;
        this.doctorServiceClient = doctorServiceClient;
        this.userServiceClient = userServiceClient;
        this.patientService = patientService;
        this.repository = repository;
        this.mapper = mapper;
    }


    @Override
    public Page<MedicalConsultationResponseDTO> getMedicalConsultations(Long doctorId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicalConsultation> medicalConsultations = repository.findByDoctorIdAndDeletedFalse(doctorId, pageable);

        if (medicalConsultations.isEmpty()) {
            throw new NotFoundException("No se han encontrado consultas médicas para el doctor");
        }

        return medicalConsultations.map(this::buildMedicalConsultationResponse);
    }

    @Override
    public MedicalConsultationResponseDTO getMedicalConsultationById(Long id) {
        MedicalConsultation medicalConsultation = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Consulta no encontrada"));

        return buildMedicalConsultationResponse(medicalConsultation);
    }

    @Override
    public MedicalConsultationResponseDTO addMedicalConsultation(MedicalConsultationRequestDTO request) {
        MedicalConsultation record = mapper.toEntity(request);
        record = repository.save(record);

        return buildMedicalConsultationResponse(record);
    }

    @Override
    public MedicalConsultationResponseDTO updateMedicalConsultation(Long id, MedicalConsultationRequestDTO request) {
        MedicalConsultation record = repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Consulta médica no encontrada"));

        mapper.updateEntityFromDto(request, record);
        repository.save(record);

        return buildMedicalConsultationResponse(record);
    }

    @Override
    public void deleteMedicalConsultation(Long id) {
        MedicalConsultation record = repository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Consulta no encontrada"));
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

    @Override
    public Page<MedicalConsultationResponseDTO> getMedicalConsultationsByCenter(Long centerId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicalConsultation> consultations = repository.findByCenterIdAndDeletedFalse(centerId, pageable);

        if (consultations.isEmpty()) {
            throw new NotFoundException("No se han encontrado consultas para el centro médico");
        }

        return consultations.map(this::buildMedicalConsultationResponse);
    }

    @Override
    public Page<MedicalConsultationResponseDTO> getAllMedicalConsultations(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<MedicalConsultation> consultations = repository.findByDeletedFalse(pageable);

        if (consultations.isEmpty()) {
            throw new NotFoundException("No se han encontrado consultas médicas");
        }

        return consultations.map(this::buildMedicalConsultationResponse);
    }

    @Override
    public Page<MedicalConsultationResponseDTO> getMedicalConsultationsBySpecialty(Long specialtyId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);


        List<Long> doctorIds = doctorServiceClient.getDoctorIdsBySpecialty(specialtyId, pageable);


        Page<MedicalConsultation> consultations = repository.findByDoctorIdInAndDeletedFalse(doctorIds, pageable);

        if (consultations.isEmpty()) {
            throw new NotFoundException("No se han encontrado consultas para la especialidad solicitada");
        }


        return consultations.map(this::buildMedicalConsultationResponse);
    }


    private MedicalConsultationResponseDTO buildMedicalConsultationResponse(MedicalConsultation mc) {
        MedicalConsultationResponseDTO response = mapper.toDTO(mc);


        PatientResponseDTO patient = patientService.getPatientTC(mc.getPatientId());
        response.setPatient(patient);


        Long userId = doctorServiceClient.getUserId(mc.getDoctorId());

        DoctorReadDTO doctor = userServiceClient.getDoctorByUserId(userId);
        doctor.setId(mc.getDoctorId());
        response.setDoctor(doctor);


        MedicalCenterReadDTO center = medicalCenterServiceClient.getName(mc.getCenterId());
        response.setCenter(center);

        return response;
    }
}
