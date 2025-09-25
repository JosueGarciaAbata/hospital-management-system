package com.hospital.admin_service.rest;

import com.hospital.admin_service.dto.doctor.DoctorCreateRequest;
import com.hospital.admin_service.dto.doctor.DoctorRead;
import com.hospital.admin_service.dto.doctor.DoctorRegisterRequest;
import com.hospital.admin_service.dto.doctor.DoctorUpdateRequest;
import com.hospital.admin_service.mapper.DoctorMapper;
import com.hospital.admin_service.model.Doctor;
import com.hospital.admin_service.security.filters.RequireRole;
import com.hospital.admin_service.service.doctor.DoctorReadService;
import com.hospital.admin_service.service.doctor.DoctorWriteService;
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
@RequestMapping("/admin/doctors")
@RequiredArgsConstructor
@Validated
public class DoctorController {

    private final DoctorMapper mapper;
    private final DoctorReadService readService;
    private final DoctorWriteService writeService;

    /* =========================
     *          READ
     * ========================= */

    @RequireRole("ADMIN")
    @GetMapping
    public Page<DoctorRead> list(@RequestParam(defaultValue = "false") boolean includeDeleted,
                                 Pageable pageable) {
        return readService.findAllPage(includeDeleted, pageable);
    }

    @RequireRole("ADMIN")
    @GetMapping("/all")
    public List<DoctorRead> listAll(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findAllEntities(includeDeleted).stream()
                .toList();
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    public DoctorRead getOne(@PathVariable Long id,
                             @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findEntityById(id, includeDeleted);
    }

    @RequireRole({"ADMIN","DOCTOR"})
    @GetMapping("/by-user/{userId}")
    public DoctorRead getByUserId(@PathVariable Long userId) {
        return readService.findByUserId(userId);
    }

    @RequireRole("ADMIN")
    @GetMapping("/by-specialty/{specialtyId}")
    public Page<DoctorRead> listBySpecialty(@PathVariable Long specialtyId, Pageable pageable) {
        return readService.findBySpecialty(specialtyId, pageable);
    }

    /* =========================
     *         WRITE
     * ========================= */

    /** Registro compuesto: crea User (Auth) con rol DOCTOR y luego crea Doctor local */
    @RequireRole("ADMIN")
    @PostMapping("/register")
    public ResponseEntity<DoctorRead> register(@Valid @RequestBody DoctorRegisterRequest body) {
        var saved = writeService.registerDoctor(body);
        var dto   = mapper.toRead(saved);
        return ResponseEntity.created(URI.create("/admin/doctors/" + dto.id())).body(dto);
    }

    @RequireRole("ADMIN")
    @PostMapping
    public ResponseEntity<DoctorRead> create(@Valid @RequestBody DoctorCreateRequest body) {
        Doctor entity = mapper.toEntity(body);
        Doctor saved  = writeService.create(entity, body.specialtyId());
        URI location  = URI.create("/admin/doctors/" + saved.getId());
        return ResponseEntity.created(location).body(mapper.toRead(saved));
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public DoctorRead update(@PathVariable Long id,
                             @Valid @RequestBody DoctorUpdateRequest body) {
        Doctor incoming = new Doctor();
        mapper.updateEntityFromDto(body, incoming);
        Doctor saved = writeService.update(id, incoming, body.specialtyId());
        return mapper.toRead(saved);
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        writeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
