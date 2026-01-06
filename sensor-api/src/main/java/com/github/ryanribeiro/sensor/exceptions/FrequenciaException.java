package com.github.ryanribeiro.sensor.exceptions;

public class FrequenciaException extends RuntimeException{
    public FrequenciaException() {
        super("frequenciaEmMillissegundos menor ou igual a zero.");
    }
    
    public FrequenciaException(String message) {
        super(message);
    }

    public FrequenciaException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
