package com.github.ryanribeiro.sensor.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.dto.LoginRequestDTO;
import com.github.ryanribeiro.sensor.services.TokenServices;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import java.util.Map;


@RestController
public class TokenController {

    @Autowired
    private TokenServices tokenServices;
    
    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        System.out.println("🔐 /login endpoint called");
        System.out.println("  Username: " + loginRequestDTO.username());
        
        Object response = tokenServices.loginService(loginRequestDTO);
        System.out.println("  Response type: " + response.getClass().getSimpleName());
        System.out.println("  Response: " + response);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/debug/echo")
    public ResponseEntity<Map<String, Object>> debugEcho() {
        return ResponseEntity.ok(Map.of(
            "message", "API is responding",
            "timestamp", System.currentTimeMillis(),
            "status", "OK"
        ));
    }
}

