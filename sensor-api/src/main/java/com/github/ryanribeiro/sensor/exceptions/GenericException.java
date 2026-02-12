package com.github.ryanribeiro.sensor.exceptions;

public class GenericException extends RuntimeException {
    public GenericException() {
        super("Ocorreu um erro genérico.");
    }
    
    public GenericException(String message) {
        super(message);
    }

    public GenericException(String message, Throwable cause) {
        super(message, cause);
    }
}
