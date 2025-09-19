package com.hospital.rests;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
public class UserController {

    // Si falta un header, devuelve un error de que no hay permisos.
    @GetMapping("/test")
    public String test(
            @RequestHeader("X-User-Id") String userId,
            @RequestHeader("X-Roles") String roles,
            @RequestHeader("X-Center-Id") String centerId
    ) {
        return String.format("UserId: %s, Roles: %s, CenterId: %s", userId, roles, centerId);
    }
}
