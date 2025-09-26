package com.hospital.admin_service.external.port;

import com.hospital.admin_service.external.dto.user.CreateUserForDoctorRequest;
import com.hospital.admin_service.external.dto.user.UserResponse;

public interface IAuthUserClient {
    UserResponse register(CreateUserForDoctorRequest body);
    UserResponse getUserById(Long id, boolean enable);
    void deleteUser(Long id);
    void deleteUser(Long id, boolean hard);
    boolean existsUserById(Long id);
    boolean hasActiveUsersInCenter(Long centerId);
}
