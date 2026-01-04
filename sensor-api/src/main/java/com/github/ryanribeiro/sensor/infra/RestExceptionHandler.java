package com.github.ryanribeiro.sensor.infra;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import com.github.ryanribeiro.sensor.exceptions.EventoNoFuturoException;
import com.github.ryanribeiro.sensor.exceptions.FrequenciaException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    @ExceptionHandler(EventoNoFuturoException.class)
    private ResponseEntity<RestErrorMessage> eventoNoFuturoHandler(EventoNoFuturoException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
    }  

    @ExceptionHandler(FrequenciaException.class)
    private ResponseEntity<RestErrorMessage> frequenciaExceptionHandler(FrequenciaException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }  

    @ExceptionHandler(Exception.class)
    private ResponseEntity<RestErrorMessage> genericExceptionHandler(Exception ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado: " + ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    private ResponseEntity<RestErrorMessage> badRequestExceptionHandler(IllegalArgumentException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }
}
