package com.hospital.exceptions;

public class UserNotFoundException extends RuntimeException{
    private static final String DEFAULT_MESSAGE = "User not found with ID: ";

    public UserNotFoundException(Long id) {
        super(DEFAULT_MESSAGE + id);
    }
}
