package consulting_service.rests;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import consulting_service.mappers.PatientMapper;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.Patient.PatientService;
import consulting_service.services.Patient.PatientServiceImp;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/consulting/patients")
public class PatientController {

    private final PatientService service;
    private final PatientMapper mapper;

    public PatientController(PatientServiceImp service, PatientMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /**
     * Aunque el gateway pone el centerId en los headers (@RequestHeader("X-Center-Id") Long centerId),
     * en caso de que otro microservicio lo necesite tal vez no sea lo más óptimo,
     * así que mejor lo mando como RequestParam.
     */
    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping
    public ResponseEntity<?> getPatients(
            @RequestParam Long centerId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        Page<PatientResponseDTO> patientsPage = service.getPatients(centerId, page, size);

        if (patientsPage.isEmpty()) {
            Map<String, String> response = Map.of(
                    "detail", "No hay pacientes para este centro médico"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }

        return ResponseEntity.ok(patientsPage);
    }

    @RolesAllowed({"DOCTOR", "ADMIN"})
    @GetMapping("/center-has-patients/{centerId}")
    public ResponseEntity<Void> checkCenter(@PathVariable Long centerId) {
        boolean exists = service.centerHasPatients(centerId);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RolesAllowed({"DOCTOR", "ADMIN"})
    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id) {
        Patient patient = service.getPatient(id);
        return ResponseEntity.ok(patient);
    }

    @RolesAllowed("DOCTOR")
    @PostMapping
    public ResponseEntity<PatientResponseDTO> addPatient(@Valid @RequestBody PatientRequestDTO request) {
        Patient patient = service.addPatient(request);
        PatientResponseDTO response = mapper.toDTO(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed("DOCTOR")
    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id,
                                                            @Valid @RequestBody PatientRequestDTO request) {
        Patient patient = service.updatePatient(id, request);
        PatientResponseDTO response = mapper.toDTO(patient);
        return ResponseEntity.ok(response);
    }

    @RolesAllowed("DOCTOR")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        service.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
