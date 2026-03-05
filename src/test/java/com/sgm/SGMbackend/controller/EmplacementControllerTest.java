package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoResponse.EmplacementResponseDTO;
import com.sgm.SGMbackend.entity.Emplacement;
import com.sgm.SGMbackend.mapper.EmplacementMapper;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmplacementController.class)
@AutoConfigureMockMvc(addFilters = false)
class EmplacementControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmplacementService emplacementService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @MockBean
    private EmplacementMapper emplacementMapper;

    @Test
    @DisplayName("GET /api/emplacements/disponibles: Succès")
    void findDisponibles_Success() throws Exception {
        // Arrange
        when(emplacementService.findDisponibles()).thenReturn(List.of(new Emplacement()));
        when(emplacementMapper.toResponseDTO(any(Emplacement.class)))
                .thenReturn(EmplacementResponseDTO.builder().code("C1-E1").build());

        // Act & Assert
        mockMvc.perform(get("/api/emplacements/disponibles"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].code").value("C1-E1"));
    }

    @Test
    @DisplayName("POST /api/emplacements/affecter: Succès")
    void affecter_Success() throws Exception {
        // Arrange
        Map<String, Long> body = Map.of("depouillId", 1L, "emplacementId", 2L);
        when(emplacementService.affecter(anyLong(), anyLong())).thenReturn(new Emplacement());

        // Act & Assert
        mockMvc.perform(post("/api/emplacements/affecter")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("PATCH /api/emplacements/{id}/liberer: Succès")
    void liberer_Success() throws Exception {
        // Arrange
        Map<String, String> body = Map.of("motif", "Transfert");
        when(emplacementService.liberer(anyLong(), any())).thenReturn(new Emplacement());

        // Act & Assert
        mockMvc.perform(patch("/api/emplacements/1/liberer")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(body)))
                .andExpect(status().isOk());
    }
}
