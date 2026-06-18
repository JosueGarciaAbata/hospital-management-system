package com.hospital.exceptions;

public class EmailTemplateException extends RuntimeException {
    public EmailTemplateException(String message) {
        super(message);
    }
}
