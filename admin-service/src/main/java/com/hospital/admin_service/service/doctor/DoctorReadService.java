package com.hospital.admin_service.service.doctor;

import com.hospital.admin_service.dto.doctor.DoctorRead;
import com.hospital.admin_service.repo.DoctorRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class DoctorReadService {

    private final DoctorRepository repository;
    private final DoctorReadAssembler assembler;

    public List<DoctorRead> findAllEntities(boolean includeDeleted) {
        var list = includeDeleted ? repository.findAllIncludingDeleted() : repository.findAll();
        return list.stream().map(assembler::toRead).toList();
    }

    public Page<DoctorRead> findAllPage(boolean includeDeleted, Pageable pageable) {
        var page = includeDeleted ? repository.findAllIncludingDeletedPage(pageable) : repository.findAll(pageable);
        return assembler.toReadPage(page);
    }

    public DoctorRead findEntityById(Long id, boolean includeDeleted) {
        var entity = (includeDeleted ? repository.findByIdIncludingDeleted(id) : repository.findById(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El Doctor no existe."));
        return assembler.toRead(entity);
    }

    public DoctorRead findByUserId(Long userId) {
        var entity = repository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "No existe Doctor activo para el usuario dado."));
        return assembler.toRead(entity);
    }

    public Page<DoctorRead> findBySpecialty(Long specialtyId, Pageable pageable) {
        return assembler.toReadPage(repository.findAllBySpecialty_Id(specialtyId, pageable));
    }
}

