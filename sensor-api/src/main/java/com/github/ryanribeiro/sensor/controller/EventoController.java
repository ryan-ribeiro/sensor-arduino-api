package com.github.ryanribeiro.sensor.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
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
	public ResponseEntity<Object> listar() {
		List<EventoDTO> eventos;
		try {
			eventos = eventoServices.listar();
			if (eventos.size() == 0) {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não existem eventos cadastrados no momento.");
			}
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
		return ResponseEntity.ok(eventos);
	}

	@GetMapping(value = "/{id}")
	public ResponseEntity<Object> buscarPorId(@PathVariable Long id) {
		try {
			Optional<Evento> evento = eventoServices.buscarPorId(id);
			if (evento.isPresent()) {
				return ResponseEntity.ok(new EventoDTO(evento.get()));
			} else {
				return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento não encontrado com id: " + id);
			}
		} catch (Exception e) {
			return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(e.getMessage());
		}
	}
		
	@PostMapping()
	public ResponseEntity<Object> salvar(@RequestBody EventoDTO eventoDTO) {
		EventoDTO eventoSaved;
		try {
			eventoSaved = eventoServices.salvar(eventoDTO);		
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}

		return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(eventoSaved);
	}

	/*
	DATALOGGER sem relógio RTC, para missões de exploração:
	Se o wifi falhou no MCU, não pode dar new na data assim que o wifi voltar. Então, se passar a data, ou parte dela, criar uma data manualmente na api.
	MCU recebe ultima data em formato rfc3339



	com contador (DAQ com temporizador fixo) para cada evento medido
	com contador (DAQ sem temporizador fixo) => MCU registra no cartão SD o tempo aproximado de cada medição, a partir do clock do MCU ou módulo de RTC.
		Então apenas enviar cada evento aproximado assim que a reconexão for efetuada
		Se o tempo foi adquirido por clock do MCU, é possível que haja overflow
	*/

	// DAQ com temporizador fixo
	@PostMapping("/salvarDadoCasoWiFiCaiu")
	public ResponseEntity<Object> salvarDadoCasoWiFiCaiu(@RequestBody EventoDTO eventoDTO) {
		String counter = eventoDTO.getCounter();
		Boolean isFrequent = eventoDTO.getIsFrequent();
		
		EventoDTO eventoSaved;
		try {
			if (isFrequent == null || !isFrequent || counter == null || counter.isEmpty()) {
				eventoSaved = eventoServices.salvar(eventoDTO);			
			} else {
				eventoSaved = eventoServices.salvarDadoCasoWiFiCaiu(eventoDTO);
			}
		} catch(Exception e) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
		}

		return ResponseEntity
					.status(HttpStatus.CREATED)
					.body(eventoSaved);
	}

	@GetMapping("/data-ultimo-evento")
	public ResponseEntity<String> getLastEventoDateString(
														  @RequestParam String arduino,
														  @RequestParam String tipoSensor, 
														  @RequestParam String local
														) {

		try {
			Date data = eventoServices.getLastDataEvento(arduino, tipoSensor, local);
			return ResponseEntity.ok(data.toString());
		} catch (IllegalStateException e) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.getMessage());
		}
	}

}
