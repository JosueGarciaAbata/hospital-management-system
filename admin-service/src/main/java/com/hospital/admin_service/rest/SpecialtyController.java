package com.hospital.admin_service.rest;

import com.hospital.admin_service.dto.specialty.SpecialtyCreateRequest;
import com.hospital.admin_service.dto.specialty.SpecialtyRead;
import com.hospital.admin_service.dto.specialty.SpecialtyUpdateRequest;
import com.hospital.admin_service.mapper.SpecialtyMapper;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.security.filters.RequireRole;
import com.hospital.admin_service.service.specialty.SpecialtyReadService;
import com.hospital.admin_service.service.specialty.SpecialtyWriteService;
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
@RequestMapping("/admin/specialties")
@RequiredArgsConstructor
@Validated
@Tag(name = "Especialidades Médicas", description = "Gestión de especialidades médicas en el sistema")
public class SpecialtyController {

    private final SpecialtyMapper mapper;
    private final SpecialtyReadService readService;
    private final SpecialtyWriteService writeService;

    /* =========================
     *          READING
     * ========================= */

    @RequireRole("ADMIN")
    @GetMapping
    @Operation(summary = "Listar especialidades médicas paginadas",
            description = "Devuelve una lista paginada de especialidades médicas. Opcionalmente incluye registros eliminados lógicamente.")
    public Page<SpecialtyRead> list(
            @Parameter(description = "Indica si se incluyen especialidades eliminadas", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted,
            @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return readService.findAllPage(includeDeleted, pageable).map(mapper::toRead);
    }

    @RequireRole("ADMIN")
    @GetMapping("/all")
    @Operation(summary = "Listar todas las especialidades médicas",
            description = "Devuelve la lista completa de especialidades médicas sin paginación. Opcionalmente incluye registros eliminados lógicamente.")
    public List<SpecialtyRead> listAll(
            @Parameter(description = "Indica si se incluyen especialidades eliminadas", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findAllEntities(includeDeleted).stream()
                .map(mapper::toRead)
                .toList();
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    @Operation(summary = "Obtener una especialidad médica por ID",
            description = "Devuelve una especialidad médica por su identificador. Opcionalmente incluye registros eliminados lógicamente.")
    public ResponseEntity<SpecialtyRead> getOne(
            @Parameter(description = "Identificador de la especialidad médica", example = "1") @PathVariable Long id,
            @Parameter(description = "Indica si se incluyen especialidades eliminadas", example = "false")
            @RequestParam(defaultValue = "false") boolean includeDeleted) {
        var dto = mapper.toRead(readService.findEntityById(id, includeDeleted));
        return ResponseEntity.ok()
                .eTag("\"" + dto.version() + "\"")
                .body(dto);
    }

    /* =========================
     *          WRITING
     * ========================= */

    @RequireRole("ADMIN")
    @PostMapping
    @Operation(summary = "Crear una especialidad médica",
            description = "Crea una nueva especialidad médica en el sistema.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Especialidad creada correctamente"),
            @ApiResponse(responseCode = "400", description = "Error en el cuerpo de la solicitud")
    })
    public ResponseEntity<SpecialtyRead> create(
            @Valid @RequestBody SpecialtyCreateRequest body) {
        Specialty saved = writeService.create(mapper.toEntity(body));
        URI location = URI.create("/admin/specialties/" + saved.getId());
        return ResponseEntity.created(location)
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    @Operation(summary = "Actualizar una especialidad médica",
            description = "Actualiza una especialidad médica existente. El control de concurrencia se maneja con el campo @Version.")
    public ResponseEntity<SpecialtyRead> update(
            @Parameter(description = "Identificador de la especialidad médica", example = "1") @PathVariable Long id,
            @RequestHeader(value = "If-Match", required = false) String ifMatch,
            @Valid @RequestBody SpecialtyUpdateRequest body) {
        var incoming = new Specialty();
        mapper.updateEntityFromDto(body, incoming);
        var saved = writeService.update(id, incoming);
        return ResponseEntity.ok()
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    @Operation(summary = "Eliminar una especialidad médica (borrado lógico)",
            description = "Marca una especialidad médica como eliminada sin borrarla físicamente de la base de datos.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Especialidad eliminada correctamente"),
            @ApiResponse(responseCode = "404", description = "Especialidad no encontrada")
    })
    public ResponseEntity<Void> delete(
            @Parameter(description = "Identificador de la especialidad médica", example = "1")
            @PathVariable Long id) {
        writeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
