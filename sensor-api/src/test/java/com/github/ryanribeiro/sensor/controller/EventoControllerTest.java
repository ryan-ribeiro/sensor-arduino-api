package com.github.ryanribeiro.sensor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ryanribeiro.sensor.dto.EventoDTO;
import com.github.ryanribeiro.sensor.services.EventoServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("deprecation")
@WebMvcTest(controllers = EventoController.class)
public class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EventoServices eventoServices;

    @SuppressWarnings("null")
    @Test
    void listarAdminWhenEmptyReturnsNotFound() throws Exception {
        when(eventoServices.listar()).thenReturn(List.of());

        mockMvc.perform(get("/eventos/admin/all").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
            .andExpect(status().isNotFound());
    }

    @SuppressWarnings("null")
    @Test
    void listarAdminReturnsOk() throws Exception {
        EventoDTO e = new EventoDTO();
        e.setTipoSensor("dht11");
        e.setLocal("home");
        e.setArduino("arduino-uno");
        e.setDados("50ºC");
        when(eventoServices.listar()).thenReturn(List.of(e));

        mockMvc.perform(get("/eventos/admin/all").with(org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(List.of(e))));
    }

    @SuppressWarnings("null")
    @Test
    void salvarRequiresAuthAndCreates() throws Exception {
        EventoDTO e = new EventoDTO();
        e.setTipoSensor("dht11");
        e.setLocal("home");
        e.setArduino("arduino-uno");
        e.setDados("50ºC");

        when(eventoServices.salvar(any())).thenReturn(e);

        mockMvc.perform(post("/eventos/salvar")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(e)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(e)));
    }

    @SuppressWarnings("null")
    @Test
    void salvarDadoCasoWiFiCaiuRequiresAuthAndCreates() throws Exception {
        EventoDTO e = new EventoDTO();
        e.setTipoSensor("dht11");
        e.setLocal("home");
        e.setArduino("arduino-uno");
        e.setDados("50ºC");
        e.setFrequenciaEmMillissegundos(3000L);
        e.setTemporizadorFixo(true);

        when(eventoServices.salvar(any())).thenReturn(e);

        mockMvc.perform(post("/eventos/salvarDadoCasoWiFiCaiu")
                        .with(SecurityMockMvcRequestPostProcessors.jwt().jwt(jwt -> jwt.subject(UUID.randomUUID().toString())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(e)))
                .andExpect(status().isCreated())
                .andExpect(content().json(objectMapper.writeValueAsString(e)));
    }
}
