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

import java.util.List;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

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
    @Operation(
            summary = "Listar pacientes por centro (paginado)",
            description = "Devuelve una página de pacientes registrados en un centro médico específico."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista de pacientes obtenida correctamente"),
            @ApiResponse(responseCode = "404", description = "No hay pacientes para este centro médico")
    })
    public ResponseEntity<?> getPatients(
            @Parameter(description = "Identificador del centro médico", example = "5")
            @RequestParam Long centerId,
            @Parameter(description = "Número de página (0-indexado)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
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

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/all")
    public ResponseEntity<List<PatientResponseDTO>> getAllPatients(@RequestParam Long centerId) {
        List<PatientResponseDTO> patients = service.getAllPatients(centerId);

        if (patients.isEmpty()) {
            Map<String, String> response = Map.of(
                    "detail", "No hay pacientes para este centro médico"
            );
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }

        return ResponseEntity.ok(patients);
    }

    @RolesAllowed({"DOCTOR", "ADMIN"})
    @GetMapping("/center-has-patients/{centerId}")
    @Operation(
            summary = "Validar si un centro tiene pacientes",
            description = "Devuelve 200 OK si el centro tiene pacientes, 404 si no."
    )
    public ResponseEntity<Void> checkCenter(
            @Parameter(description = "Identificador del centro médico", example = "5")
            @PathVariable Long centerId) {
        boolean exists = service.centerHasPatients(centerId);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RolesAllowed({"DOCTOR", "ADMIN"})
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener paciente por ID",
            description = "Devuelve la información completa de un paciente por su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente encontrado"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<Patient> getPatient(
            @Parameter(description = "Identificador del paciente", example = "101")
            @PathVariable Long id) {
        Patient patient = service.getPatient(id);
        return ResponseEntity.ok(patient);
    }

    @RolesAllowed("DOCTOR")
    @PostMapping
    @Operation(
            summary = "Registrar un nuevo paciente",
            description = "Crea un nuevo registro de paciente en el sistema."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Paciente registrado correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    public ResponseEntity<PatientResponseDTO> addPatient(
            @Valid @RequestBody PatientRequestDTO request) {
        Patient patient = service.addPatient(request);
        PatientResponseDTO response = mapper.toDTO(patient);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed("DOCTOR")
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar un paciente",
            description = "Modifica los datos de un paciente existente."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paciente actualizado correctamente"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @Parameter(description = "Identificador del paciente", example = "101")
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO request) {
        Patient patient = service.updatePatient(id, request);
        PatientResponseDTO response = mapper.toDTO(patient);
        return ResponseEntity.ok(response);
    }

    @RolesAllowed("DOCTOR")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar un paciente",
            description = "Elimina lógicamente un paciente del sistema por su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Paciente eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Paciente no encontrado")
    })
    public ResponseEntity<Void> deletePatient(
            @Parameter(description = "Identificador del paciente", example = "101")
            @PathVariable Long id) {
        service.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}
