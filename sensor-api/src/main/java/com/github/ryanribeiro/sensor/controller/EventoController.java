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
		eventoDTO.setUserId(user.getUserId());

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
	//Nome: considerar /eventos/sync-offline ou /eventos/batch (mais claro que salvarDadoCasoWiFiCaiu).
	// DAQ com temporizador fixo

	/**
	 * Salva um evento considerando cenários em que a conexão Wi‑Fi caiu.
	 *
	 * Comportamento:
	 * - Se o DTO contém uma data (dataEvento) válida, salva o evento nessa data.
	 * - Caso a data não seja fornecida:
	 *   - Recupera a data do último evento salvo na API e a utiliza como base.
	 *   - Se foi enviado frequenciaEmMillissegundos (temporizador), adiciona esse valor à data do último evento e salva o novo evento nessa data calculada.
	 *   - Caso não tenha sido enviado frequenciaEmMillissegundos, mas a flag temporizadorFixo estiver ativada, calcula a diferença de tempo entre os dois últimos eventos salvos e usa essa diferença como temporizador para determinar a data do evento a ser salvo.
	 *
	 * Requisitos de segurança:
	 * - Requer autorização com a autoridade 'SCOPE_USER'.
	 *
	 * Parâmetros:
	 * @param eventoDTO DTO contendo os dados do evento a salvar (pode incluir dataEvento, frequenciaEmMillissegundos e temporizadorFixo).
	 * @param token     token de autenticação JWT do usuário; o userId é obtido a partir de token.getName().
	 *
	 * Retorno:
	 * @return ResponseEntity com status 201 (CREATED) e o EventoDTO salvo no corpo.
	 *
	 * Exceções:
	 * @throws IllegalArgumentException se o token não contiver o identificador do usuário (token.getName() == null).
	 * @throws EventoNotFoundException se não for possível recuperar o último evento necessário para calcular a data.
	 * @throws
	 */
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
		eventoDTO.setUserId(user.getUserId());

		if (frequenciaEmMillissegundos != null && eventoDTO.getTemporizadorFixo() == true) {
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

		Date data = eventoServices.getLastDataEvento(user.getUserId(), arduino, tipoSensor, local);
		if (data == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Nenhum evento encontrado.");
		}
		return ResponseEntity.ok(data.toString());
	}

	@GetMapping("/id-ultimo-evento")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	public String getLastEventoId(
			@RequestParam String arduino,
			@RequestParam String tipoSensor, 
			@RequestParam String local,
			JwtAuthenticationToken token	
	) {
		User user = new User();
		user.setUserId(UUID.fromString(token.getName()));

		String id = eventoServices.getLastEventoId(user.getUserId(), arduino, tipoSensor, local);
		if (id == null) {
			return "Nenhum evento encontrado.";
		}
		return id;
	}

	@GetMapping("/ultimo-evento")
	@PreAuthorize("hasAuthority('SCOPE_USER')")
	public ResponseEntity<EventoDTO> getLastEvento(
		@RequestParam String arduino,
		@RequestParam String tipoSensor, 
		@RequestParam String local,
		JwtAuthenticationToken token	
	) {
		User user = new User();
		user.setUserId(UUID.fromString(token.getName()));

		EventoDTO evento = eventoServices.getLastEvento(user.getUserId(), arduino, tipoSensor, local);
		if (evento == null) {
			return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
		}
		return ResponseEntity.ok(evento);
	}

}
