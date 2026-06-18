package com.hospital.admin_service.external.impl;

import com.hospital.admin_service.external.dto.user.CreateUserForDoctorRequest;
import com.hospital.admin_service.external.dto.user.UserResponse;
import com.hospital.admin_service.external.port.IAuthUserClient;
import com.hospital.admin_service.external.feign.AuthFeignClient;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;

import feign.FeignException;
import org.springframework.http.ResponseEntity;
import org.springframework.http.HttpStatus;

@Component
@RequiredArgsConstructor
public class AuthUserClientFeign implements IAuthUserClient {

    private final AuthFeignClient feign;

    private static boolean is2xx(ResponseEntity<?> resp) {
        return resp != null && resp.getStatusCode().is2xxSuccessful();
    }

    @Override
    public UserResponse register(CreateUserForDoctorRequest body) {
        var resp = feign.register(body);
        if (resp == null || resp.getBody() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY,
                    "Cuerpo vacío desde auth-service al registrar");
        }
        return resp.getBody();
    }

    @Override
    public UserResponse getUserById(Long id, boolean enabled) {
        try {
            var resp = feign.getUserById(id, enabled);
            if (!is2xx(resp) || resp.getBody() == null) {
                throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado con id: " + id);
            }
            return resp.getBody();
        } catch (FeignException.NotFound e) {
            // por si en tu versión aún lanza
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Usuario no encontrado por id: " + id, e);
        }
    }

    @Override
    public void deleteUser(Long id) { feign.deleteUser(id, false); }

    @Override
    public void deleteUser(Long id, boolean hard) { feign.deleteUser(id, hard); }

    @Override
    public boolean existsUserById(Long id) {
        var resp = feign.getUserById(id, true);
        return is2xx(resp) && resp.getBody() != null;
    }

    @Override
    public boolean hasActiveUsersInCenter(Long centerId) {
        var resp = feign.existsUserByCenterId(centerId, true);
        return is2xx(resp);
    }
}
