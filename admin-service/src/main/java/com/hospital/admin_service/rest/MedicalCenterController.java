package com.hospital.admin_service.rest;

import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterCreateRequest;
import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterRead;
import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterUpdateRequest;
import com.hospital.admin_service.mapper.MedicalCenterMapper;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.security.filters.RequireRole;
import com.hospital.admin_service.service.medicalCenter.MedicalCenterReadService;
import com.hospital.admin_service.service.medicalCenter.MedicalCenterWriteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/admin/centers")
@RequiredArgsConstructor
@Validated
public class MedicalCenterController {

    private final MedicalCenterMapper mapper;
    private final MedicalCenterReadService readService;
    private final MedicalCenterWriteService writeService;

    @RequireRole("ADMIN")
    @GetMapping
    public Page<MedicalCenterRead> list(@RequestParam(defaultValue = "false") boolean includeDeleted,
                                        @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return readService.findAllPage(includeDeleted, pageable).map(mapper::toRead);
    }

    @RequireRole("ADMIN")
    @GetMapping("/all")
    public List<MedicalCenterRead> listAll(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findAllEntities(includeDeleted).stream()
                .map(mapper::toRead)
                .toList();
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<MedicalCenterRead> getOne(@PathVariable Long id,
                                                    @RequestParam(defaultValue = "false") boolean includeDeleted) {
        var dto = mapper.toRead(readService.findEntityById(id, includeDeleted));
        return ResponseEntity.ok()
                .eTag("\"" + dto.version() + "\"")
                .body(dto);
    }

    @RequireRole("ADMIN")
    @PostMapping
    public ResponseEntity<MedicalCenterRead> create(@Valid @RequestBody MedicalCenterCreateRequest body) {
        MedicalCenter saved = writeService.create(mapper.toEntity(body));
        URI location = URI.create("/admin/centers/" + saved.getId());
        return ResponseEntity.created(location)
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<MedicalCenterRead> update(@PathVariable Long id,
                                                    @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                    @Valid @RequestBody MedicalCenterUpdateRequest body) {
        var incoming = new MedicalCenter();
        mapper.updateEntityFromDto(body, incoming);
        var saved = writeService.update(id, incoming); // @Version protege
        return ResponseEntity.ok()
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        writeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validateee/{id}")
    public ResponseEntity<Void> validateCenterId(@PathVariable Long id) {
        if (!readService.existsById(id)) {
            return ResponseEntity.notFound().build(); // 404 Not Found
        }

        return ResponseEntity.ok().build(); // 200 OK
    }
}
