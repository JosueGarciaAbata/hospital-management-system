package com.hospital.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Petición para solicitar restablecimiento de contraseña")
public class RequestPasswordRequest {

    @Schema(description = "Correo electrónico o DNI del usuario", example = "usuario@correo.com")
    private String input; // Puede ser correo o DNI
}
