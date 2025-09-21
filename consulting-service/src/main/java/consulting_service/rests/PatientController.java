package consulting_service.rests;

import consulting_service.dtos.request.PatientRequestDTO;
import consulting_service.dtos.response.PatientResponseDTO;
import consulting_service.entities.Patient;
import consulting_service.mappers.PatientMapper;
import consulting_service.services.PatientService;
import consulting_service.services.PatientServiceImp;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/consulting/patients")
public class PatientController {

    private final PatientService service;
    private final PatientMapper mapper;
    public PatientController(PatientServiceImp service,PatientMapper mapper) {
        this.service = service;
        this.mapper = mapper;
    }

    /*
    *  aunque el gateway pone el center id en los header (@RequestHeader("X-Center-Id") Long centerId), en caso
    * de que otro microservicio lo necesite tal vez no sea lo mas optimo
    * asi que mejor lo mando como RequestParam
    * */
    @GetMapping
    public ResponseEntity<?> getPatients(@RequestParam Long centerId) {
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
    public ResponseEntity<Patient> getPatient(@PathVariable Long id
                                             ) {
        Patient patient = service.getPatient(id);

        return ResponseEntity.ok(patient);

    }

    @PostMapping
    public ResponseEntity<PatientResponseDTO> addPatient(@Valid  @RequestBody PatientRequestDTO request) {

        Patient patient = service.addPatient(request);

        PatientResponseDTO response = mapper.toDTO(patient);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);

    }

    @PutMapping("/{id}")
    public ResponseEntity<PatientResponseDTO> updatePatient(@PathVariable Long id,@Valid  @RequestBody PatientRequestDTO request) {

        Patient patient = service.updatePatient(id, request);

        PatientResponseDTO response = mapper.toDTO(patient);

        return ResponseEntity.ok(response);

    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        service.deletePatient(id);
        return ResponseEntity.noContent().build();
    }


}
