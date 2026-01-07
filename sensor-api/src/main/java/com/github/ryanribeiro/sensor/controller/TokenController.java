package com.github.ryanribeiro.sensor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.dto.LoginRequestDTO;
import com.github.ryanribeiro.sensor.services.TokenServices;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class TokenController {

    @Autowired
    private TokenServices tokenServices;
    
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        return ResponseEntity.ok(tokenServices.loginService(loginRequestDTO));
    }
    
}
