package com.github.ryanribeiro.sensor.dto;

import com.github.ryanribeiro.sensor.domain.Evento;

import io.micrometer.common.lang.Nullable;

public class EventoDTO {
	private String local;

	private String arduino;

	private String dados;

	private String tipoSensor;

	@Nullable
	private String dataEvento;	//TODO: Refactor para tornar esse atributo um Date
	
	@Nullable
	private String counter;

	@Nullable
	private Long frequenciaEmMillissegundos;

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
		this.local = evento.getLocal();
		this.arduino = evento.getArduino();
		this.dados = evento.getDados();
		this.tipoSensor = evento.getTipoSensor();
		if (evento.getDataEvento() != null) {
			this.dataEvento = evento.getDataEvento().toString();
		}
		this.counter = evento.getCounter();
		this.frequenciaEmMillissegundos = evento.getFrequenciaEmMillissegundos();
	}
}
