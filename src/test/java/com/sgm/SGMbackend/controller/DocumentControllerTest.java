package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.entity.Document;
import com.sgm.SGMbackend.service.DocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(DocumentController.class)
@AutoConfigureMockMvc(addFilters = false)
class DocumentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DocumentService documentService;

    @MockBean
    private com.sgm.SGMbackend.repository.UtilisateurRepository utilisateurRepository;

    @Test
    @DisplayName("POST /api/documents/upload: Succès")
    void upload_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile("file", "test.pdf", "application/pdf", "data".getBytes());
        when(documentService.upload(any(), anyString(), anyString(), anyLong())).thenReturn(new Document());

        // Act & Assert
        mockMvc.perform(multipart("/api/documents/upload")
                .file(file)
                .param("typeDocument", "ID")
                .param("entiteType", "DEPOUILLE")
                .param("entiteId", "1"))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("GET /api/documents/{id}/telecharger: Succès")
    void telecharger_Success() throws Exception {
        // Arrange
        when(documentService.genererLienTelecharge(1L)).thenReturn("http://signed-url");

        // Act & Assert
        mockMvc.perform(get("/api/documents/1/telecharger"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("http://signed-url"));
    }

    @Test
    @DisplayName("DELETE /api/documents/{id}: Succès")
    void supprimer_Success() throws Exception {
        // Act & Assert
        mockMvc.perform(delete("/api/documents/1"))
                .andExpect(status().isNoContent());
    }
}
