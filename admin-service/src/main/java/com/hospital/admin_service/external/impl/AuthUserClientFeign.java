package com.hospital.admin_service.external.impl;

import com.hospital.admin_service.external.dto.user.CreateUserForDoctorRequest;
import com.hospital.admin_service.external.dto.user.UserResponse;
import com.hospital.admin_service.external.port.IAuthUserClient;
import com.hospital.admin_service.external.feign.AuthFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

@Component
@RequiredArgsConstructor
public class AuthUserClientFeign implements IAuthUserClient {

    private final AuthFeignClient feign;

    @Override
    public UserResponse register(CreateUserForDoctorRequest body) {
        var resp = feign.register(body);
        if (resp == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "Cuerpo vacío desde auth-service al registrar");
        }
        return resp.getBody();
    }

    @Override
    public UserResponse getUserById(Long id) {
        var resp = feign.getUserById(id);
        if (resp == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado");
        }
        return resp.getBody();
    }

    @Override
    public void deleteUser(Long id) {
        feign.deleteUser(id, false);
    }

    @Override
    public void deleteUser(Long id, boolean hard) {
        feign.deleteUser(id, hard);
    }

    @Override
    public boolean existsUserById(Long id) {
        return feign.getUserById(id) != null;
    }

    @Override
    public boolean hasActiveUsersInCenter(Long centerId) {
        return feign.existsUserByCenterId(centerId, true) != null;
    }
}
