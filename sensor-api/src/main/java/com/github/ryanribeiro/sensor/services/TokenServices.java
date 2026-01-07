package com.github.ryanribeiro.sensor.services;

import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;

import com.github.ryanribeiro.sensor.domain.Role;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.LoginRequestDTO;
import com.github.ryanribeiro.sensor.dto.LoginResponseDTO;
import com.github.ryanribeiro.sensor.repository.UserRepository;

@Service
public class TokenServices {
    @Autowired
    private JwtEncoder jwtEncoder;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public LoginResponseDTO loginService(LoginRequestDTO loginRequestDTO) {
        Optional<User> user = userRepository.findByUsername(loginRequestDTO.username());
        if (user.isEmpty() || !user.get().isPasswordValid(loginRequestDTO, bCryptPasswordEncoder)) {
            throw new RuntimeException("user or password is invalid");
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
        
        return new LoginResponseDTO(tokenJwt, expiresIn.toEpochMilli());
    }
}
