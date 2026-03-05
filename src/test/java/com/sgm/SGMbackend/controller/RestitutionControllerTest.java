package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.service.RestitutionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RestitutionController.class)
@AutoConfigureMockMvc(addFilters = false)
class RestitutionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private RestitutionService restitutionService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("POST /api/restitutions: Succès")
    void planifier_Success() throws Exception {
        // Arrange
        RestitutionRequestDTO dto = new RestitutionRequestDTO();
        dto.setDepouilleId(1L);
        when(restitutionService.planifier(any())).thenReturn(new RestitutionResponseDTO());

        // Act & Assert
        mockMvc.perform(post("/api/restitutions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /api/restitutions/{id}/confirmer: Succès")
    void confirmer_Success() throws Exception {
        // Arrange
        when(restitutionService.confirmer(1L)).thenReturn(new RestitutionResponseDTO());

        // Act & Assert
        mockMvc.perform(patch("/api/restitutions/1/confirmer"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/restitutions/{id}/effectuer: Succès")
    void effectuer_Success() throws Exception {
        // Arrange
        Map<String, String> body = Map.of("pieceIdentiteRef", "REF-123");
        when(restitutionService.effectuer(eq(1L), eq("REF-123"))).thenReturn(new RestitutionResponseDTO());

        // Act & Assert
        mockMvc.perform(patch("/api/restitutions/1/effectuer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}
