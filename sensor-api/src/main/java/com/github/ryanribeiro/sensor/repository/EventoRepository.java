package com.github.ryanribeiro.sensor.repository;


import java.util.Date;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.ryanribeiro.sensor.domain.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long>{

    @Query("SELECT e FROM Evento e WHERE e.dataEvento <= :data")
    Evento findLastEvento(@Param("data")Date data);
}
