package com.hospital.services;

import com.hospital.dtos.CreateUserRequest;
import com.hospital.dtos.UpdatePasswordRequest;
import com.hospital.dtos.UpdateUserRequest;
import com.hospital.entities.User;

public interface UserService {
     User register(CreateUserRequest request);
     User findUserByDni(String email);
     User findUserById(Long id);
     User findUserByCenterId(Long centerId, boolean includeDisabled);
     boolean existsUserByCenterId(Long centerId, boolean includeDisabled);
     User update(Long id, UpdateUserRequest request);
     void updatePassword(Long id, UpdatePasswordRequest request);
     void disableUser(Long id);
     void hardDeleteUser(Long id);
     User findByUsername(String username);
}
