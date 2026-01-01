package com.github.ryanribeiro.sensor.dto;

import com.github.ryanribeiro.sensor.domain.Evento;

import io.micrometer.common.lang.Nullable;

public class EventoDTO {
	private String local;

	private String arduino;

	private String dados;

	private String tipoSensor;

	@Nullable
	private String dataEvento;
	
	@Nullable
	private String counter;

	@Nullable
	private Boolean isFrequent;

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

	public Boolean getIsFrequent() {
		return isFrequent;
	}

	public void setIsFrequent(Boolean isFrequent) {
		this.isFrequent = isFrequent;
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
		this.isFrequent = evento.getIsFrequent();
	}
}
