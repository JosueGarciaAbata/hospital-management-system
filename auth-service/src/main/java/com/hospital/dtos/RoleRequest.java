package com.hospital.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Representa un rol asignado al usuario")
public class RoleRequest {

    @Schema(description = "Nombre del rol", example = "ADMIN")
    private String name;
}
