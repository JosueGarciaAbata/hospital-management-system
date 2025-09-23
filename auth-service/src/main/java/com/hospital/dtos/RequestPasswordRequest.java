package com.hospital.dtos;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RequestPasswordRequest {

    private String input; // Puede ser correo o DNI
}
