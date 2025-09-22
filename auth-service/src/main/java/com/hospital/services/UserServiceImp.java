package com.hospital.services;

import com.hospital.dtos.CreateUserRequest;
import com.hospital.dtos.UpdatePasswordRequest;
import com.hospital.dtos.UpdateUserRequest;
import com.hospital.entities.Role;
import com.hospital.entities.User;
import com.hospital.enums.GenderType;
import com.hospital.exceptions.CenterIdNotFoundException;
import com.hospital.exceptions.DniAlreadyExistsException;
import com.hospital.exceptions.ServiceUnavailableException;
import com.hospital.exceptions.UserNotFoundException;
import com.hospital.feign.AdminServiceWrapper;
import com.hospital.mappers.UserMapper;
import com.hospital.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImp implements UserService {

    private final UserRepository repository;
    private final AdminServiceWrapper wrapper;
    private final RoleService roleService;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper mapper;

    @Override
    public User register(CreateUserRequest request) {
        User user = mapper.toUser(request);

        if (repository.existsByUsername(user.getUsername())) {
            throw new DniAlreadyExistsException("Ya existe un usuario con DNI " + user.getUsername());
        }

        Long centerId = user.getCenterId();
        ResponseEntity<Void> response = wrapper.validateCenterId(centerId);
        if (response.getStatusCode().is5xxServerError()) {
            throw new ServiceUnavailableException("El servicio de administración no está disponible en este momento. Intente nuevamente más tarde.");
        }
        if (response.getStatusCode().is4xxClientError()) {
            throw new CenterIdNotFoundException("El ID del centro no existe: " + centerId);
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        Set<Role> savedRoles = user.getRoles().stream()
                .map(role -> roleService.findRoleByDni(role.getName()))
                .collect(Collectors.toSet());
        user.setRoles(savedRoles);

        return repository.save(user);
    }

    @Override
    public User findUserByDni(String dni) {
        return repository.findByUsername(dni)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con DNI: " + dni));
    }

    @Override
    public User findUserById(Long id) {
        return repository.findUserById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));
    }

    @Override
    public User findUserByCenterId(Long centerId, boolean includeDisabled) {
        if (includeDisabled) {
            return repository.findFirstByCenterId(centerId)
                    .orElseThrow(() -> new UserNotFoundException(
                            "Usuario (habilitado o no) no encontrado para Centro Médico ID: " + centerId));
        }
        return repository.findFirstByCenterIdAndEnabledTrue(centerId)
                .orElseThrow(() -> new UserNotFoundException(
                        "Usuario habilitado no encontrado para Centro Médico ID: " + centerId));
    }

    @Override
    public boolean existsUserByCenterId(Long centerId, boolean includeDisabled) {
        return includeDisabled
                ? repository.existsByCenterId(centerId)
                : repository.existsByCenterIdAndEnabledTrue(centerId);
    }

    @Override
    public User update(Long id, UpdateUserRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(GenderType.valueOf(request.getGender()));
        user.setCenterId(request.getCenterId());
        return repository.save(user);
    }

    @Override
    public void updatePassword(Long id, UpdatePasswordRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado con ID: " + id));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("La contraseña anterior es incorrecta.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        user.setEnabled(false);
        repository.save(user);
    }

    @Override
    @Transactional
    public void hardDeleteUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("Usuario no encontrado"));
        repository.hardDeleteById(user.getId());
    }
}

