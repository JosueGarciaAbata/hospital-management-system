package com.hospital.admin_service.service.doctor;

import com.hospital.admin_service.model.Doctor;
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

    public List<Doctor> findAllEntities(boolean includeDeleted) {
        return includeDeleted ? repository.findAllIncludingDeleted() : repository.findAll();
    }

    public Page<Doctor> findAllPage(boolean includeDeleted, Pageable pageable) {
        return includeDeleted ? repository.findAllIncludingDeletedPage(pageable)
                : repository.findAll(pageable);
    }

    public Doctor findEntityById(Long id, boolean includeDeleted) {
        return (includeDeleted ? repository.findByIdIncludingDeleted(id) : repository.findById(id))
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "El Doctor no existe."
                ));
    }

    public Doctor findByUserId(Long userId) {
        return repository.findByUserId(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "No existe Doctor activo para el usuario dado."
                ));
    }

    public Page<Doctor> findBySpecialty(Long specialtyId, Pageable pageable) {
        return repository.findAllBySpecialty_Id(specialtyId, pageable);
    }
}
