package com.hospital.admin_service.rest;

import com.hospital.admin_service.security.filters.RequireRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/admin")
@Tag(name = "Salud del Sistema", description = "Endpoints para verificar el estado del servicio de administración")
public class HealthController {

    @GetMapping("/ping")
    @RequireRole({"ADMIN"}) // sólo deja pasar si X-Roles contiene ADMIN
    @Operation(
            summary = "Comprobar estado del servicio",
            description = "Devuelve una respuesta PONG junto con el ID de usuario y centro para verificar conectividad y seguridad."
    )
    public String ping(
            @Parameter(description = "Identificador del usuario que realiza la petición", example = "101")
            @RequestHeader("X-User-Id") String userId,
            @Parameter(description = "Identificador del centro médico asociado al usuario", example = "5")
            @RequestHeader("X-Center-Id") String centerId) {
        return "PONG - OK - user=" + userId + " center=" + centerId;
    }
}
