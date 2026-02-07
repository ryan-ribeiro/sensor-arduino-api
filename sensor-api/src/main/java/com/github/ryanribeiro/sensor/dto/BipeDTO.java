package com.github.ryanribeiro.sensor.dto;

import java.util.UUID;

import com.github.ryanribeiro.sensor.domain.Bipe;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.Instant;

public class BipeDTO {
    private Long id;
    @NotNull
    @NotBlank
    private String mensagem;
    @NotNull
    @NotBlank
    private String local;
    @NotNull
    @NotBlank
    private String arduino;

    private Instant createdAt;

    private Instant updatedAt;

    private UUID senderId;

    @NotNull
    private UUID receiverId;

    public BipeDTO() {
    }

    public BipeDTO(Bipe bipe) {
        this(bipe.getId(), bipe.getMensagem(), bipe.getLocal(), bipe.getArduino(), bipe.getSender().getUserId(), bipe.getReceiver().getUserId(), bipe.getCreatedAt(), bipe.getUpdatedAt());
    }

    public BipeDTO(Long id, String mensagem, String local, String arduino, UUID senderId, UUID receiverId, Instant createdAt, Instant updatedAt ) {
        this.id = id;
        this.mensagem = mensagem;
        this.local = local;
        this.arduino = arduino;
        this.senderId = senderId;
        this.receiverId = receiverId;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public UUID getSenderId() {
        return senderId;
    }

    public void setSenderId(UUID senderId) {
        this.senderId = senderId;
    }
    
    public UUID getReceiverId() {
        return receiverId;
    }

    public void setReceiverId(UUID receiverId) {
        this.receiverId = receiverId;
    }

    public String getLocal() {
        return local;
    }

    public void setLocal(String local) {
        this.local = local;
    }

    public String getArduino() {
        return arduino;
    }

    public void setArduino(String arduino) {
        this.arduino = arduino;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }
}
