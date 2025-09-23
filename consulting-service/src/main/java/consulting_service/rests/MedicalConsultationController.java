package consulting_service.rests;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import consulting_service.feign.admin_service.dtos.DoctorRead;
import consulting_service.feign.admin_service.services.DoctorServiceClient;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.MedicalConsultations.MedicalConsultationsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consulting/medical-consultations")
public class MedicalConsultationController {

    private final MedicalConsultationsService service;
    private final DoctorServiceClient doctorServiceClient;

    public MedicalConsultationController(MedicalConsultationsService service, DoctorServiceClient doctorServiceClient) {
        this.service = service;
        this.doctorServiceClient = doctorServiceClient;
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getMedicalConsultations(
            @RequestParam Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> medicalConsultations =
                service.getMedicalConsultations(doctorId, page, size);

        return ResponseEntity.ok(medicalConsultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-center/{centerId}")
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getConsultationsByCenter(
            @PathVariable Long centerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> consultations =
                service.getMedicalConsultationsByCenter(centerId, page, size);

        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/all")
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getAllConsultations(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> consultations =
                service.getAllMedicalConsultations(page, size);

        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-specialty/{specialtyId}")
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getConsultationsBySpecialty(
            @PathVariable Long specialtyId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> consultations =
                service.getMedicalConsultationsBySpecialty(specialtyId, page, size);

        return ResponseEntity.ok(consultations);
    }


    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/center-has-consultations/{centerId}")
    public ResponseEntity<Void> checkCenter(@PathVariable Long centerId) {
        boolean exists = service.centerHasConsultations(centerId);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/doctor-has-consultations/{doctorId}")
    public ResponseEntity<Void> checkDoctor(@PathVariable Long doctorId) {
        boolean exists = service.doctorHasConsultations(doctorId);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/{id}")
    public ResponseEntity<MedicalConsultationResponseDTO> getMedicalConsultation(@PathVariable Long id
    ) {
        MedicalConsultationResponseDTO response = this.service.getMedicalConsultationById(id);

        return ResponseEntity.ok(response);

    }



    @RolesAllowed("DOCTOR")
    @PostMapping
    public ResponseEntity<MedicalConsultationResponseDTO> addMedicalConsultation(@Valid @RequestBody MedicalConsultationRequestDTO request) {

        MedicalConsultationResponseDTO response = service.addMedicalConsultation(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }
    @RolesAllowed("DOCTOR")
    @PutMapping("/{id}")
    public ResponseEntity<MedicalConsultationResponseDTO> updateMedicalConsultation(@PathVariable Long id,@Valid  @RequestBody MedicalConsultationRequestDTO request) {

        MedicalConsultationResponseDTO response = service.updateMedicalConsultation(id, request);

        return ResponseEntity.ok(response);
    }

    @RolesAllowed("DOCTOR")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteMedicalConsultation(@PathVariable Long id) {
        service.deleteMedicalConsultation(id);
        return ResponseEntity.noContent().build();
    }



}
