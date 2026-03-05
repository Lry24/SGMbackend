package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoResponse.CaisseResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.MouvementCaisseResponseDTO;
import com.sgm.SGMbackend.entity.Caisse;
import com.sgm.SGMbackend.mapper.CaisseMapper;
import com.sgm.SGMbackend.mapper.MouvementCaisseMapper;
import com.sgm.SGMbackend.service.ComptabiliteService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ComptabiliteController.class)
@AutoConfigureMockMvc(addFilters = false)
class ComptabiliteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ComptabiliteService comptabiliteService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @MockBean
    private CaisseMapper caisseMapper;

    @MockBean
    private MouvementCaisseMapper mouvementMapper;

    @Test
    @DisplayName("GET /api/comptabilite/journal: Succès")
    void getJournal_Success() throws Exception {
        // Arrange
        Page<com.sgm.SGMbackend.entity.MouvementCaisse> page = new PageImpl<>(List.of());
        when(comptabiliteService.getJournal(any(), any(), any())).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/comptabilite/journal")
                .param("dateDebut", "2023-01-01T00:00:00")
                .param("dateFin", "2023-12-31T23:59:59"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/comptabilite/caisse/ouvrir: Succès")
    void ouvrirCaisse_Success() throws Exception {
        // Arrange
        Map<String, Double> payload = Map.of("fondCaisse", 500.0);
        CaisseResponseDTO responseDTO = new CaisseResponseDTO();
        responseDTO.setId(1L);
        when(caisseMapper.toResponseDTO(any())).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(post("/api/comptabilite/caisse/ouvrir")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(payload)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    @DisplayName("GET /api/comptabilite/export: Succès CSV")
    void export_Success() throws Exception {
        // Arrange
        when(comptabiliteService.exportJournal(any(), any(), any())).thenReturn("CSV data".getBytes());

        // Act & Assert
        mockMvc.perform(get("/api/comptabilite/export")
                .param("format", "CSV")
                .param("dateDebut", "2023-01-01T00:00:00")
                .param("dateFin", "2023-01-01T23:59:59"))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", org.hamcrest.Matchers.containsString("journal")))
                .andExpect(content().contentType("text/csv"));
    }

    @Test
    @DisplayName("GET /api/comptabilite/caisse: Succès avec DTO conversion")
    void getCaisse_Success() throws Exception {
        // Arrange
        Map<String, Object> report = new HashMap<>();
        report.put("encaissements", 1000.0);
        report.put("mouvements", List.of(new com.sgm.SGMbackend.entity.MouvementCaisse()));

        MouvementCaisseResponseDTO responseDTO = new MouvementCaisseResponseDTO();
        responseDTO.setId(1L);

        when(comptabiliteService.getCaisseJournaliere(any())).thenReturn(report);
        when(mouvementMapper.toResponseDTO(any())).thenReturn(responseDTO);

        // Act & Assert
        mockMvc.perform(get("/api/comptabilite/caisse")
                .param("date", "2023-01-01T00:00:00"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.encaissements").value(1000.0))
                .andExpect(jsonPath("$.mouvements[0].id").value(1));
    }
}
