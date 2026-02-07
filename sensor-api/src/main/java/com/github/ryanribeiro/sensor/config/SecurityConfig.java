package com.github.ryanribeiro.sensor.config;

import java.io.IOException;
import java.security.KeyFactory;
import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import org.springframework.core.io.Resource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ResourceLoader;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${jwt.public.key:}")
    private String jwtPublicKeyLocation;

    @Value("${jwt.private.key:}")
    private String jwtPrivateKeyLocation;

    private final ResourceLoader resourceLoader;

    private RSAPublicKey publicKey;
    private RSAPrivateKey privateKey;

    public SecurityConfig(
        ResourceLoader resourceLoader,
        @Value("${jwt.public.key:}") String jwtPublicKeyLocation,
        @Value("${jwt.private.key:}") String jwtPrivateKeyLocation
    ) {
        this.resourceLoader = resourceLoader;
        this.jwtPublicKeyLocation = jwtPublicKeyLocation;
        this.jwtPrivateKeyLocation = jwtPrivateKeyLocation;
        loadKeyPair();
    }

    private void loadKeyPair() {
        if (jwtPublicKeyLocation == null || jwtPublicKeyLocation.isBlank()) {
            throw new IllegalStateException("jwt.public.key is not configured");
        }
        if (jwtPrivateKeyLocation == null || jwtPrivateKeyLocation.isBlank()) {
            throw new IllegalStateException("jwt.private.key is not configured");
        }
        try {
            byte[] publicBytes = readResourceBytes(jwtPublicKeyLocation);
            byte[] privateBytes = readResourceBytes(jwtPrivateKeyLocation);

            KeyFactory keyFactory = KeyFactory.getInstance("RSA");
            this.publicKey = (RSAPublicKey) keyFactory.generatePublic(
                new X509EncodedKeySpec(publicBytes)
            );
            this.privateKey = (RSAPrivateKey) keyFactory.generatePrivate(
                new PKCS8EncodedKeySpec(privateBytes)
            );
        } catch (Exception e) {
            throw new IllegalStateException("Unable to load RSA keys", e);
        }
    }

    private byte[] readResourceBytes(String location) throws IOException {
        Resource resource = resourceLoader.getResource(location);
        if (!resource.exists()) {
            throw new IllegalStateException("Key resource not found: " + location);
        }
        try (var inputStream = resource.getInputStream()) {
            return inputStream.readAllBytes();
        }
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http
                .authorizeHttpRequests(authorize -> authorize
                    .requestMatchers(HttpMethod.POST, "/login").permitAll()
                    .requestMatchers(HttpMethod.POST, "/users").permitAll()
                    .requestMatchers(HttpMethod.GET, "/admin/**").hasAuthority("SCOPE_ADMIN")
                    .anyRequest().authenticated())
                .csrf(csrf -> csrf.disable())
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(Customizer.withDefaults()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));
        return http.build();
    }

    @Bean
    public JwtEncoder jwtEncoder() {
        JWK jwk = new RSAKey.Builder(this.publicKey).privateKey(privateKey).build();
        var jwks = new ImmutableJWKSet<>(new JWKSet(jwk));
        return new NimbusJwtEncoder(jwks);
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        return NimbusJwtDecoder.withPublicKey(publicKey).build();
    }

    @Bean
    public BCryptPasswordEncoder bCryptPasswordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
