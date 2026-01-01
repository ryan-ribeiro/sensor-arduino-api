package com.github.ryanribeiro.sensor.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "tb_evento")
public class Evento{

	@Id
	@GeneratedValue
	@Column(name = "id")
	private Long id;
	
	@Column(name = "tp_sensor")
	private String tipoSensor;
	
	@Column(name = "local")
	private String local;
	
	@Column(name = "arduino")
	private String arduino;

	@Column(name = "dados")
	private String dados;

	@JsonInclude(value = Include.NON_NULL)
	// Padrão RFC 3339 para data e hora com timezone
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX")
	@Column(name = "dt_evento")
	private Date dataEvento;

	@Column(name = "counter")
	private String counter;	// Contador de overflow ou quantidade de reconexões

	@Column(name = "is_frequent")
	private Boolean isFrequent;	// Flag para se os dados são coletados em tempo frequente ou não
								// Por exemplo, a cada 5 segundos
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

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

	public Date getDataEvento() {
		return dataEvento;
	}

	public void setDataEvento(Date dataEvento) {
		this.dataEvento = dataEvento;
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
	
}
