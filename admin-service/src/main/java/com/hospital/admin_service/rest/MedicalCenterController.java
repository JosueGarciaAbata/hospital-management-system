package com.hospital.admin_service.rest;

import com.hospital.admin_service.dto.medicalCenter.MedicalCenterCreateRequest;
import com.hospital.admin_service.dto.medicalCenter.MedicalCenterRead;
import com.hospital.admin_service.dto.medicalCenter.MedicalCenterUpdateRequest;
import com.hospital.admin_service.mapper.MedicalCenterMapper;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.security.filters.RequireRole;
import com.hospital.admin_service.service.medicalCenter.MedicalCenterReadService;
import com.hospital.admin_service.service.medicalCenter.MedicalCenterWriteService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/centers")
@RequiredArgsConstructor
@Validated
@Tag(name = "Centros Médicos", description = "Gestión de centros médicos en el sistema")
public class MedicalCenterController {

    private final MedicalCenterMapper mapper;
    private final MedicalCenterReadService readService;
    private final MedicalCenterWriteService writeService;

    /* =========================
     *          READING
     * ========================= */

    @RequireRole("ADMIN")
    @GetMapping
    @Operation(summary = "Listar centros médicos paginados",
            description = "Devuelve una lista paginada de centros médicos. Opcionalmente incluye registros eliminados lógicamente.")
    public Page<MedicalCenterRead> list(
            @Parameter(description = "Indica si se incluyen centros médicos eliminados", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return readService.findAllPage(includeDeleted, pageable).map(mapper::toRead);
    }

    @RequireRole("ADMIN")
    @GetMapping("/all")
    @Operation(summary = "Listar todos los centros médicos",
            description = "Devuelve la lista completa de centros médicos sin paginación. Opcionalmente incluye registros eliminados lógicamente.")
    public List<MedicalCenterRead> listAll(
            @Parameter(description = "Indica si se incluyen centros médicos eliminados", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findAllEntities(includeDeleted).stream()
                .map(mapper::toRead)
                .toList();
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener un centro médico por ID",
            description = "Devuelve un centro médico por su identificador. Opcionalmente incluye registros eliminados lógicamente.")
    public ResponseEntity<MedicalCenterRead> getOne(
            @Parameter(description = "Identificador del centro médico", example = "1") @PathVariable Long id,
            @Parameter(description = "Indica si se incluyen centros médicos eliminados", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        var dto = mapper.toRead(readService.findEntityById(id, includeDeleted));
        return ResponseEntity.ok()
                .eTag("\"" + dto.version() + "\"")
                .body(dto);
    }

    @RequireRole("ADMIN")
    @PostMapping("/batch")
    @Operation(summary = "Obtener múltiples centros médicos por sus IDs",
            description = "Devuelve la lista de centros médicos cuyos identificadores se envían en el cuerpo de la petición.")
    public List<MedicalCenterRead> getByIds(
            @RequestBody List<Long> ids,
            @Parameter(description = "Indica si se incluyen centros médicos eliminados", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findCentersByIds(ids, includeDeleted).stream()
                .map(mapper::toRead)
                .toList();
    }

    /* =========================
     *          WRITING
     * ========================= */

    @RequireRole("ADMIN")
    @PostMapping
    @Operation(summary = "Crear un centro médico",
            description = "Crea un nuevo centro médico en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Centro médico creado correctamente"),
            @ApiResponse(responseCode = "400", description = "Error en el cuerpo de la solicitud")
    })
    public ResponseEntity<MedicalCenterRead> create(
            @Valid @RequestBody MedicalCenterCreateRequest body) {
        MedicalCenter saved = writeService.create(mapper.toEntity(body));
        URI location = URI.create("/admin/centers/" + saved.getId());
        return ResponseEntity.created(location)
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar un centro médico",
            description = "Actualiza un centro médico existente. El control de versiones se maneja con el campo @Version.")
    public ResponseEntity<MedicalCenterRead> update(
            @Parameter(description = "Identificador del centro médico", example = "1") @PathVariable Long id,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody MedicalCenterUpdateRequest body) {
        var incoming = new MedicalCenter();
        mapper.updateEntityFromDto(body, incoming);
        var saved = writeService.update(id, incoming);
        return ResponseEntity.ok()
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar un centro médico (borrado lógico)",
            description = "Marca un centro médico como eliminado sin borrarlo físicamente de la base de datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Centro médico eliminado correctamente"),
            @ApiResponse(responseCode = "404", description = "Centro médico no encontrado")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador del centro médico", example = "1")
            @PathVariable Long id) {
        writeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    /* =========================
     *       VALIDATION
     * ========================= */

    @GetMapping("/validate/{id}")
    @Operation(summary = "Validar existencia de un centro médico",
            description = "Verifica si existe un centro médico con el identificador especificado. Devuelve 200 OK si existe, 404 Not Found si no.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Centro médico encontrado"),
            @ApiResponse(responseCode = "404", description = "Centro médico no encontrado")
    })
    public ResponseEntity<Void> validateCenterId(
            @Parameter(description = "Identificador del centro médico a validar", example = "1")
            @PathVariable Long id) {
        if (!readService.existsById(id)) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok().build();
    }
}
