package com.github.ryanribeiro.sensor.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.ryanribeiro.sensor.domain.User;
import com.github.ryanribeiro.sensor.dto.CreateUserDTO;
import com.github.ryanribeiro.sensor.services.UserServices;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SuppressWarnings("deprecation")
@WebMvcTest(controllers = UserController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private UserServices userServices;

    @SuppressWarnings("null")
    @Test
    void createUserReturnsCreated() throws Exception {
        CreateUserDTO dto = new CreateUserDTO("ryann", "123456");

        doNothing().when(userServices).createUser(org.mockito.ArgumentMatchers.any(com.github.ryanribeiro.sensor.dto.CreateUserDTO.class));

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @SuppressWarnings("null")
    @Test
    void getAllUsersReturnsList() throws Exception {
        User u = new User();
        u.setUsername("ryann");
        when(userServices.getAllUsers()).thenReturn(List.of(u));

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(content().json(objectMapper.writeValueAsString(List.of(u))));
    }
}
