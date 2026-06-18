package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Petición para actualizar la contraseña del usuario autenticado")
public class UpdatePasswordRequest {

    @Schema(description = "Nueva contraseña", example = "PassActualizado456!")
    @JsonProperty("new_password")
    private String newPassword;
}
