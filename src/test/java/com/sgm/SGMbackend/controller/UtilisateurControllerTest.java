package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.UtilisateurRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.enums.Role;
import com.sgm.SGMbackend.service.UtilisateurService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UtilisateurController.class)
@AutoConfigureMockMvc(addFilters = false)
class UtilisateurControllerTest {

        @Autowired
        private MockMvc mockMvc;

        @Autowired
        private ObjectMapper objectMapper;

        @MockBean
        private UtilisateurService utilisateurService;

        @MockBean
        private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

        @Test
        @DisplayName("GET /api/utilisateurs: Succès")
        void list_Success() throws Exception {
                // Arrange
                Page<UtilisateurResponseDTO> page = new PageImpl<>(List.of(
                                UtilisateurResponseDTO.builder().email("user1@test.com").build()));
                when(utilisateurService.findAll(any(Pageable.class), any())).thenReturn(page);

                // Act & Assert
                mockMvc.perform(get("/api/utilisateurs"))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.content[0].email").value("user1@test.com"));
        }

        @Test
        @DisplayName("GET /api/utilisateurs/{id}: Succès")
        void getById_Success() throws Exception {
                // Arrange
                String id = "user-123";
                UtilisateurResponseDTO user = UtilisateurResponseDTO.builder().id(id).email("test@test.com").build();
                when(utilisateurService.findById(id)).thenReturn(user);

                // Act & Assert
                mockMvc.perform(get("/api/utilisateurs/" + id))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.id").value(id))
                                .andExpect(jsonPath("$.email").value("test@test.com"));
        }

        @Test
        @DisplayName("POST /api/utilisateurs: Succès")
        void create_Success() throws Exception {
                // Arrange
                UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
                                .nom("Nom")
                                .prenom("Prenom")
                                .email("new@test.com")
                                .role(Role.AGENT)
                                .build();
                UtilisateurResponseDTO response = UtilisateurResponseDTO.builder().id("new-id").email("new@test.com")
                                .build();
                when(utilisateurService.create(any(UtilisateurRequestDTO.class))).thenReturn(response);

                // Act & Assert
                mockMvc.perform(post("/api/utilisateurs")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isCreated())
                                .andExpect(jsonPath("$.id").value("new-id"));
        }

        @Test
        @DisplayName("PUT /api/utilisateurs/{id}: Succès")
        void update_Success() throws Exception {
                // Arrange
                String id = "user-123";
                UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
                                .nom("Updated")
                                .build();
                UtilisateurResponseDTO response = UtilisateurResponseDTO.builder().id(id).nom("Updated").build();
                when(utilisateurService.update(eq(id), any(UtilisateurRequestDTO.class))).thenReturn(response);

                // Act & Assert
                mockMvc.perform(put("/api/utilisateurs/" + id)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(dto)))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.nom").value("Updated"));
        }

        @Test
        @DisplayName("DELETE /api/utilisateurs/{id}: Succès")
        void delete_Success() throws Exception {
                // Act & Assert
                mockMvc.perform(delete("/api/utilisateurs/user-123"))
                                .andExpect(status().isNoContent());
        }

        @Test
        @DisplayName("PATCH /api/utilisateurs/{id}/activer: Succès")
        void activer_Success() throws Exception {
                // Act & Assert
                mockMvc.perform(patch("/api/utilisateurs/user-123/activer")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(Map.of("actif", true))))
                                .andExpect(status().isOk());
        }

        @Test
        @DisplayName("POST /api/utilisateurs/{id}/reset-password: Succès")
        void resetPassword_Success() throws Exception {
                // Act & Assert
                mockMvc.perform(post("/api/utilisateurs/user-123/reset-password"))
                                .andExpect(status().isOk());
        }
}
