package com.hospital.admin_service.rest;

import com.hospital.admin_service.security.filters.RequireRole;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
public class AdminController {

    @GetMapping("/ping")
    @RequireRole({"ADMIN"}) // sólo deja pasar si X-Roles contiene ADMIN
    public String ping(@RequestHeader("X-User-Id") String userId,
                       @RequestHeader("X-Center-Id") String centerId) {
        return "PONG - OK - user=" + userId + " center=" + centerId;
    }
}
