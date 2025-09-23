package com.hospital.admin_service.service.doctor;

import com.hospital.admin_service.dto.doctor.DoctorRegisterRequest;
import com.hospital.admin_service.external.dto.user.CreateUserForDoctorRequest;
import com.hospital.admin_service.external.dto.user.RoleRequest;
import com.hospital.admin_service.external.port.IAuthUserClient;
import com.hospital.admin_service.external.port.IConsultingClient;
import com.hospital.admin_service.model.Doctor;
import com.hospital.admin_service.model.Specialty;
import com.hospital.admin_service.repo.DoctorRepository;
import com.hospital.admin_service.repo.SpecialtyRepository;
import jakarta.persistence.OptimisticLockException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class DoctorWriteService {

    private final DoctorRepository repository;
    private final SpecialtyRepository specialtyRepository;
    private final IAuthUserClient authUserClient;
    private final IConsultingClient consultingClient;

    @Transactional
    public Doctor registerDoctor(DoctorRegisterRequest req) {
        Long createdUserId = null;

        try {
            var body = new CreateUserForDoctorRequest(
                    req.username(),
                    req.password(),
                    req.gender(),
                    req.firstName(),
                    req.lastName(),
                    req.centerId(),
                    Set.of(new RoleRequest("DOCTOR"))
            );
            var user = authUserClient.register(body);
            createdUserId = user.id();

            var doctor = new Doctor();
            doctor.setUserId(createdUserId);
            return this.create(doctor, req.specialtyId());

        } catch (ResponseStatusException rse) {
            compensateDeleteUser(createdUserId);
            throw rse;
        } catch (Exception ex) {
            compensateDeleteUser(createdUserId);
            throw new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR, "Error inesperado al registrar al doctor.", ex);
        }
    }

    private void compensateDeleteUser(@Nullable Long userId) {
        if (userId == null) return;
        try {
            authUserClient.deleteUser(userId, true);
        } catch (ResponseStatusException ex) {
            if (ex.getStatusCode().value() == 404) return;
        } catch (Exception ignored) { }
    }

    @Transactional
    public Doctor create(Doctor entity, @Nullable Long specialtyId) {
        if (repository.existsByUserId(entity.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe un Doctor activo para el userId especificado.");
        }
        if (!authUserClient.existsUserById(entity.getUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el usuario servicio de autenticación.");
        }
        entity.setSpecialty(resolveSpecialtyOrNull(specialtyId));
        return repository.save(entity);
    }

    @Transactional
    public Doctor update(Long id, Doctor incoming, @Nullable Long specialtyId) {
        Doctor current = repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor no encontrado."));

        Long newUserId = incoming.getUserId();
        if (newUserId != null && !newUserId.equals(current.getUserId())) {
            if (repository.existsByUserIdAndIdNot(newUserId, id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe otro Doctor activo con ese userId.");
            }
            if (!authUserClient.existsUserById(newUserId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "No existe el usuario en el servicio de autenticación.");
            }
            current.setUserId(newUserId);
        }

        current.setSpecialty(resolveSpecialtyOrNull(specialtyId));
        try {
            return repository.save(current);
        } catch (OptimisticLockException e) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El Doctor fue modificado por otro proceso.", e);
        }
    }

    @Transactional
    public Doctor updateWithPessimisticLock(Long id, Doctor incoming, @Nullable Long specialtyId) {
        Doctor current = repository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor no encontrado."));

        Long newUserId = incoming.getUserId();
        if (newUserId != null && !newUserId.equals(current.getUserId())) {
            if (repository.existsByUserIdAndIdNot(newUserId, id)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Ya existe otro Doctor activo con ese userId.");
            }
            if (!authUserClient.existsUserById(newUserId)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "El nuevo userId no existe en el servicio de autenticación.");
            }
            current.setUserId(newUserId);
        }

        current.setSpecialty(resolveSpecialtyOrNull(specialtyId));
        return repository.save(current);
    }

    @Transactional
    public void softDelete(Long id) {
        Doctor current = repository.lockById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Doctor no encontrado."));

        if (consultingClient.hasFutureAppointments(id)) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "El doctor tiene consultas futuras y no puede ser eliminado.");
        }

        try {
            authUserClient.deleteUser(current.getUserId());
            repository.delete(current);
        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception ex) {
            log.error("[DoctorWriteService] Error inesperado en softDelete: {}", ex.getMessage(), ex);
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Error comunicándose con servicios externos.", ex);
        }
    }

    private Specialty resolveSpecialtyOrNull(@Nullable Long specialtyId) {
        if (specialtyId == null) return null;
        return specialtyRepository.findById(specialtyId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "La especialidad especificada no existe."));
    }
}
