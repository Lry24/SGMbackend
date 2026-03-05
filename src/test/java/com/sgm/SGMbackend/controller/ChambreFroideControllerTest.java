package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.ChambreFroideRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.ChambreFroideResponseDTO;
import com.sgm.SGMbackend.entity.ChambreFroide;
import com.sgm.SGMbackend.mapper.ChambreFroideMapper;
import com.sgm.SGMbackend.mapper.EmplacementMapper;
import com.sgm.SGMbackend.service.ChambreFroideService;
import com.sgm.SGMbackend.service.EmplacementService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ChambreFroideController.class)
@AutoConfigureMockMvc(addFilters = false)
class ChambreFroideControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ChambreFroideService chambreFroideService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @MockBean
    private ChambreFroideMapper chambreFroideMapper;

    @MockBean
    private EmplacementService emplacementService;

    @MockBean
    private EmplacementMapper emplacementMapper;

    @Test
    @DisplayName("GET /api/chambres: Succès")
    void findAll_Success() throws Exception {
        // Arrange
        when(chambreFroideService.findAll()).thenReturn(List.of(new ChambreFroide()));
        when(chambreFroideMapper.toResponseDTO(any(ChambreFroide.class)))
                .thenReturn(ChambreFroideResponseDTO.builder().numero("C1").build());

        // Act & Assert
        mockMvc.perform(get("/api/chambres"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].numero").value("C1"));
    }

    @Test
    @DisplayName("POST /api/chambres: Succès")
    void creer_Success() throws Exception {
        // Arrange
        ChambreFroideRequestDTO dto = new ChambreFroideRequestDTO();
        dto.setNumero("C1");
        dto.setCapacite(5);

        when(chambreFroideService.creer(anyString(), anyInt(), anyFloat())).thenReturn(new ChambreFroide());
        when(chambreFroideMapper.toResponseDTO(any(ChambreFroide.class)))
                .thenReturn(ChambreFroideResponseDTO.builder().id(1L).build());

        // Act & Assert
        mockMvc.perform(post("/api/chambres")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PATCH /api/chambres/{id}/temperature: Succès")
    void enregistrerTemperature_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/chambres/1/temperature")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("temperature", -5.0f))))
                .andExpect(status().isOk());
    }
}
