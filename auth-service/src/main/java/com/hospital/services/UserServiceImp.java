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

        boolean exists = this.repository.existsByUsername(user.getUsername());
        if (exists) {
            throw new DniAlreadyExistsException("User with DNI " + user.getUsername() + " already exists.");
        }

        // Antes de guardar, ver que ese centroId exista en el microservicio de administracion.
        Long centerId = user.getCenterId();

        ResponseEntity<Void> response = wrapper.validateCenterId(centerId);

        if (response.getStatusCode().is5xxServerError()) {
            throw new ServiceUnavailableException("Admin service is currently unavailable. Please try again later.");
        }

        if (response.getStatusCode().is4xxClientError()) {
            throw new CenterIdNotFoundException("Center ID does not exist: " + centerId);
        }

        // Encriptando la contrasena.
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        // Validando los roles.
        Set<Role> savedRoles = user.getRoles().stream().map(role -> roleService.findRoleByDni(role.getName())).collect(Collectors.toSet());
        user.setRoles(savedRoles);

        return this.repository.save(user);
    }

    @Override
    public User findUserByDni(String dni) {
        return this.repository.findByUsername(dni).orElseThrow(() -> new UserNotFoundException("User not found with DNI: " + dni));
    }

    @Override
    public User findUserById(Long id) {
        return this.repository.findUserById(id).orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));
    }

    @Override
    public User update(Long id, UpdateUserRequest request) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setGender(GenderType.valueOf(request.getGender()));
        user.setCenterId(request.getCenterId());

        return repository.save(user);
    }

    @Override
    public void updatePassword(Long id, UpdatePasswordRequest request) {

        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found with ID: " + id));

        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Old password is incorrect.");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        repository.save(user);
    }

    @Override
    public void disableUser(Long id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        user.setEnabled(false);
        repository.save(user);
    }
}
