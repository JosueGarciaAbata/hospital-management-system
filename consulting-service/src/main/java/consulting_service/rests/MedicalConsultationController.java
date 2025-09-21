package consulting_service.rests;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.MedicalConsultations.MedicalConsultationsService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consulting/medical-consultations")
public class MedicalConsultationController {

    private final MedicalConsultationsService service;

    public MedicalConsultationController(MedicalConsultationsService service) {
        this.service = service;
    }

    @RolesAllowed("DOCTOR")
    @GetMapping
    public ResponseEntity<?> getMedicalConsultations(@RequestParam Long doctorId) {
        List<MedicalConsultationResponseDTO> medicalConsultations = service.getMedicalConsultations(doctorId);

        if (medicalConsultations.isEmpty()) {
            Map<String, String> response = Map.of(
                    "detail", "Sin consultas médicas"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(medicalConsultations);
    }

    @RolesAllowed("DOCTOR")
    @GetMapping("/{id}")
    public ResponseEntity<MedicalConsultationResponseDTO> getMedicalConsultation(@PathVariable Long id
    ) {
        MedicalConsultationResponseDTO response = this.service.getMedicalConsultationById(id);

        return ResponseEntity.ok(response);

    }

    @PostMapping
    public ResponseEntity<MedicalConsultationResponseDTO> addMedicalConsultation(@Valid @RequestBody MedicalConsultationRequestDTO request) {

        MedicalConsultationResponseDTO response = service.addMedicalConsultation(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

}
