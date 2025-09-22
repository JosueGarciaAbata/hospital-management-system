package com.hospital.admin_service.exception;

import lombok.Getter;

import java.util.Map;

@Getter

public class RemoteFieldValidationException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    private final Map<String, String> errors;

    public RemoteFieldValidationException(Map<String, String> errors) {
        super("Remote field validation error");
        this.errors = (errors == null) ? Map.of() : Map.copyOf(errors);
    }

}
