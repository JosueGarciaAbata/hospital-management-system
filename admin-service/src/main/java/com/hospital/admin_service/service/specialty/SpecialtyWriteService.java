package com.hospital.admin_service.service.specialty;

import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.repo.SpecialtyRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@RequiredArgsConstructor
public class SpecialtyWriteService {

    private final SpecialtyRepository repository;

    /** CREATE (optimista por defecto: @Version arranca en inserts) */
    @Transactional
    public Specialty create(Specialty entity) {
        return repository.save(entity);
    }

    /** UPDATE (optimista): confía en @Version para detectar conflictos */
    @Transactional
    public Specialty update(Long id, Specialty incoming) {
        Specialty current = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especialidad no encontrada."
                ));

        current.setName(incoming.getName());
        current.setDescription(incoming.getDescription());

        try {
            return repository.save(current);
        } catch (OptimisticLockException e) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "La especialidad fue modificada por otro proceso.", e
            );
        }
    }

    /** UPDATE con bloqueo pesimista (exclusión durante la transacción) */
    @Transactional
    public Specialty updateWithPessimisticLock(Long id, Specialty incoming) {
        Specialty current = repository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especialidad no encontrada."
                ));

        current.setName(incoming.getName());
        current.setDescription(incoming.getDescription());

        return repository.save(current);
    }

    /** DELETE lógico con PESSIMISTIC_WRITE para evitar doble borrado */
    @Transactional
    public void softDelete(Long id) {
        Specialty current = repository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Especialidad no encontrada."
                ));
        repository.delete(current);
    }
}
