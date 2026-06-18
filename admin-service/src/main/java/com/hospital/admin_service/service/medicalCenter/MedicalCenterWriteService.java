package com.hospital.admin_service.service.medicalCenter;

import com.hospital.admin_service.external.port.IAuthUserClient;
import com.hospital.admin_service.external.port.IConsultingClient;
import com.hospital.admin_service.external.port.IPatientClient;
import com.hospital.admin_service.mapper.MedicalCenterMapper;
import com.hospital.admin_service.model.MedicalCenter;
import com.hospital.admin_service.repo.MedicalCenterRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Slf4j
@Service
@RequiredArgsConstructor
public class MedicalCenterWriteService {

    private final MedicalCenterRepository repository;
    private final IAuthUserClient authUserClient;
    private final IConsultingClient consultingClient;
    private final IPatientClient patientClient;
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
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Centro Médico no encontrado."));

        try {
            if (authUserClient.hasActiveUsersInCenter(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede eliminar: existen usuarios activos vinculados al centro.");
            }

            if (patientClient.hasActivePatientsInCenter(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede eliminar: existen pacientes activos en el centro.");
            }

            if (consultingClient.hasActiveAppointmentsInCenter(id)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT,
                        "No se puede eliminar: existen consultas asignadas este centro.");
            }
            repository.delete(current);

        } catch (ResponseStatusException rse) {
            throw rse; // ya mapea el status correcto
        } catch (Exception ex) {
            log.error("[MedicalCenterWriteService] Error en softDelete centerId={}: {}", id, ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR,
                    "Error eliminando el centro médico.", ex);
        }
    }
}