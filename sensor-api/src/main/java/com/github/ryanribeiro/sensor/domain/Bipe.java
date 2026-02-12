package com.github.ryanribeiro.sensor.domain;

import java.time.Instant;

import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import jakarta.persistence.*;

@Entity
@Table(name = "tb_bipe")
public class Bipe {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @Column(name = "mensagem")
    private String mensagem;

    @Column(name = "local_sender")
    private String localSender;   // Local do bipe do sender
    @Column(name = "arduino_sender")
    private String arduinoSender; // Arduino do bipe do sender

    @Column(name = "local", nullable=false)
    private String local;   // Local do bipe do receiver

    @Column(name = "arduino", unique = true, nullable = false)
    private String arduino; // Arduino do bipe do receiver

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    private User sender;  // user.userId -> sender

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = false)
    private User receiver; // evento.userId -> receiver

    @Column(name="created_at", nullable = false, updatable = false) // Ensures this field isn't updated on subsequent saves
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant createdAt;

    @Column(name="updated_at", nullable = false, updatable = true)
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
    private Instant updatedAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
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

    public User getSender() {
        return sender;
    }

    public void setSender(User sender) {
        this.sender = sender;
    }

    public User getReceiver() {
        return receiver;
    }

    public void setReceiver(User receiver) {
        this.receiver = receiver;
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
