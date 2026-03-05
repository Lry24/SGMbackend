package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.DepouilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.mapper.DepouilleMapper;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.service.DepouilleService;
import com.sgm.SGMbackend.service.FactureService;
import com.sgm.SGMbackend.service.RestitutionService;
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

@WebMvcTest(DepouilleController.class)
@AutoConfigureMockMvc(addFilters = false)
class DepouilleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DepouilleService depouilleService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @MockBean
    private DepouilleMapper depouilleMapper;

    @MockBean
    private RestitutionService restitutionService;

    @MockBean
    private FactureService factureService;

    @MockBean
    private FactureMapper factureMapper;

    @Test
    @DisplayName("GET /api/depouilles: Succès")
    void findAll_Success() throws Exception {
        // Arrange
        Page<Depouille> page = new PageImpl<>(List.of(new Depouille()));
        when(depouilleService.findAll(any(Pageable.class), any(), any())).thenReturn(page);
        when(depouilleMapper.toResponseDTO(any(Depouille.class)))
                .thenReturn(DepouilleResponseDTO.builder().identifiantUnique("SGM-123").build());

        // Act & Assert
        mockMvc.perform(get("/api/depouilles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].identifiantUnique").value("SGM-123"));
    }

    @Test
    @DisplayName("POST /api/depouilles: Succès")
    void enregistrer_Success() throws Exception {
        // Arrange
        DepouilleRequestDTO dto = new DepouilleRequestDTO();
        Depouille d = new Depouille();
        when(depouilleMapper.toEntity(any(DepouilleRequestDTO.class))).thenReturn(d);
        when(depouilleService.enregistrer(any(Depouille.class))).thenReturn(d);
        when(depouilleMapper.toResponseDTO(d)).thenReturn(DepouilleResponseDTO.builder().id(1L).build());

        // Act & Assert
        mockMvc.perform(post("/api/depouilles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("PATCH /api/depouilles/{id}/statut: Succès")
    void changerStatut_Success() throws Exception {
        // Arrange
        Long id = 1L;
        StatutDepouille statut = StatutDepouille.EN_CHAMBRE_FROIDE;
        when(depouilleService.changerStatut(eq(id), eq(statut))).thenReturn(new Depouille());

        // Act & Assert
        mockMvc.perform(patch("/api/depouilles/" + id + "/statut")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("statut", statut))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/depouilles/{id}/qrcode: Succès")
    void getQRCode_Success() throws Exception {
        // Arrange
        when(depouilleService.getQRCode(1L)).thenReturn(new byte[] { 1, 2, 3 });

        // Act & Assert
        mockMvc.perform(get("/api/depouilles/1/qrcode"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }
}
