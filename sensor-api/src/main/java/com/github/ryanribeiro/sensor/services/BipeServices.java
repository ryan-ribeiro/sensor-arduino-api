package com.github.ryanribeiro.sensor.services;

import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ryanribeiro.sensor.domain.Bipe;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.BipeDTO;
import com.github.ryanribeiro.sensor.repository.BipeRepository;
import com.github.ryanribeiro.sensor.repository.UserRepository;

import java.time.Instant;

@Service
public class BipeServices {

    @Autowired
    private BipeRepository bipeRepository;

    @Autowired
    private UserRepository userRepository;

    public BipeDTO enviarBipe(BipeDTO bipeDto) {
        if (bipeDto == null) {
            throw new IllegalArgumentException("BipeDTO não pode ser nulo");
        }
        if (bipeDto.getSenderId() == null) {
            throw new IllegalArgumentException("SenderId não pode ser nulo");
        }

        @SuppressWarnings("null")
        User sender = userRepository.findById(bipeDto.getSenderId())
                    .orElseThrow(() -> new IllegalArgumentException("Sender não encontrado."));


        User receiver = userRepository.findByLocalAndArduino(bipeDto.getLocal(), bipeDto.getArduino())
            .orElseThrow(() -> new IllegalArgumentException("Receiver não encontrado para local: " + bipeDto.getLocal() + ", arduino: " + bipeDto.getArduino()));

        if (sender.getUserId().equals(receiver.getUserId())) {
            throw new IllegalArgumentException("Sender e Receiver não podem ser o mesmo usuário.");
        }

        Bipe bipe = new Bipe();
        BeanUtils.copyProperties(bipeDto, bipe);
        bipe.setSender(sender);
        bipe.setReceiver(receiver);
        return new BipeDTO(bipeRepository.save(bipe));
    }

     public BipeDTO getLastBipe(
        String senderId, String receiverId, String local, String arduino
    ) {
         Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findTop1ByLocalAndArduinoOrderByCreatedAtDesc(
            local, 
            arduino
        ).orElse(null));
         if (bipe.isEmpty()) {
             throw new RuntimeException("Nenhum bipe encontrado para os parâmetros fornecidos.");
         }
         return new BipeDTO(bipe.get());
    }

    public String getLastBipeId(
        String senderId, String receiverId, String local, String arduino
    ) {
         Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findTop1ByLocalAndArduinoOrderByCreatedAtDesc(
            local, 
            arduino
        ).orElse(null));
         if (bipe.isEmpty()) {
             throw new RuntimeException("Nenhum bipe encontrado para os parâmetros fornecidos.");
         }
         return bipe.get().getId().toString();
    }

    public BipeDTO getBipeById(String bipeId, String senderId) {
        if (bipeId == null) {
            throw new IllegalArgumentException("ID do bipe não pode ser nulo");
        }
        if (bipeId.isBlank()) {
            throw new IllegalArgumentException("ID do bipe não pode ser nulo");
        }
        if (bipeId.isEmpty()) {
            throw new IllegalArgumentException("ID do bipe não pode ser vazio");
        }
        if (Long.parseLong(bipeId) <= 0) {
            throw new IllegalArgumentException("ID do bipe deve ser um número positivo");
        }

        Optional<Bipe> bipe = bipeRepository.findById(Long.parseLong(bipeId));
        
        if (bipe.isEmpty()) {
            throw new RuntimeException("Bipe não encontrado com id: " + bipeId + " para o usuário: " + senderId);
        }

        if (bipe.get().getSender() != null && !bipe.get().getSender().getUserId().equals(UUID.fromString(senderId))) {
            throw new RuntimeException("Acesso negado: o bipe não pertence ao usuário autenticado.");
        }
        return new BipeDTO(bipe.get());
    }

    public BipeDTO findFirstBipeBeforeId(String id, String senderId) {
        Optional<Bipe> bipe = bipeRepository.findFirstByIdLessThanOrderByIdDesc(Long.parseLong(id));

        if (bipe.isEmpty()) {
            throw new RuntimeException("Nenhum bipe encontrado antes do ID: " + id);    
        }

        if (bipe.get().getSender() != null && !bipe.get().getSender().getUserId().equals(UUID.fromString(senderId))) {
            throw new RuntimeException("Acesso negado: o bipe não pertence ao usuário autenticado.");
        }

        return new BipeDTO(bipe.get());
    }

    public BipeDTO findFirstBipeAfterId(String id, String senderId) {
        Optional<Bipe> bipe = bipeRepository.findFirstByIdGreaterThanOrderByIdAsc(Long.parseLong(id));

        if (bipe.isEmpty()) {
            throw new RuntimeException("Nenhum bipe encontrado depois do ID: " + id);    
        }

        if (bipe.get().getSender() != null && !bipe.get().getSender().getUserId().equals(UUID.fromString(senderId))) {
            throw new RuntimeException("Acesso negado: o bipe não pertence ao usuário autenticado.");
        }

        return new BipeDTO(bipe.get());
    }
}
