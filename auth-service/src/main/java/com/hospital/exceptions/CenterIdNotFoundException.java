package com.hospital.exceptions;

public class CenterIdNotFoundException extends RuntimeException{

    public CenterIdNotFoundException(String message){
        super(message);
    }
}
