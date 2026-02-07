package com.github.ryanribeiro.sensor.domain;

import java.time.Instant;

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

    //TODO: se extende Evento, não precisa desses campos, ou seja, local e arduino já estão na classe pai Evento
    @Column(name = "local")
    private String local;

    @Column(name = "arduino")
    private String arduino;

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
}
