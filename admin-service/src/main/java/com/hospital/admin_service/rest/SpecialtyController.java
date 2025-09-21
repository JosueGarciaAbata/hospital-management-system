package com.hospital.admin_service.rest;

import com.hospital.admin_service.DTO.specialty.SpecialtyCreateRequest;
import com.hospital.admin_service.DTO.specialty.SpecialtyRead;
import com.hospital.admin_service.DTO.specialty.SpecialtyUpdateRequest;
import com.hospital.admin_service.mapper.SpecialtyMapper;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.security.filters.RequireRole;
import com.hospital.admin_service.service.specialty.SpecialtyReadService;
import com.hospital.admin_service.service.specialty.SpecialtyWriteService;
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
@RequestMapping("/admin/specialties")
@RequiredArgsConstructor
@Validated
public class SpecialtyController {

    private final SpecialtyMapper mapper;
    private final SpecialtyReadService readService;
    private final SpecialtyWriteService writeService;

    @RequireRole("ADMIN")
    @GetMapping
    public Page<SpecialtyRead> list(@RequestParam(defaultValue = "false") boolean includeDeleted,
                                    @org.springdoc.core.annotations.ParameterObject Pageable pageable) {
        return readService.findAllPage(includeDeleted, pageable).map(mapper::toRead);
    }

    @RequireRole("ADMIN")
    @GetMapping("/all")
    public List<SpecialtyRead> listAll(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findAllEntities(includeDeleted).stream()
                .map(mapper::toRead)
                .toList();
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    public ResponseEntity<SpecialtyRead> getOne(@PathVariable Long id,
                                                @RequestParam(defaultValue = "false") boolean includeDeleted) {
        var dto = mapper.toRead(readService.findEntityById(id, includeDeleted));
        return ResponseEntity.ok()
                .eTag("\"" + dto.version() + "\"")
                .body(dto);
    }

    @RequireRole("ADMIN")
    @PostMapping
    public ResponseEntity<SpecialtyRead> create(@Valid @RequestBody SpecialtyCreateRequest body) {
        Specialty saved = writeService.create(mapper.toEntity(body));
        URI location = URI.create("/admin/specialties/" + saved.getId());
        return ResponseEntity.created(location)
                .eTag("\"" + saved.getVersion() + "\"")
                .body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public ResponseEntity<SpecialtyRead> update(@PathVariable Long id,
                                                @RequestHeader(value = "If-Match", required = false) String ifMatch,
                                                @Valid @RequestBody SpecialtyUpdateRequest body) {
        var incoming = new Specialty();
        mapper.updateEntityFromDto(body, incoming);
        var saved = writeService.update(id, incoming); // @Version protege concurrencia
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
}
