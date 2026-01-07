package com.github.ryanribeiro.sensor.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.ryanribeiro.sensor.domain.Evento;
import com.github.ryanribeiro.sensor.domain.User;

public interface EventoRepository extends JpaRepository<Evento, Long>{

    // Busca eventos pelo user.userId (UUID). Campo na entidade User é `userId`.
    List<Evento> findByUser(User user);
    Optional<Evento> findByUserAndId(User user, Long id);

    /**
     * Retorna o último evento ordenado por data/hora decrescente.
     * O Spring Data JPA gera automaticamente a query: SELECT * FROM tb_evento ORDER BY dt_evento DESC LIMIT 1
     * @return Optional com o evento mais recente, ou empty se não houver eventos
     */
    Optional<Evento> findTop1ByOrderByDataEventoDesc();

    Optional<List<Evento>> findTop2ByOrderByDataEventoDesc();

    Optional<Evento> findTop1ByTipoSensorAndArduinoAndLocalOrderByDataEventoDesc(String tipoSensor, String arduino, String local);

    Optional<Evento> findTop1ByUserAndTipoSensorAndArduinoAndLocalOrderByDataEventoDesc(User user, String tipoSensor, String arduino, String local);

    Optional<List<Evento>> findTop2ByTipoSensorAndArduinoAndLocalOrderByDataEventoDesc(String tipoSensor, String arduino, String local);

    // Retorna os 2 eventos DESC apenas de um dado usuário
    Optional<List<Evento>> findTop2ByUserAndTipoSensorAndArduinoAndLocalOrderByDataEventoDesc(User user, String tipoSensor, String arduino, String local);


}
