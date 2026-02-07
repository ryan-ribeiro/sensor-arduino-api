package com.github.ryanribeiro.sensor.controller;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.github.ryanribeiro.sensor.dto.BipeDTO;
import com.github.ryanribeiro.sensor.services.BipeServices;

@Controller
@RequestMapping("bipes")
public class BipeController {

    @Autowired
    private BipeServices bipeServices;
    //TODO: retirar senderId e receiverId como variáveis obrigatórias de serem passadas
    // Assume a chave composta local + arduino
    
    @PostMapping("/enviarBipe")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED)
    public ResponseEntity<Object> enviarBipe(@RequestBody BipeDTO bipeDTO, JwtAuthenticationToken token) {
        BipeDTO bipeSaved;
        if (token.getName() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido: userId não encontrado.");
        }
        
        bipeDTO.setSenderId(UUID.fromString(token.getName()));

        if (token.getName() == null || bipeDTO.getReceiverId() == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("SenderId ou ReceiverId não pode ser nulo.");
        }

        bipeSaved = bipeServices.enviarBipe(bipeDTO);

        return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(bipeSaved);
    }

    @GetMapping("/ultimo-bipe")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ResponseEntity<String> getLastBipe(
        @RequestParam String receiverId,
        @RequestParam String arduino,
        @RequestParam String local,
        JwtAuthenticationToken token	
    ) {
        BipeDTO bipe = bipeServices.getLastBipe(
            token.getName(),
            receiverId,
            local,
            arduino
        );
        if (bipe == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        return ResponseEntity.ok(bipe.getMensagem());
    }

    @GetMapping("/id-ultimo-bipe")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ResponseEntity<String> getLastBipeId(
        @RequestParam String receiverId,
        @RequestParam String local,
        @RequestParam String arduino,
        JwtAuthenticationToken token	
    ) {
        String id = bipeServices.getLastBipeId(
            token.getName(), 
            receiverId,
            local, 
            arduino
        );
        if (id == null) {
            throw new RuntimeException("Nenhum bipe encontrado para os parâmetros fornecidos.");
        }
        return ResponseEntity.ok(id);
    }
    
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('SCOPE_USER')")
    @Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
    public ResponseEntity<BipeDTO> getBipeById(
                @PathVariable String id,
                JwtAuthenticationToken token
    ) {
        try {
            BipeDTO bipe = bipeServices.getBipeById(id, token.getName());
            return ResponseEntity.ok(bipe);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }
    
}