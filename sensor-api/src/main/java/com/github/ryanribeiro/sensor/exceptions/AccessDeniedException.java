package com.github.ryanribeiro.sensor.exceptions;

public class AccessDeniedException extends RuntimeException {
    public AccessDeniedException() {
        super("Acesso negado.");
    }
    
    public AccessDeniedException(String message) {
        super(message);
    }

    public AccessDeniedException(String message, Throwable cause) {
        super(message, cause);
    }
}
