package com.github.ryanribeiro.sensor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ryanribeiro.sensor.dto.LoginRequestDTO;
import com.github.ryanribeiro.sensor.services.TokenServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SuppressWarnings("deprecation")
@WebMvcTest(controllers = TokenController.class)
@AutoConfigureMockMvc(addFilters = false)
public class TokenControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TokenServices tokenServices;

    @SuppressWarnings("null")
    @Test
    void loginReturnsToken() throws Exception {
        LoginRequestDTO dto = new LoginRequestDTO("admin", "admin");

        when(tokenServices.loginService(any())).thenReturn(new com.github.ryanribeiro.sensor.dto.LoginResponseDTO("abc", 123L));

        mockMvc.perform(post("/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
            .andExpect(status().isOk())
            .andExpect(content().json(objectMapper.writeValueAsString(new com.github.ryanribeiro.sensor.dto.LoginResponseDTO("abc", 123L))));
    }
}
