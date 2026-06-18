package com.hospital.services;

import com.hospital.entities.Role;
import com.hospital.exceptions.RoleNotFoundException;
import com.hospital.repositories.RoleRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class RoleServiceImp implements RoleService {

    private final RoleRepository repository;

    @Override
    public Role findRoleByDni(String email) {
        return this.repository.findByName(email).orElseThrow(()->new RoleNotFoundException("Role not found"));
    }
}
