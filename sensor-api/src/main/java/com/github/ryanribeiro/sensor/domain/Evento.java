package com.github.ryanribeiro.sensor.domain;

import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * 
 */
@Entity
@Table(name = "tb_evento")
public class Evento{

	@Id
	@GeneratedValue(strategy = jakarta.persistence.GenerationType.SEQUENCE)
	@Column(name = "id")
	private Long id;

	@ManyToOne
	@JoinColumn(name = "user_id")
	private User user;
	
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

	private Long frequenciaEmMillissegundos; // Tempo em milissegundos entre eventos, para dados frequentes

	private Boolean temporizadorFixo;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public User getUser() {
		return user;
	}

	public void setUser(User user) {
		this.user = user;
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

	public Long getFrequenciaEmMillissegundos() {
		return frequenciaEmMillissegundos;
	}

	public void setFrequenciaEmMillissegundos(Long frequenciaEmMillissegundos) {
		this.frequenciaEmMillissegundos = frequenciaEmMillissegundos;
	}

	public Boolean getTemporizadorFixo() {
		return temporizadorFixo;
	}

	public void setTemporizadorFixo(Boolean frequenciaAnalogica) {
		this.temporizadorFixo = frequenciaAnalogica;
	}

	
}
