package com.hospital.repositories;

import com.hospital.entities.Role;

import java.util.Optional;

public interface RoleRepository {
    Optional<Role> findByName(String name);
}
