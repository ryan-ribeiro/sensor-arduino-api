package com.github.ryanribeiro.sensor.services;


import java.util.List;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.ryanribeiro.sensor.domain.Evento;
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
}
