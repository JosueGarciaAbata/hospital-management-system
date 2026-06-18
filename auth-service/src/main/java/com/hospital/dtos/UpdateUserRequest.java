package com.hospital.dtos;

import com.fasterxml.jackson.annotation.JsonProperty;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Schema(description = "Petición para actualizar los datos de un usuario existente")
public class UpdateUserRequest {

    @Schema(description = "Nombre del usuario", example = "Carlos")
    @JsonProperty("first_name")
    private String firstName;

    @Schema(description = "Apellido del usuario", example = "García")
    @JsonProperty("last_name")
    private String lastName;

    @Schema(description = "Género del usuario", example = "FEMALE")
    private String gender;

}
