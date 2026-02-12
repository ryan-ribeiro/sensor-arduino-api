package com.github.ryanribeiro.sensor.infra;

import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import com.github.ryanribeiro.sensor.exceptions.AccessDeniedException;
import com.github.ryanribeiro.sensor.exceptions.EventoNoFuturoException;
import com.github.ryanribeiro.sensor.exceptions.FrequenciaException;
import com.github.ryanribeiro.sensor.exceptions.GenericException;
import jakarta.validation.ConstraintViolationException;

@ControllerAdvice
public class RestExceptionHandler extends ResponseEntityExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(RestExceptionHandler.class);
    @ExceptionHandler(EventoNoFuturoException.class)
    private ResponseEntity<RestErrorMessage> EventoNoFuturoHandler(EventoNoFuturoException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }

    @ExceptionHandler(FrequenciaException.class)
    private ResponseEntity<RestErrorMessage> FrequenciaExceptionHandler(FrequenciaException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }  

    @ExceptionHandler(GenericException.class)
    private ResponseEntity<RestErrorMessage> GenericExceptionHandler(GenericException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(errorMessage);
    }

    @ExceptionHandler(IllegalStateException.class)
    private ResponseEntity<RestErrorMessage> IllegalStateExceptionHandler(IllegalStateException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.NOT_FOUND, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(errorMessage);
    }

    @ExceptionHandler(AccessDeniedException.class)
    private ResponseEntity<RestErrorMessage> AccessDeniedHandler(AccessDeniedException ex) {
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.FORBIDDEN, ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(errorMessage);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    private ResponseEntity<RestErrorMessage> ConstraintViolationHandler(ConstraintViolationException ex) {
        String details = ex.getConstraintViolations()
                .stream()
                .map(cv -> cv.getPropertyPath() + ": " + cv.getMessage())
                .collect(Collectors.joining("; "));
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, "Constraint violations: " + details);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RestErrorMessage> GenericExceptionHandler(Exception ex) {
        logger.error("Unhandled exception caught by RestExceptionHandler", ex);
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.INTERNAL_SERVER_ERROR, "Ocorreu um erro inesperado: " + ex.getMessage());
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorMessage);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<RestErrorMessage> BadRequestExceptionHandler(IllegalArgumentException ex) {
        // Log para confirmar captura
        System.err.println("RestExceptionHandler caught IllegalArgumentException: " + ex.getMessage());
        RestErrorMessage errorMessage = new RestErrorMessage(HttpStatus.BAD_REQUEST, ex.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(errorMessage);
    }
}
