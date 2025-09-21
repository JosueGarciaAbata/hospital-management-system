package com.hospital.admin_service.rest;

import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterCreateRequest;
import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterRead;
import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterUpdateRequest;
import com.hospital.admin_service.mapper.MedicalCenterMapper;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.security.filters.RequireRole;
import com.hospital.admin_service.service.medicalCenter.MedicalCenterWriteService;
import com.hospital.admin_service.service.medicalCenter.MedicalCenterReadService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

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
    public List<MedicalCenterRead> list(@RequestParam(defaultValue = "false") boolean includeDeleted) {
        return readService.findAllEntities(includeDeleted).stream()
                .map(mapper::toRead)
                .toList();
    }

    @RequireRole("ADMIN")
    @GetMapping("/{id}")
    public MedicalCenterRead getOne(@PathVariable Long id,
                                    @RequestParam(defaultValue = "false") boolean includeDeleted) {
        return mapper.toRead(readService.findEntityById(id, includeDeleted));
    }

    @RequireRole("ADMIN")
    @PostMapping
    public MedicalCenterRead create(@Valid @RequestBody MedicalCenterCreateRequest body) {
        MedicalCenter entity = mapper.toEntity(body);
        MedicalCenter saved  = writeService.create(entity);
        return mapper.toRead(saved);
    }

    @RequireRole("ADMIN")
    @PutMapping("/{id}")
    public MedicalCenterRead update(@PathVariable Long id,
                                    @Valid @RequestBody MedicalCenterUpdateRequest body) {

        MedicalCenter incoming = new MedicalCenter();
        mapper.updateEntityFromDto(body, incoming);

        MedicalCenter saved = writeService.update(id, incoming);
        return mapper.toRead(saved);
    }

    @RequireRole("ADMIN")
    @DeleteMapping("/{id}")
    public void delete(@PathVariable Long id) {
        writeService.softDelete(id);
    }
}

