package com.github.ryanribeiro.sensor.dto;

public record UpdateUserDTO(
    String username,
    String local,
    String arduino
) {
    
}