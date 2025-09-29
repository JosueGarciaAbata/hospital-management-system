package consulting_service.rests;

import consulting_service.dtos.request.MedicalConsultationRequestDTO;
import consulting_service.dtos.response.MedicalConsultations.MedicalConsultationResponseDTO;
import consulting_service.feign.admin_service.services.DoctorServiceClient;
import consulting_service.security.annotations.RolesAllowed;
import consulting_service.services.MedicalConsultations.MedicalConsultationsService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

@RestController
@RequestMapping("/api/consulting/medical-consultations")
@Tag(name = "Consultas Médicas", description = "Gestión de consultas médicas")
public class MedicalConsultationController {

    private final MedicalConsultationsService service;
    public MedicalConsultationController(MedicalConsultationsService service) {
        this.service = service;
    }

    /* =========================
     *          READING
     * ========================= */

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping
    @Operation(
            summary = "Listar consultas médicas por doctor (paginado)",
            description = "Devuelve una página de consultas médicas filtradas por el identificador del doctor."
    )
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getMedicalConsultations(
            @Parameter(description = "Identificador del doctor", example = "12")
            @RequestParam Long doctorId,
            @Parameter(description = "Número de página (0-indexado)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> medicalConsultations =
                service.getMedicalConsultations(doctorId, page, size);

        return ResponseEntity.ok(medicalConsultations);
    }



    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-center/{centerId}")
    @Operation(
            summary = "Listar consultas médicas por centro (paginado)",
            description = "Devuelve una página de consultas médicas filtradas por el identificador del centro médico."
    )
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getConsultationsByCenter(
            @Parameter(description = "Identificador del centro médico", example = "5")
            @PathVariable Long centerId,
            @Parameter(description = "Número de página (0-indexado)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> consultations =
                service.getMedicalConsultationsByCenter(centerId, page, size);

        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/all")
    @Operation(
            summary = "Listar todas las consultas médicas (paginado)",
            description = "Devuelve una página con todas las consultas médicas."
    )
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getAllConsultations(
            @Parameter(description = "Número de página (0-indexado)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> consultations =
                service.getAllMedicalConsultations(page, size);

        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/by-specialty/{specialtyId}")
    @Operation(
            summary = "Listar consultas médicas por especialidad (paginado)",
            description = "Devuelve una página de consultas médicas filtradas por el identificador de la especialidad."
    )
    public ResponseEntity<Page<MedicalConsultationResponseDTO>> getConsultationsBySpecialty(
            @Parameter(description = "Identificador de la especialidad", example = "3")
            @PathVariable Long specialtyId,
            @Parameter(description = "Número de página (0-indexado)", example = "0")
            @RequestParam(defaultValue = "0") int page,
            @Parameter(description = "Tamaño de página", example = "10")
            @RequestParam(defaultValue = "10") int size) {

        Page<MedicalConsultationResponseDTO> consultations =
                service.getMedicalConsultationsBySpecialty(specialtyId, page, size);

        return ResponseEntity.ok(consultations);
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/center-has-consultations/{centerId}")
    @Operation(
            summary = "Validar si un centro tiene consultas",
            description = "Devuelve 200 OK si el centro tiene consultas; 404 Not Found en caso contrario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El centro tiene consultas"),
            @ApiResponse(responseCode = "404", description = "El centro no tiene consultas")
    })
    public ResponseEntity<Void> checkCenter(
            @Parameter(description = "Identificador del centro médico", example = "5")
            @PathVariable Long centerId) {
        boolean exists = service.centerHasConsultations(centerId);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/doctor-has-consultations/{doctorId}")
    @Operation(
            summary = "Validar si un doctor tiene consultas",
            description = "Devuelve 200 OK si el doctor tiene consultas; 404 Not Found en caso contrario."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "El doctor tiene consultas"),
            @ApiResponse(responseCode = "404", description = "El doctor no tiene consultas")
    })
    public ResponseEntity<Void> checkDoctor(
            @Parameter(description = "Identificador del doctor", example = "12")
            @PathVariable Long doctorId) {
        boolean exists = service.doctorHasConsultations(doctorId);
        return exists ? ResponseEntity.ok().build() : ResponseEntity.notFound().build();
    }

    @RolesAllowed({"ADMIN", "DOCTOR"})
    @GetMapping("/{id}")
    @Operation(
            summary = "Obtener una consulta médica por ID",
            description = "Devuelve la información de una consulta médica por su identificador."
    )
    public ResponseEntity<MedicalConsultationResponseDTO> getMedicalConsultation(
            @Parameter(description = "Identificador de la consulta médica", example = "1001")
            @PathVariable Long id) {
        MedicalConsultationResponseDTO response = this.service.getMedicalConsultationById(id);
        return ResponseEntity.ok(response);
    }

    /* =========================
     *          WRITING
     * ========================= */

    @RolesAllowed("DOCTOR")
    @PostMapping
    @Operation(
            summary = "Crear una consulta médica",
            description = "Crea una nueva consulta médica."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Consulta médica creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Datos inválidos en la solicitud")
    })
    public ResponseEntity<MedicalConsultationResponseDTO> addMedicalConsultation(
            @Valid @RequestBody MedicalConsultationRequestDTO request) {
        MedicalConsultationResponseDTO response = service.addMedicalConsultation(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @RolesAllowed("DOCTOR")
    @PutMapping("/{id}")
    @Operation(
            summary = "Actualizar una consulta médica",
            description = "Actualiza una consulta médica existente por su identificador."
    )
    public ResponseEntity<MedicalConsultationResponseDTO> updateMedicalConsultation(
            @Parameter(description = "Identificador de la consulta médica", example = "1001")
            @PathVariable Long id,
            @Valid @RequestBody MedicalConsultationRequestDTO request) {
        MedicalConsultationResponseDTO response = service.updateMedicalConsultation(id, request);
        return ResponseEntity.ok(response);
    }

    @RolesAllowed("DOCTOR")
    @DeleteMapping("/{id}")
    @Operation(
            summary = "Eliminar una consulta médica",
            description = "Elimina lógicamente una consulta médica por su identificador."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Consulta médica eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Consulta médica no encontrada")
    })
    public ResponseEntity<Void> deleteMedicalConsultation(
            @Parameter(description = "Identificador de la consulta médica", example = "1001")
            @PathVariable Long id) {
        service.deleteMedicalConsultation(id);
        return ResponseEntity.noContent().build();
    }
}
