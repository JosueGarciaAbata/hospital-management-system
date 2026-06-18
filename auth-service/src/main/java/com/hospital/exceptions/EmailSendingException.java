package com.hospital.exceptions;

public class EmailSendingException extends RuntimeException {

    public EmailSendingException(String message) {
        super(message);
    }
}
