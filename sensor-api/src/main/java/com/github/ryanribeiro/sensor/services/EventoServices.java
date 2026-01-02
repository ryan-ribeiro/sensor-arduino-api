package com.github.ryanribeiro.sensor.services;


import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

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
	
	
	public List<EventoDTO> listar() {
		var eventos = eventoRepository.findAll();

		List<EventoDTO> eventosDTO = eventos.stream()
				.map(evento -> new EventoDTO(evento))
				.toList();

			return eventosDTO;
	}
	
	public Optional<Evento> buscarPorId(Long id) throws IllegalArgumentException {
		if (id == null) {
			return Optional.empty();
		}
		
		if (id < 0) {
			throw new IllegalArgumentException("O id passado nao pode ser negativo: " + id);
		}

		return eventoRepository.findById(id);
	}
	
	// Para DAQ sem temporizador fixo
	//			Necessário um relógio para salvar num disco rígido cada medição
	//			Ou uma lógica para recuperar o tempo da medição, caso o wifi tenha caído
	public EventoDTO salvar(EventoDTO eventoDTO) {
		if (eventoDTO == null) {
			throw new IllegalArgumentException("EventoDTO cannot be null");
		}

		Evento sensor = new Evento();
		BeanUtils.copyProperties(eventoDTO, sensor);
		
		// Se a data foi fornecida no DTO, converter de String para Date
		Date dataConvertida;
		if (eventoDTO.getDataEvento() == null || eventoDTO.getDataEvento() == "") {
			dataConvertida = createDataEventoRFC3339(null);
		} else {
			dataConvertida = parseDataEvento(eventoDTO.getDataEvento());
		}

		if (dataConvertida != null) {
			sensor.setDataEvento(dataConvertida);
		}
		
		return new EventoDTO(eventoRepository.save(sensor));
	}

	// Para DAQ com temporizador fixo
	public EventoDTO salvarDadoCasoWiFiCaiu(EventoDTO eventoDTO) {
		Date dataParaSalvar = null;
		
		// Priorizar a data fornecida no DTO no formato RFC 3339, se existir
		if (eventoDTO.getDataEvento() != null && !eventoDTO.getDataEvento().trim().isEmpty()) {
			// Se a data foi fornecida no DTO, converter de String para Date (RFC 3339)
			dataParaSalvar = parseDataEvento(eventoDTO.getDataEvento());
		} else {
			// Pegar hora do último evento salvo
			Date dataUltimoEvento = getLastDataEvento( 
													  eventoDTO.getArduino(),
													  eventoDTO.getTipoSensor(),
													  eventoDTO.getLocal()
													);
			if (dataUltimoEvento == null) {
				return salvar(eventoDTO);	// Salvar normalmente se não houver evento anterior
			}

			// Pegar diferença de tempo dos eventos
			if (eventoDTO.getFrequenciaEmMillissegundos() != null && eventoDTO.getFrequenciaEmMillissegundos() > 0) {
				// Usar a frequencia enviada no DTO
				long novoTimestamp = dataUltimoEvento.getTime() + eventoDTO.getFrequenciaEmMillissegundos();
				dataParaSalvar = new Date(novoTimestamp);
			} else {
				long frequency = getEventoFrequencyDAQ(
														eventoDTO.getArduino(),
														eventoDTO.getTipoSensor(),
														eventoDTO.getLocal()
													);

				// Somar frequency à data do ultimo evento
				long novoTimestamp = dataUltimoEvento.getTime() + frequency;
				dataParaSalvar = new Date(novoTimestamp);
			}
		}
		// Se dataEvento for null/vazio E isFrequent for false/null, o @PrePersist setará a data atual

		// Salvar cada evento em cascata, por ordem de aquisição
		Evento sensor = new Evento();
		BeanUtils.copyProperties(eventoDTO, sensor);
		
		// Setar a data no objeto Evento (se foi calculada ou fornecida)
		if (dataParaSalvar != null) {
			sensor.setDataEvento(dataParaSalvar);
		}
		// Caso contrário, o @PrePersist na entidade Evento setará a data atual automaticamente
		
		return new EventoDTO(eventoRepository.save(sensor));
	}

	// Para caso o DAQ com temporizador fixo tenha o temporizados enviado em segundos
	// Mas, por algum motivo, a data em questão pode ser enviada ou não
	public EventoDTO salvarDadoCasoWiFiCaiuFrequencia(EventoDTO eventoDTO) {

		Date dataParaSalvar = null;

		if (eventoDTO.getDataEvento() != null && !eventoDTO.getDataEvento().trim().isEmpty()) {
			dataParaSalvar = parseDataEvento(eventoDTO.getDataEvento());
		} else {	// Entao pegar hora do ultimo evento e somar a frequencia
			Date dataUltimoEvento = getLastDataEvento( 
													  eventoDTO.getArduino(),
													  eventoDTO.getTipoSensor(),
													  eventoDTO.getLocal()
													);

			// Pegar diferença de tempo dos eventos
			long frequency = eventoDTO.getFrequenciaEmMillissegundos();

			long novoTimestamp = dataUltimoEvento.getTime() + frequency;
			dataParaSalvar = new Date(novoTimestamp);
		}

		Evento sensor = new Evento();
		BeanUtils.copyProperties(eventoDTO, sensor);

		if (dataParaSalvar != null) {
			sensor.setDataEvento(dataParaSalvar);
		}

		return new EventoDTO(eventoRepository.save(sensor));
	}

	/**
	 * Converte um Date para String no formato RFC 3339.
	 * Se nenhum Date for fornecido (null), pega a data atual e converte para RFC 3339.
	 * 
	 * @param date Date a ser convertido para RFC 3339 (opcional, se null usa data atual)
	 * @return String no formato RFC 3339 (ex: "2024-01-01T12:00:00.000-03:00")
	 */
	public Date createDataEventoRFC3339(Date date) {
		// Se a data for nula, usamos a data atual
		Date dataParaFormatar = (date != null) ? date : new Date();
    
		// Convertendo para a API moderna (java.time)
		Instant instant = dataParaFormatar.toInstant();
		ZoneId systemZoneId = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = instant.atZone(systemZoneId);
		
		// Para retornar um java.util.Date a partir de um ZonedDateTime:
		return Date.from(zonedDateTime.toInstant());
	}

	/**
	 * Converte uma String de data para Date.
	 * Aceita APENAS formato RFC 3339 com timezone (yyyy-MM-dd'T'HH:mm:ss.SSSXXX).
	 * Exemplo válido: "2024-01-01T12:00:00.000-03:00"
	 * 
	 * @param dataString String no formato RFC 3339
	 * @return Date convertido da string
	 * @throws IllegalArgumentException se o formato da data for inválido ou não for RFC 3339
	 */
	public Date parseDataEvento(String dataString) {
		if (dataString == null || dataString.trim().isEmpty()) {
			return null;
		}
		
		String dataTrimmed = dataString.trim();
		
		// Validar formato básico RFC 3339: deve conter 'T' e timezone
		if (!dataTrimmed.contains("T") || !dataTrimmed.matches(".*[+-]\\d{2}:\\d{2}$")) {
			throw new IllegalArgumentException(
				"Formato de data inválido. Esperado RFC 3339 (yyyy-MM-dd'T'HH:mm:ss.SSSXXX). " +
				"Exemplo: '2024-01-01T12:00:00.000-03:00'. Recebido: '" + dataString + "'"
			);
		}
		
		// Formato RFC 3339 com timezone (padrão usado no JSON)
		String formato = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";
		
		try {
			SimpleDateFormat sdf = new SimpleDateFormat(formato);
			sdf.setLenient(false); // Não permitir datas inválidas (ex: 32 de janeiro)
			return sdf.parse(dataTrimmed);
		} catch (ParseException e) {
			throw new IllegalArgumentException(
				"Formato de data inválido. Esperado RFC 3339 (yyyy-MM-dd'T'HH:mm:ss.SSSXXX). " +
				"Exemplo: '2024-01-01T12:00:00.000-03:00'. Recebido: '" + dataString + "'. " +
				"Erro: " + e.getMessage(), e
			);
		}
	}

	// Para DAQ com temporizador fixo
	public long getEventoFrequencyDAQ(String arduino, String tipoSensor, String local) {
		List<Evento> listaEventos = eventoRepository.findTop2ByTipoSensorAndArduinoAndLocalOrderByDataEventoDesc(
								tipoSensor,
								arduino,
								local
								)
							.orElseThrow(() -> new IllegalStateException("Pelo menos 2 eventos nao foram encontrados"));
		
		// encontrar diferença de tempo entre os dois eventos acima
		if (!listaEventos.isEmpty() && listaEventos.size() == 2) {
			long frequenciaMillis = listaEventos.get(0).getDataEvento().getTime() - listaEventos.get(1).getDataEvento().getTime();
			return frequenciaMillis;
		}
		
		return 0;		// Isso nunca vai ser executado
	}

	
	// Será mais rápido o MCU enviar a data e hora no formato RFC 3339?
	// Ou será mais rápido o MCU fazer requisição HTTP para obter cada componente separadamente?
	public String getLastDataEventoString() {
		Evento evento = eventoRepository.findTop1ByOrderByDataEventoDesc()
				.orElseThrow(() -> new IllegalStateException("Nenhum evento encontrado"));
		return evento.getDataEvento().toString();
	}

	public Date getLastDataEvento(String arduino, String tipoSensor, String local) {
		return eventoRepository.findTop1ByTipoSensorAndArduinoAndLocalOrderByDataEventoDesc(
				tipoSensor,
				arduino,
				local
		).map(Evento::getDataEvento)
		.orElse(null);
		// .orElseThrow(() -> new IllegalStateException("Nenhum evento encontrado"));
	}

	
	/**
	 * Retorna o último evento como DataDTO com os componentes de data/hora separados.
	 * A serialização JSON é feita automaticamente pelo Spring Boot (Jackson).
	 * 
	 * @return DataDTO contendo os componentes de data/hora do último evento
	 */
	public DataDTO getLastDataEventoJSON() {
		Evento evento = eventoRepository.findTop1ByOrderByDataEventoDesc()
				.orElseThrow(() -> new IllegalStateException("Nenhum evento encontrado"));
		
		if (evento.getDataEvento() == null) {
			throw new IllegalStateException("Evento encontrado mas sem data");
		}

		Instant instant = evento.getDataEvento().toInstant();
		ZoneId systemZoneId = ZoneId.systemDefault();
		ZonedDateTime zonedDateTime = instant.atZone(systemZoneId);
		
		DataDTO dataDTO = new DataDTO();
		dataDTO.setYear(zonedDateTime.getYear());
		dataDTO.setMonth(zonedDateTime.getMonthValue());
		dataDTO.setDay(zonedDateTime.getDayOfMonth());
		dataDTO.setHour(zonedDateTime.getHour());
		dataDTO.setMinute(zonedDateTime.getMinute());
		dataDTO.setSecond(zonedDateTime.getSecond());
		dataDTO.setNanoseconds(zonedDateTime.getNano());
		dataDTO.setLocal(systemZoneId.toString());
		
		return dataDTO;
	}
}
