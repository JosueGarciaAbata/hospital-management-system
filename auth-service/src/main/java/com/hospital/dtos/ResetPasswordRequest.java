package com.hospital.dtos;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Petición para aplicar un restablecimiento de contraseña")
public class ResetPasswordRequest {

    @Schema(description = "Token de restablecimiento recibido en el correo", example = "abcd1234efgh5678")
    private String token;

    @Schema(description = "Nueva contraseña a establecer", example = "NuevoPass123!")
    private String newPassword;
}
