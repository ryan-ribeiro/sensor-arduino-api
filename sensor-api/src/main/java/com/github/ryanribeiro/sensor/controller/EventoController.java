package com.github.ryanribeiro.sensor.controller;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.ryanribeiro.sensor.domain.Evento;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.EventoDTO;
import com.github.ryanribeiro.sensor.services.EventoServices;

@RestController
@RequestMapping("eventos")
public class EventoController{
	
	@Autowired
	private EventoServices eventoServices;

	@GetMapping("/admin/all")
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public ResponseEntity<Object> listar() {
		List<EventoDTO> eventos = eventoServices.listar();
		if (eventos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não existem eventos cadastrados no momento.");
		}
		return ResponseEntity.ok(eventos);
	}
	
	@GetMapping("")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public ResponseEntity<Object> listar(JwtAuthenticationToken token) {
		List<EventoDTO> eventos = eventoServices.listarPorUserId(token.getName());
		if (eventos.isEmpty()) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Não existem eventos cadastrados no momento.");
		}
		return ResponseEntity.ok(eventos);
	}

	@GetMapping(value = "/{id}")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ResponseEntity<Object> buscarPorId(@PathVariable Long id,
												JwtAuthenticationToken token
	) {
		Optional<Evento> evento = eventoServices.buscarPorId(id, token);
		if (evento.isPresent()) {
			return ResponseEntity.ok(new EventoDTO(evento.get()));
		} else {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Evento não encontrado com id: " + id);
		}
	}
		
	@PostMapping("/salvar")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ResponseEntity<Object> salvar(@RequestBody EventoDTO eventoDTO, JwtAuthenticationToken token) {
		EventoDTO eventoSaved;
		if (token.getName() == null) {
			return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Token inválido: userId não encontrado.");
		}
		
		User user = new User();
		user.setUserId(UUID.fromString(token.getName()));
		eventoDTO.setUser(user);

		eventoSaved = eventoServices.salvar(eventoDTO);

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

	//Se baseTimestamp não existir, exigir que cada leitura tenha um offset/índice ou usar hora de recepção (com perda de precisão).
	//Resposta: retornar contagem de eventos criados ou lista de IDs; tornar idempotente quando possível.
	//Nome: considerar /eventos/sync-offline ou /eventos/batch (mais claro que salvarDadoCasoWiFiCaiu).
	// DAQ com temporizador fixo
	@PostMapping("/salvarDadoCasoWiFiCaiu")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@Transactional(readOnly = false, propagation = Propagation.REQUIRED)
	public ResponseEntity<Object> salvarDadoCasoWiFiCaiu(@RequestBody EventoDTO eventoDTO,
			JwtAuthenticationToken token) {
		Long frequenciaEmMillissegundos = eventoDTO.getFrequenciaEmMillissegundos();

		EventoDTO eventoSaved;
		if (token.getName() == null) {
			throw new IllegalArgumentException();
		}
		User user = new User();
		user.setUserId(UUID.fromString(token.getName()));
		eventoDTO.setUser(user);

		if (frequenciaEmMillissegundos != null && eventoDTO.getFrequenciaAnalogica() == true) {
			// Se veio frequenciaEmMillissegundos válida, usar esse método
			eventoSaved = eventoServices.salvar(eventoDTO);
		} else {
			// Senão, salvar normalmente
			eventoSaved = eventoServices.salvarDadoCasoWiFiCaiu(eventoDTO);
		}

		return ResponseEntity
					.status(HttpStatus.CREATED)
					// .body(a);
					.body(eventoSaved);
	}

	// Considerar cache/TTL se for usado com alta frequência
	@GetMapping("/data-ultimo-evento")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	@Transactional(readOnly = true, propagation = Propagation.SUPPORTS)
	public ResponseEntity<String> getLastEventoDateString(
															@RequestParam String arduino,
															@RequestParam String tipoSensor, 
															@RequestParam String local,
															JwtAuthenticationToken token	
														)
	{

		User user = new User();
		user.setUserId(UUID.fromString(token.getName()));

		Date data = eventoServices.getLastDataEvento(user, arduino, tipoSensor, local);
		if (data == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum evento encontrado.");
		}
		return ResponseEntity.ok(data.toString());
	}

}
