package consulting_service.rests;

import consulting_service.entities.Patient;
import consulting_service.services.PatientService;
import consulting_service.services.PatientServiceImp;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consulting/patients")
public class PatientController {

    private final PatientService service;

    public PatientController(PatientServiceImp service) {
        this.service = service;
    }

    @GetMapping
    public ResponseEntity<?> getPatients(@RequestHeader("X-Center-Id") Long centerId) {
        List<Patient> patients = service.getPatients(centerId);

        if (patients.isEmpty()) {
            Map<String, String> response = Map.of(
                    "detail", "No hay pacientes para este centro médico"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
        }
        return ResponseEntity.ok(patients);
    }


    @GetMapping("/{id}")
    public ResponseEntity<Patient> getPatient(@PathVariable Long id,
                                              @RequestHeader("X-Center-Id") Long centerId) {
        Patient patient = service.getPatient(id, centerId);

        return ResponseEntity.ok(patient);

    }
}
