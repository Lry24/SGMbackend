package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.FactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.LigneFactureMapper;
import com.sgm.SGMbackend.mapper.MouvementCaisseMapper;
import com.sgm.SGMbackend.service.FactureService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FactureController.class)
@AutoConfigureMockMvc(addFilters = false)
class FactureControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FactureService factureService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @MockBean
    private FactureMapper factureMapper;

    @MockBean
    private LigneFactureMapper ligneMapper;

    @MockBean
    private MouvementCaisseMapper mouvementMapper;

    @Test
    @DisplayName("POST /api/factures: Succès")
    void creer_Success() throws Exception {
        // Arrange
        FactureRequestDTO dto = new FactureRequestDTO();
        dto.setDepouilleId(1L);
        dto.setFamilleId(2L);
        dto.setLignes(new ArrayList<>());

        Facture f = new Facture();
        when(factureService.creer(anyLong(), anyLong(), anyList(), any())).thenReturn(f);
        when(factureMapper.toResponseDTO(f)).thenReturn(FactureResponseDTO.builder().id(1L).build());

        // Act & Assert
        mockMvc.perform(post("/api/factures")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("POST /api/factures/{id}/paiements: Succès")
    void enregistrerPaiement_Success() throws Exception {
        // Arrange
        Map<String, Object> body = Map.of("montant", 5000, "mode", "CASH");
        when(factureService.enregistrerPaiement(eq(1L), anyDouble(), anyString(), anyString()))
                .thenReturn(new Facture());

        // Act & Assert
        mockMvc.perform(post("/api/factures/1/paiements")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("PATCH /api/factures/{id}/emettre: Succès")
    void emettre_Success() throws Exception {
        // Arrange
        when(factureService.emettre(1L)).thenReturn(new Facture());

        // Act & Assert
        mockMvc.perform(patch("/api/factures/1/emettre"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/factures/{id}/pdf: Succès")
    void getPdf_Success() throws Exception {
        // Arrange
        when(factureService.generatePdf(1L)).thenReturn(new byte[] { 1, 2, 3 });
        when(factureService.findById(1L)).thenReturn(Facture.builder().numero("FAC-001").build());

        // Act & Assert
        mockMvc.perform(get("/api/factures/1/pdf"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_PDF));
    }
}
