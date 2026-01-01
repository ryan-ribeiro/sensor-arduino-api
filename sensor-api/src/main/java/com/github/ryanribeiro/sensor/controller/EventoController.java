package com.github.ryanribeiro.sensor.controller;


import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.domain.Evento;
import com.github.ryanribeiro.sensor.dto.EventoDTO;
import com.github.ryanribeiro.sensor.services.EventoServices;

@RestController
@RequestMapping("eventos")
public class EventoController{
	
	@Autowired
	private EventoServices eventoServices;
	
	@GetMapping
	public ResponseEntity<List<Evento>> listar() {
		return ResponseEntity.ok(eventoServices.listar());
	}
		
	@PostMapping()
	public ResponseEntity<Void> salvar(@RequestBody EventoDTO eventoDTO) {
		
		eventoServices.salvar(eventoDTO);
		
		return ResponseEntity
					.status(HttpStatus.CREATED)
					.build();
	}

	@GetMapping("/ultimo-evento")
	public String getLastEvento() {
		return eventoServices.getLastDataEvento();
	}

	/*
	DATALOGGER sem relógio RTC, para missões de exploração:
	Se o wifi falhou no MCU, não pode dar new na data assim que o wifi voltar. Então, se passar a data, ou parte dela, criar uma data manualmente na api.
	MCU recebe ultima data em formato rfc3339



	com contador (DAQ com temporizador fixo) para cada evento medido
	com contador (DAQ sem temporizador fixo) => MCU registra no cartão SD o tempo aproximado de cada medição, a partir do clock do MCU.
	*/
}
