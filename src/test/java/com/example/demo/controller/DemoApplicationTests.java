package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.service.UserService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Arrays;
import java.util.Optional;
import java.util.UUID;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserService userService;

    @Autowired
    private ObjectMapper objectMapper;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(UUID.randomUUID());
        user.setUsername("John Doe");
        user.setEmail("john.doe@example.com");
    }

    @Test
    void getAllUsers_shouldReturnListOfUsers() throws Exception {
        Mockito.when(userService.getAllUsers()).thenReturn(Arrays.asList(user));

        mockMvc.perform(get("/api/users")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].username", is(user.getUsername())))
                .andExpect(jsonPath("$[0].email", is(user.getEmail())));
    }

    @Test
    void getUserById_shouldReturnUser() throws Exception {
        Mockito.when(userService.getUserById(user.getId())).thenReturn(Optional.of(user));

        mockMvc.perform(get("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void getUserById_shouldReturnNotFound() throws Exception {
        Mockito.when(userService.getUserById(user.getId())).thenReturn(Optional.empty());

        mockMvc.perform(get("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

    @Test
    void createUser_shouldReturnCreatedUser() throws Exception {
        Mockito.when(userService.createUser(Mockito.any(User.class))).thenReturn(user);

        mockMvc.perform(post("/api/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk()) // Default status for Spring's @PostMapping is 200 OK
                .andExpect(jsonPath("$.username", is(user.getUsername())))
                .andExpect(jsonPath("$.email", is(user.getEmail())));
    }

    @Test
    void updateUser_shouldUpdateAndReturnUser() throws Exception {
        User updatedUser = new User();
        updatedUser.setId(user.getId());
        updatedUser.setUsername("Jane Doe");
        updatedUser.setEmail("jane.doe@example.com");

        Mockito.when(userService.updateUser(Mockito.eq(user.getId()), Mockito.any(User.class))).thenReturn(updatedUser);

        mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username", is(updatedUser.getUsername())))
                .andExpect(jsonPath("$.email", is(updatedUser.getEmail())));
    }

    @Test
    void updateUser_shouldReturnNotFound() throws Exception {
        Mockito.when(userService.updateUser(Mockito.eq(user.getId()), Mockito.any(User.class)))
                .thenThrow(new RuntimeException("User not found"));

        User updatedUser = new User();
        updatedUser.setUsername("Jane Doe");
        updatedUser.setEmail("jane.doe@example.com");

        mockMvc.perform(put("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updatedUser)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteUser_shouldReturnNoContent() throws Exception {
        Mockito.doNothing().when(userService).deleteUser(user.getId());

        mockMvc.perform(delete("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteUser_shouldReturnNotFound() throws Exception {
        Mockito.doThrow(new RuntimeException("User not found"))
                .when(userService).deleteUser(Mockito.eq(user.getId()));

        mockMvc.perform(delete("/api/users/{id}", user.getId())
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound());
    }

}
