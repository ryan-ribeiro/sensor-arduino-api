package com.github.ryanribeiro.sensor.controller;

import java.util.Optional;
import java.util.stream.Collectors;
import java.util.Map;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.domain.Role;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.LoginRequestDTO;
import com.github.ryanribeiro.sensor.dto.LoginResponseDTO;
import com.github.ryanribeiro.sensor.repository.UserRepository;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController
public class TokenController {

    private final JwtEncoder jwtEncoder;

    private final UserRepository userRepository;

    private final BCryptPasswordEncoder bCryptPasswordEncoder;

    private static final Logger logger = LoggerFactory.getLogger(TokenController.class);

    public TokenController(JwtEncoder jwtEncoder,
            UserRepository userRepository,
            BCryptPasswordEncoder bCryptPasswordEncoder,
            UserController userController) {
        this.jwtEncoder = jwtEncoder;
        this.userRepository = userRepository;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
    }

    @PostMapping("/login")
    public ResponseEntity<Object> login(@RequestBody LoginRequestDTO loginRequestDTO) {
        Optional<User> user = userRepository.findByUsername(loginRequestDTO.username());
        if (user.isEmpty() || !user.get().isPasswordValid(loginRequestDTO, bCryptPasswordEncoder)) {
            logger.warn("Invalid login attempt for username={}", loginRequestDTO.username());
            return ResponseEntity.status(401).body(Map.of("error", "user or password is invalid"));
        }

        var expiresIn = java.time.Instant.now().plusSeconds(54000L);

        var scopes = user.get().getRoles().stream()
            .map(Role::getName)
            .filter(Objects::nonNull)
            .map(s -> s.replaceFirst("^ROLE_", ""))
            .collect(Collectors.joining(" "));

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("sensor-api")
                .subject(user.get().getUserId().toString())
                .expiresAt(expiresIn)
                .issuedAt(java.time.Instant.now())
                .claim("scope", scopes)
                .build();

        var tokenJwt = jwtEncoder.encode(
                JwtEncoderParameters.from((JwtClaimsSet) claims)
                )
                .getTokenValue();

        return ResponseEntity.ok(new LoginResponseDTO(tokenJwt, expiresIn.toEpochMilli()));
    }
    
}
