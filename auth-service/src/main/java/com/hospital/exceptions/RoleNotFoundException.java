package com.hospital.exceptions;

public class RoleNotFoundException extends RuntimeException{
    public RoleNotFoundException(String s) {
        super(s);
    }
}
