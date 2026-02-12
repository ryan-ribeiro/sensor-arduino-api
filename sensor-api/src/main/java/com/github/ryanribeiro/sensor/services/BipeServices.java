package com.github.ryanribeiro.sensor.services;

import java.util.Base64;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ryanribeiro.sensor.domain.Bipe;
import com.github.ryanribeiro.sensor.exceptions.AccessDeniedException;
import com.github.ryanribeiro.sensor.exceptions.GenericException;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.BipeDTO;
import com.github.ryanribeiro.sensor.repository.BipeRepository;
import com.github.ryanribeiro.sensor.repository.UserRepository;

import java.nio.charset.StandardCharsets;

@Service
public class BipeServices {

    @Autowired
    private BipeRepository bipeRepository;

    @Autowired
    private UserRepository userRepository;

    public String encodeBase64(String mensagem) {
        try {
            return Base64.getEncoder().encodeToString(mensagem.getBytes(StandardCharsets.UTF_8));
        } catch (IllegalArgumentException e) {
            throw new GenericException("Mensagem inválida: não é uma string válida.", e);
        }
    }

    public String decodeBase64(String hash) {
        try {
            byte[] decodedBytes = Base64.getDecoder().decode(hash);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        } catch (IllegalArgumentException e) {
            throw new GenericException("Hash inválida: não é uma string Base64 válida.", e);
        }
    }

    private boolean isValidBase64(String str) {
        if (str == null || str.isEmpty()) {
            return false;
        }
        try {
            Base64.getDecoder().decode(str);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private String safeDecodeBase64(String mensagem) {
        if (mensagem == null) {
            return null;
        }
        if (isValidBase64(mensagem)) {
            try {
                return decodeBase64(mensagem);
            } catch (GenericException e) {
                return mensagem; // Retorna a mensagem original se falhar
            }
        }
        return mensagem; // Retorna a mensagem original se não for Base64
    }
    
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

        // Encode a mensagem em Base64 antes de salvar
        try {
            bipe.setMensagem(encodeBase64(bipeDto.getMensagem()));
        } catch (GenericException e) {
            throw new GenericException("Erro ao codificar a mensagem: " + e.getMessage(), e);
        }
        bipeRepository.save(bipe);
        bipe.setMensagem(safeDecodeBase64(bipe.getMensagem()));
        return new BipeDTO(bipe);
    }

     public BipeDTO getLastBipe(
        String senderId, String local, String arduino
    ) {
         Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findTop1ByReceiverUserIdAndLocalAndArduinoOrderByCreatedAtDesc(
            UUID.fromString(senderId),
            local, 
            arduino
        ).orElse(null));
         if (bipe.isEmpty()) {
             throw new IllegalArgumentException("Nenhum bipe encontrado para os parâmetros fornecidos.");
         }

         // Decode a mensagem do bipe antes de retornar
         BipeDTO bipeEntity = new BipeDTO(bipe.get());
         bipeEntity.setMensagem(safeDecodeBase64(bipeEntity.getMensagem()));

         return bipeEntity;
    }

    public String getLastBipeId(
        String senderId, String local, String arduino
    ) {
         Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findTop1ByReceiverUserIdAndLocalAndArduinoOrderByCreatedAtDesc(
            UUID.fromString(senderId),
            local, 
            arduino
        ).orElse(null));
         if (bipe.isEmpty()) {
             throw new IllegalArgumentException("Nenhum bipe encontrado para os parâmetros fornecidos.");
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

        Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findByIdAndReceiverUserId(Long.parseLong(bipeId), UUID.fromString(senderId)).orElse(null));
        if (bipe.isEmpty()) {
            throw new IllegalArgumentException("Bipe não encontrado com id: " + bipeId + " para o usuário: " + senderId);
        }
        // Decode da mensagem do bipe antes de retornar
        BipeDTO bipeEntity = new BipeDTO(bipe.get());
        bipeEntity.setMensagem(safeDecodeBase64(bipeEntity.getMensagem()));

        if (bipe.get().getReceiver() != null && !bipe.get().getReceiver().getUserId().equals(UUID.fromString(senderId))) {
            throw new AccessDeniedException("Acesso negado: o bipe não pertence ao usuário autenticado.");
        }
        return bipeEntity;
    }

    public BipeDTO findFirstBipeBeforeId(String id, String senderId) {
        Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findFirstByIdLessThanAndReceiverUserIdOrderByIdDesc(Long.parseLong(id), UUID.fromString(senderId)).orElse(null));

        if (bipe == null || bipe.isEmpty()) {
            throw new IllegalArgumentException("Nenhum bipe encontrado antes do ID: " + id);    
        }

        if (bipe.get().getReceiver() != null && !bipe.get().getReceiver().getUserId().equals(UUID.fromString(senderId))) {
            throw new AccessDeniedException("Acesso negado: o bipe não pertence ao usuário autenticado.");
        }

        // Decode da mensagem do bipe antes de retornar
        BipeDTO bipeEntity = new BipeDTO(bipe.get());
        bipeEntity.setMensagem(safeDecodeBase64(bipeEntity.getMensagem()));

        return bipeEntity;
    }

    public BipeDTO findFirstBipeAfterId(String id, String senderId) {
        Optional<Bipe> bipe = Optional.ofNullable(bipeRepository.findFirstByIdGreaterThanAndReceiverUserIdOrderByIdAsc(Long.parseLong(id), UUID.fromString(senderId)).orElse(null));

        if (bipe == null || bipe.isEmpty()) {
            throw new IllegalArgumentException("Nenhum bipe encontrado depois do ID: " + id);    
        }

        if (bipe.get().getReceiver() != null && !bipe.get().getReceiver().getUserId().equals(UUID.fromString(senderId))) {
            throw new AccessDeniedException("Acesso negado: o bipe não pertence ao usuário autenticado.");
        }

        // Decode da mensagem do bipe antes de retornar
        BipeDTO bipeEntity = new BipeDTO(bipe.get());
        bipeEntity.setMensagem(safeDecodeBase64(bipeEntity.getMensagem()));

        return bipeEntity;
    }
}
