package com.github.ryanribeiro.sensor.repository;

import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    // Consulta SQL para encontrar o registro mais recente para um local e arduino específicos, ordenando por created_at
    @Query("SELECT b FROM Bipe b WHERE b.local = :local AND b.arduino = :arduino ORDER BY b.createdAt DESC LIMIT 1")
    Optional<Bipe> findLastBipeGivenLocalAndArduino(
        @Param("local") String local,
        @Param("arduino") String arduino
    );

    @Query("SELECT b from Bipe b WHERE b.id > :id AND b.receiver.userId = :receiverUserId ORDER BY b.id ASC LIMIT 1")
    Optional<Bipe> findBipeGreaterThanIdAndReceiverId(
        @Param("id") Long id,
        @Param("receiverUserId") UUID receiverUserId
    );
}
