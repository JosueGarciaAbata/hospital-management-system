package com.hospital.exceptions;

public class SelfDeletionNotAllowedException extends RuntimeException {
    public SelfDeletionNotAllowedException(String mes) {
        super(mes);
    }
}
