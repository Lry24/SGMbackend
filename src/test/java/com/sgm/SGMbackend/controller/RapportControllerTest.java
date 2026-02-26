package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.service.RapportService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Map;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(RapportController.class)
@AutoConfigureMockMvc(addFilters = false)
class RapportControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RapportService rapportService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("GET /api/rapports/dashboard: Succès")
    void dashboard_Success() throws Exception {
        // Arrange
        when(rapportService.getDashboardKpis()).thenReturn(Map.of("kpi", 1));

        // Act & Assert
        mockMvc.perform(get("/api/rapports/dashboard"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/rapports/occupation: Succès")
    void occupation_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(get("/api/rapports/occupation")
                .param("dateDebut", "2023-01-01")
                .param("dateFin", "2023-01-31"))
                .andExpect(status().isOk());
    }
}
