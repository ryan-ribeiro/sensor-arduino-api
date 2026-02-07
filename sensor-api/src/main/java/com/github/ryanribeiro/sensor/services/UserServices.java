package com.github.ryanribeiro.sensor.services;

import java.util.List;
import java.util.Set;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.ryanribeiro.sensor.domain.Role;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.CreateUserDTO;
import com.github.ryanribeiro.sensor.repository.RoleRepository;
import com.github.ryanribeiro.sensor.repository.UserRepository;

@Service
public class UserServices {
    
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public Void createUser(CreateUserDTO createUserDTO) {
        if (createUserDTO == null) {
            throw new IllegalArgumentException("Invalid user data");
        }
        if (createUserDTO.username() == null) {
            throw new IllegalArgumentException("Username cannot be null");
        }
        if (createUserDTO.username().isBlank()) {
            throw new IllegalArgumentException("Username cannot be blank");
        }
        if (createUserDTO.password() == null) {
            throw new IllegalArgumentException("Password cannot be null");
        }
        if(createUserDTO.password().isBlank()) {
            throw new IllegalArgumentException("Password cannot be blank");
        }
        if(createUserDTO.password().length() < 6) {
            throw new IllegalArgumentException("Password must be at least 6 characters long");
        }
        var userFromDb = userRepository.findByUsername(createUserDTO.username());
        if (userFromDb.isPresent()) {
            throw new IllegalStateException("User already exists");
        }

        User user = new User();
        BeanUtils.copyProperties(createUserDTO, user);
        Role role = roleRepository.findByName(Role.Values.ROLE_USER.name());
        if (role == null) {
            role = roleRepository.save(new Role(Role.Values.ROLE_USER.name()));
        }
        user.setRoles(Set.of(role));
        user.setPassword(bCryptPasswordEncoder.encode(createUserDTO.password()));
        
        userRepository.save(user);
        return null;
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
