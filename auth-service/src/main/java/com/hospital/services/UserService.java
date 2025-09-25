package com.hospital.services;

import com.hospital.dtos.CreateUserRequest;
import com.hospital.dtos.UpdatePasswordRequest;
import com.hospital.dtos.UpdateUserRequest;
import com.hospital.dtos.UserResponse;
import com.hospital.entities.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface UserService {
     Page<UserResponse> findAll(Pageable pageable, boolean includeDeleted);
     Page<UserResponse> findAllExludingUser(Long userId, Pageable pageable, boolean includeDeleted);
     List<User> findAllTesting();
     User register(CreateUserRequest request);
     User findUserByDni(String email);
     User findUserById(Long id);
     User findUserByCenterId(Long centerId, boolean includeDisabled);
     boolean existsUserByCenterId(Long centerId, boolean includeDisabled);
     User update(Long id, UpdateUserRequest request);
     void updatePassword(Long id, String newPass);
     void disableUser(Long id);
     void hardDeleteUser(Long id);
     User findByUsername(String username);
     void validateDoctorAssigned(Long id);
}
