package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import com.sgm.SGMbackend.service.AutopsieService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AutopsieController.class)
@AutoConfigureMockMvc(addFilters = false)
class AutopsieControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AutopsieService autoService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("POST /api/autopsies: Succès")
    void planifier_Success() throws Exception {
        // Arrange
        Map<String, Object> body = Map.of(
                "depouillId", 1,
                "medecinId", "med-1",
                "datePlanifiee", LocalDateTime.now().plusDays(1).toString());
        when(autoService.planifier(anyLong(), anyString(), any(LocalDateTime.class)))
                .thenReturn(new Autopsie());

        // Act & Assert
        mockMvc.perform(post("/api/autopsies")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /api/autopsies/{id}/demarrer: Succès")
    void demarrer_Success() throws Exception {
        // Arrange
        when(autoService.demarrer(1L)).thenReturn(new Autopsie());

        // Act & Assert
        mockMvc.perform(patch("/api/autopsies/1/demarrer"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/autopsies/{id}/terminer: Succès")
    void terminer_Success() throws Exception {
        // Arrange
        Map<String, String> body = Map.of("rapport", "OK", "conclusion", "Naturelle");
        when(autoService.terminer(eq(1L), anyString(), anyString())).thenReturn(new Autopsie());

        // Act & Assert
        mockMvc.perform(patch("/api/autopsies/1/terminer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/autopsies/{id}/analyses: Succès")
    void ajouterAnalyse_Success() throws Exception {
        // Arrange
        Map<String, String> body = Map.of("description", "Examen X");
        when(autoService.ajouterAnalyse(eq(1L), anyString())).thenReturn(new Autopsie());

        // Act & Assert
        mockMvc.perform(post("/api/autopsies/1/analyses")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("DELETE /api/autopsies/{id}: Succès")
    void annuler_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/autopsies/1"))
                .andExpect(status().isNoContent());
    }
}
