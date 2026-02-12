package com.github.ryanribeiro.sensor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.ryanribeiro.sensor.domain.Bipe;

@Repository
public interface BipeRepository extends JpaRepository<Bipe, Long> {
    Optional<Bipe> findTop1ByLocalAndArduinoOrderByCreatedAtDesc(
        String local, 
        String arduino
    );

    Optional<Bipe> findTop1ByReceiverUserIdAndLocalAndArduinoOrderByCreatedAtDesc(
        UUID receiverUserId,
        String local, 
        String arduino
    );

    Optional<Bipe> findByIdAndReceiverUserId(Long id, UUID receiverUserId);

    // Encontra o primeiro registro com ID menor que o atual, ordenando do maior para o menor
    Optional<Bipe> findFirstByIdLessThanAndReceiverUserIdOrderByIdDesc(Long id, UUID receiverUserId);

    // Encontra o primeiro registro com ID maior que o atual, ordenando do menor para o maior
    Optional<Bipe> findFirstByIdGreaterThanAndReceiverUserIdOrderByIdAsc(Long id, UUID receiverUserId);
}
