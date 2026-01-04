package com.github.ryanribeiro.sensor.controller;
import java.util.List;
import java.util.Set;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.domain.Role;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.CreateUserDTO;
import com.github.ryanribeiro.sensor.repository.UserRepository;
import com.github.ryanribeiro.sensor.repository.RoleRepository;

import org.springframework.web.bind.annotation.RequestBody;
import jakarta.transaction.Transactional;

@RestController
public class UserController {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController(UserRepository userRepository, RoleRepository roleRepository, BCryptPasswordEncoder bCryptPasswordEncoder) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/users")
    @Transactional
    public ResponseEntity<Void> createUser(@RequestBody CreateUserDTO createUserDTO) {
        if (createUserDTO == null || createUserDTO.username() == null || createUserDTO.password() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        var userFromDb = userRepository.findByUsername(createUserDTO.username());
        if (userFromDb.isPresent()) {
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).build();
        }

        User user = new User();
        user.setUsername(createUserDTO.username());
        user.setPassword(bCryptPasswordEncoder.encode(createUserDTO.password()));
        Object roleObj = roleRepository.findByName(Role.Values.ROLE_USER.name());
        Role role = roleObj instanceof Role ? (Role) roleObj : new Role(Role.Values.ROLE_USER.name());
        user.setRoles(Set.of(role));
        
        userRepository.save(user);

        return ResponseEntity.ok().build();
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<User>> getAllUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok(users);
    }
}
