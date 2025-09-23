package com.hospital.exceptions;

public class UserByDniNotFoundException extends RuntimeException {

    public UserByDniNotFoundException(String message) {
        super(message);
    }
}
