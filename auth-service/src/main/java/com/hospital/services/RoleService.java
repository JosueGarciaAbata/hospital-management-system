package com.hospital.services;

import com.hospital.entities.Role;

public interface RoleService {

    Role findRoleByDni(String email);
}
