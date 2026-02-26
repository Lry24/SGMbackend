package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.LoginRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false) // Désactiver la sécurité pour les tests unitaires du controller
class AuthControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private AuthService authService;

        @MockBean
        private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

        @Test
        @DisplayName("POST /api/auth/login: Succès")
        void login_Success() throws Exception {
                // Arrange
                LoginRequestDTO req = new LoginRequestDTO();
                req.setEmail("test@test.com");
                req.setPassword("password");

                when(authService.login(anyString(), anyString()))
                                .thenReturn(Map.of("accessToken", "token-123"));

                // Act & Assert
                mockMvc.perform(post("/api/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(req)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.accessToken").value("token-123"));
        }

        @Test
        @DisplayName("GET /api/auth/me: Succès")
        void me_Success() throws Exception {
                // Arrange
                UtilisateurResponseDTO user = UtilisateurResponseDTO.builder()
                                .email("test@test.com")
                                .nom("Test")
                                .build();

                when(authService.getCurrentUser()).thenReturn(user);

                // Act & Assert
                mockMvc.perform(get("/api/auth/me"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.email").value("test@test.com"))
                                .andExpect(jsonPath("$.nom").value("Test"));
        }

        @Test
        @DisplayName("POST /api/auth/logout: Succès")
        void logout_Success() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/auth/logout"))
                                .andExpect(status().isOk());
        }
}
