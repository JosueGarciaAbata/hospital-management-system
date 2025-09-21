package com.hospital.admin_service.service.specialty;

import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.repo.SpecialtyRepository;
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
public class SpecialtyReadService {

    private final SpecialtyRepository repository;

    public List<Specialty> findAllEntities(boolean includeDeleted) {
        return includeDeleted ? repository.findAllIncludingDeleted()
                : repository.findAll();
    }

    public Page<Specialty> findAllPage(boolean includeDeleted, Pageable pageable) {
        return includeDeleted ? repository.findAllIncludingDeletedPage(pageable)
                : repository.findAll(pageable);
    }

    public Specialty findEntityById(Long id, boolean includeDeleted) {
        return (includeDeleted ? repository.findByIdIncludingDeleted(id) : repository.findById(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "La Especialidad no existe."
                ));
    }
}
