package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.BaremeRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.BaremeResponseDTO;
import com.sgm.SGMbackend.entity.Bareme;
import com.sgm.SGMbackend.mapper.BaremeMapper;
import com.sgm.SGMbackend.service.BaremeService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(BaremeController.class)
@AutoConfigureMockMvc(addFilters = false)
class BaremeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private BaremeService baremeService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @MockBean
    private BaremeMapper baremeMapper;

    @Test
    @DisplayName("GET /api/bareme: Succès")
    void getActifs_Success() throws Exception {
        // Arrange
        when(baremeService.findAllActifs()).thenReturn(List.of(new Bareme()));
        when(baremeMapper.toResponseDTO(any(Bareme.class))).thenReturn(new BaremeResponseDTO());

        // Act & Assert
        mockMvc.perform(get("/api/bareme"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/bareme: Succès")
    void creer_Success() throws Exception {
        // Arrange
        BaremeRequestDTO dto = new BaremeRequestDTO();
        dto.setNom("Test");
        Bareme b = new Bareme();
        when(baremeMapper.toEntity(any(BaremeRequestDTO.class))).thenReturn(b);
        when(baremeService.creer(any(Bareme.class))).thenReturn(b);

        // Act & Assert
        mockMvc.perform(post("/api/bareme")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("DELETE /api/bareme/{id}: Succès")
    void supprimer_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/bareme/1"))
                .andExpect(status().isNoContent());
    }
}
