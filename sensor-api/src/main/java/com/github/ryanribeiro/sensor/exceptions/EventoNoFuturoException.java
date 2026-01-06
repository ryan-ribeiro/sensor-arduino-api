package com.github.ryanribeiro.sensor.exceptions;

public class EventoNoFuturoException extends RuntimeException{
    public EventoNoFuturoException() {
        super("O WiFi caiu e um evento que tentou ser cadastrado estava no futuro.");
    }
    
    public EventoNoFuturoException(String message) {
        super(message);
    }

    public EventoNoFuturoException(String message, Throwable cause) {
        super(message, cause);
    }
    
}
