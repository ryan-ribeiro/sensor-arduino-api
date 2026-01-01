package com.github.ryanribeiro.sensor.services;


import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ryanribeiro.sensor.domain.Evento;
import com.github.ryanribeiro.sensor.dto.DataDTO;
import com.github.ryanribeiro.sensor.dto.EventoDTO;
import com.github.ryanribeiro.sensor.repository.EventoRepository;

@Service
public class EventoServices {
	@Autowired
	private EventoRepository eventoRepository;
	
	
	public List<Evento> listar() {
		return eventoRepository.findAll();
	}
	
	public Evento salvar(EventoDTO eventoDTO) {
		if (eventoDTO == null) {
			throw new IllegalArgumentException("EventoDTO cannot be null");
		}

		Evento sensor = new Evento();
		BeanUtils.copyProperties(eventoDTO, sensor);
		
		return eventoRepository.save(sensor);
	}

	
	// Será mais rápido o MCU enviar a data e hora no formato RFC 3339?
	// Ou será mais rápido o MCU fazer requisição HTTP para obter cada componente separadamente?
	public String getLastDataEvento() {
		Evento evento = eventoRepository.findLastEvento(new java.util.Date());
		return evento.getDataEvento().toString();
	}
	/**
	 * Retorna o último evento como DataDTO com os componentes de data/hora separados.
	 * A serialização JSON é feita automaticamente pelo Spring Boot (Jackson).
	 * 
	 * @return DataDTO contendo os componentes de data/hora do último evento
	 */
	public DataDTO getLastDataEventoJSON() {
		Date dataAtual = new Date();
		Evento evento = eventoRepository.findLastEvento(dataAtual);
		
		if (evento == null || evento.getDataEvento() == null) {
			throw new IllegalStateException("Nenhum evento encontrado ou evento sem data");
		}

		Instant instant = evento.getDataEvento().toInstant();
		ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());
		
		DataDTO dataDTO = new DataDTO();
		dataDTO.setYear(zonedDateTime.getYear());
		dataDTO.setMonth(zonedDateTime.getMonthValue());
		dataDTO.setDay(zonedDateTime.getDayOfMonth());
		dataDTO.setHour(zonedDateTime.getHour());
		dataDTO.setMinute(zonedDateTime.getMinute());
		dataDTO.setSecond(zonedDateTime.getSecond());
		dataDTO.setNanoseconds(zonedDateTime.getNano());
		dataDTO.setLocal(evento.getLocal());
		
		return dataDTO;
	}
}
