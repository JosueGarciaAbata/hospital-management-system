package com.hospital.admin_service.service.medicalCenter;

import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterRead;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.repo.MedicalCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class MedicalCenterReadService {

    private final MedicalCenterRepository repository;

    public List<MedicalCenter> findAllEntities(boolean includeDeleted) {
        return includeDeleted ? repository.findAllIncludingDeleted() : repository.findAll();
    }

    public MedicalCenter findEntityById(Long id, boolean includeDeleted) {
        return (includeDeleted ? repository.findByIdIncludingDeleted(id) : repository.findById(id))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "El Centro Médico no existe."));
    }
}