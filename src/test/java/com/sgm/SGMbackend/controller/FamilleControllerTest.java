package com.sgm.SGMbackend.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.FamilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FamilleResponseDTO;
import com.sgm.SGMbackend.service.FamilleService;
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

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FamilleController.class)
@AutoConfigureMockMvc(addFilters = false)
class FamilleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FamilleService familleService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("GET /api/familles: Succès")
    void list_Success() throws Exception {
        // Arrange
        Page<FamilleResponseDTO> page = new PageImpl<>(List.of(new FamilleResponseDTO()));
        when(familleService.findAll(any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/familles"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("POST /api/familles: Succès")
    void create_Success() throws Exception {
        // Arrange
        FamilleRequestDTO dto = new FamilleRequestDTO();
        dto.setTelephone("123456");
        when(familleService.create(any(FamilleRequestDTO.class))).thenReturn(new FamilleResponseDTO());

        // Act & Assert
        mockMvc.perform(post("/api/familles")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /api/familles/{id}/depouilles/{depId}: Succès")
    void lierDepouille_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(post("/api/familles/1/depouilles/2"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("GET /api/familles/recherche: Succès")
    void recherche_Success() throws Exception {
        // Arrange
        Page<FamilleResponseDTO> page = new PageImpl<>(List.of(new FamilleResponseDTO()));
        when(familleService.recherche(anyString(), any(Pageable.class))).thenReturn(page);

        // Act & Assert
        mockMvc.perform(get("/api/familles/recherche").param("q", "Doe"))
                .andExpect(status().isOk());
    }
}
