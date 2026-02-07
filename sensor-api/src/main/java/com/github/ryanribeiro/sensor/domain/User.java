package com.github.ryanribeiro.sensor.domain;

import java.util.Date;
import java.util.Set;
import java.util.UUID;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.ryanribeiro.sensor.dto.LoginRequestDTO;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinTable;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;

@Entity
@Table(name = "tb_users")
public class User{
    @Id
    @GeneratedValue(strategy = jakarta.persistence.GenerationType.UUID)
    @Column(name = "user_id", updatable = false, nullable = false)
    private UUID userId;

    @Column(name = "username", unique = true, nullable = false)
    private String username;

    private String password;

    @Column(name = "local")
    private String local;

    @Column(name = "arduino")
    private String arduino;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private Date createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private Date updatedAt;

    @ManyToMany(
        cascade = jakarta.persistence.CascadeType.ALL,
        fetch = jakarta.persistence.FetchType.EAGER
    )
    @JoinTable(
        name = "tb_user_roles",
        joinColumns = @JoinColumn(name= "user_id"),
        inverseJoinColumns = @JoinColumn(name = "role_id")
    )
    private Set<Role> roles;

    @OneToMany(mappedBy = "user")
    private Set<Evento> eventos;

    public User() {

    }

    public User(UUID userId) {
        this.userId = userId;
    }

    public User(UUID userId, String username, String password, String local, String arduino, Set<Role> roles, Set<Evento> eventos, Date createdAt, Date updatedAt) {
        this.userId = userId;
        this.username = username;
        this.password = password;
        this.local = local;
        this.arduino = arduino;
        this.roles = roles;
        this.eventos = eventos;
        this.local = local;
        this.arduino = arduino;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
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

    public Set<Role> getRoles() {
        return roles;
    }

    public void setRoles(Set<Role> roles) {
        this.roles = roles;
    }

    public Boolean isPasswordValid(LoginRequestDTO loginRequestDTO, PasswordEncoder passwordEncoder) {
        if (passwordEncoder.matches(loginRequestDTO.password(), this.password)) {
            return true;
        }
        return false;
    }
}
