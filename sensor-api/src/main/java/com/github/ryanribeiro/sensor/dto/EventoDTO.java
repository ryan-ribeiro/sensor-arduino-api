package com.github.ryanribeiro.sensor.dto;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.github.ryanribeiro.sensor.domain.Evento;

public class EventoDTO {
	private Long id;

	private String local;

	private String arduino;

	private String dados;

	private String tipoSensor;

	private UUID userId;

	@JsonProperty("data")
	private String dataEvento;	//TODO: Refactor para tornar esse atributo um Date
	
	private String counter;

	private Long frequenciaEmMillissegundos;

	private Boolean temporizadorFixo;

	public String getTipoSensor() {
		return tipoSensor;
	}

	public void setTipoSensor(String tipoSensor) {
		this.tipoSensor = tipoSensor;
	}

	public String getLocal() {
		return local;
	}

	public void setLocal(String local) {
		this.local = local;
	}

	public String getArduino() {
		return arduino;
	}

	public void setArduino(String arduino) {
		this.arduino = arduino;
	}

	public String getDados() {
		return dados;
	}

	public void setDados(String dados) {
		this.dados = dados;
	}

	public String getDataEvento() {
		return dataEvento;
	}

	public void setDataEvento(String dataAquisicao) {
		this.dataEvento = dataAquisicao;
	}

	public String getCounter() {
		return counter;
	}

	public void setCounter(String counter) {
		this.counter = counter;
	}

	public Long getFrequenciaEmMillissegundos() {
		return frequenciaEmMillissegundos;
	}

	public void setFrequenciaEmMillissegundos(Long tempoEmSegundos) {
		this.frequenciaEmMillissegundos = tempoEmSegundos;
	}

	public EventoDTO() {
	}

	public EventoDTO(Evento evento) {
		this.id = evento.getId();
		this.userId = evento.getUser() != null ? evento.getUser().getUserId() : null; 
		this.local = evento.getLocal();
		this.arduino = evento.getArduino();
		this.dados = evento.getDados();
		this.tipoSensor = evento.getTipoSensor();
		if (evento.getDataEvento() != null) {
			this.dataEvento = DateTimeFormatter.ISO_INSTANT.format(evento.getDataEvento().toInstant());
		}
		this.counter = evento.getCounter();
		this.frequenciaEmMillissegundos = evento.getFrequenciaEmMillissegundos();
	}

    public EventoDTO(List<Evento> evento) {
		if (evento == null || evento.isEmpty()) {
			return;
		}
		Evento first = evento.get(0);
		this.id = first.getId();
		this.userId = first.getUser() != null ? first.getUser().getUserId() : null;
		this.local = first.getLocal();
		this.arduino = first.getArduino();
		this.dados = first.getDados();
		this.tipoSensor = first.getTipoSensor();
		if (first.getDataEvento() != null) {
			this.dataEvento = DateTimeFormatter.ISO_INSTANT.format(first.getDataEvento().toInstant());
		}
		this.counter = first.getCounter();
		this.frequenciaEmMillissegundos = first.getFrequenciaEmMillissegundos();
    }

	public UUID getUserId() {
		return userId;
	}

	public void setUserId(UUID userId) {
		this.userId = userId;
	}

    public Boolean getTemporizadorFixo() {
        return temporizadorFixo;
    }

	public void setTemporizadorFixo(Boolean frequenciaAnalogica) {
		this.temporizadorFixo = frequenciaAnalogica;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}
}
