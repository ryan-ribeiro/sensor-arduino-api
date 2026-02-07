package com.github.ryanribeiro.sensor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.github.ryanribeiro.sensor.domain.Bipe;

@Repository
public interface BipeRepository extends JpaRepository<Bipe, Long> {
    Optional<Bipe> findTop1BySender_UserIdAndReceiver_UserIdAndLocalAndArduinoOrderByCreatedAtDesc(
        UUID senderId, 
        UUID receiverId, 
        String local, 
        String arduino
    );
}
