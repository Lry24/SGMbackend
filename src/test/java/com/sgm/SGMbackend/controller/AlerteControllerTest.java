package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteResponseDTO;
import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import com.sgm.SGMbackend.service.AlerteService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AlerteController.class)
@AutoConfigureMockMvc(addFilters = false)
class AlerteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AlerteService alerteService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("GET /api/alertes: Succès")
    void list_Success() throws Exception {
        // Arrange
        Page<AlerteResponseDTO> page = new PageImpl<>(List.of(
                AlerteResponseDTO.builder().id(1L).message("Alerte 1").build()));
        when(alerteService.findAll(any(), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/alertes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].message").value("Alerte 1"));
    }

    @Test
    @DisplayName("PATCH /api/alertes/{id}/acquitter: Succès")
    void acquitter_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(patch("/api/alertes/1/acquitter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(Map.of("commentaire", "OK"))))
                .andExpect(status().isOk());
    }
}
