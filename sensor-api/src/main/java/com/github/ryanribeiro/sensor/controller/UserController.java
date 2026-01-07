package com.github.ryanribeiro.sensor.controller;
import java.util.List;
import com.github.ryanribeiro.sensor.services.UserServices;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.CreateUserDTO;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
public class UserController {

    @Autowired
    private UserServices userServices;

    @PostMapping("/users")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDTO createUserDTO) {
        userServices.createUser(createUserDTO);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        return ResponseEntity.ok(userServices.getAllUsers());
    }
}
