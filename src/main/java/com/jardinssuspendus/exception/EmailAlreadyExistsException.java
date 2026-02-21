package com.jardinssuspendus.exception;

public class EmailAlreadyExistsException extends RuntimeException {
    
    public EmailAlreadyExistsException(String message) {
        super(message);
    }

    public EmailAlreadyExistsException(String email, boolean formatted) {
        super(String.format("Un compte avec l'email '%s' existe déjà", email));
    }
}