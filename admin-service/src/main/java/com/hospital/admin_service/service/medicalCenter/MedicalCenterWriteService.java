package com.hospital.admin_service.service.medicalCenter;

import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterCreateRequest;
import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterRead;
import com.hospital.admin_service.DTO.medicalCenter.MedicalCenterUpdateRequest;
import com.hospital.admin_service.mapper.MedicalCenterMapper;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.repo.MedicalCenterRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class MedicalCenterWriteService {

    private final MedicalCenterRepository repository;
    private final MedicalCenterMapper mapper;

    /** CREATE (sin bloqueos; @Version maneja desde la primera inserción) */
    @Transactional
    public MedicalCenter create(MedicalCenter entity) {
        return repository.save(entity);
    }

    /** UPDATE optimista (por defecto): confía en @Version para detectar conflictos */
    @Transactional
    public MedicalCenter update(Long id, MedicalCenter incoming) {
        MedicalCenter current = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro Médico no encontrado."));

        current.setName(incoming.getName());
        current.setCity(incoming.getCity());
        current.setAddress(incoming.getAddress());

        try {
            return repository.save(current); // @Version maneja el conflicto
        } catch (OptimisticLockException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Otro recurso lo modifico.", e);
        }
    }

    /** UPDATE con bloqueo pesimista: nadie más puede escribir mientras dura la transacción */
    @Transactional
    public MedicalCenter updateWithPessimisticLock(Long id, MedicalCenter incoming) {
        MedicalCenter current = repository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro Médico no encontrado."));

        current.setName(incoming.getName());
        current.setCity(incoming.getCity());
        current.setAddress(incoming.getAddress());

        return repository.save(current);
    }

    /** DELETE lógico (soft): preferimos PESSIMISTIC_WRITE para evitar doble borrado concurrente */
    @Transactional
    public void softDelete(Long id) {
        MedicalCenter current = repository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Centro Médico no encontrado."));
        repository.delete(current);
    }
}