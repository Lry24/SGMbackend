package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.config.SupabaseConfig;
import com.sgm.SGMbackend.entity.Document;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DocumentRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;
    @Mock
    private SupabaseConfig supabaseConfig;
    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private DocumentService documentService;

    @Test
    @DisplayName("upload: Succès")
    void upload_Success() throws Exception {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "test.pdf", "application/pdf", "dummy content".getBytes());

        when(supabaseConfig.getSupabaseUrl()).thenReturn("http://supabase.com");
        when(supabaseConfig.getServiceKey()).thenReturn("key");
        when(documentRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        // Act
        Document result = documentService.upload(file, "ID", "DEPOUILLE", 1L);

        // Assert
        assertNotNull(result);
        assertEquals("test.pdf", result.getNomFichier());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(), eq(String.class));
        verify(documentRepository).save(any());
    }

    @Test
    @DisplayName("upload: Échec (Type non autorisé)")
    void upload_Failure_InvalidType() {
        // Arrange
        MockMultipartFile file = new MockMultipartFile(
                "file", "script.sh", "text/x-shellscript", "echo hello".getBytes());

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> documentService.upload(file, "ID", "DEPOUILLE", 1L));
    }

    @Test
    @DisplayName("genererLienTelecharge: Succès")
    void genererLienTelecharge_Success() {
        // Arrange
        Long id = 1L;
        Document doc = Document.builder().id(id).cheminStorage("path/to/file").build();
        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));
        when(supabaseConfig.getSupabaseUrl()).thenReturn("http://supabase.com");

        Map<String, String> body = Map.of("signedURL", "/signed-path");
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(body));

        // Act
        String link = documentService.genererLienTelecharge(id);

        // Assert
        assertNotNull(link);
        assertTrue(link.contains("/signed-path"));
    }

    @Test
    @DisplayName("supprimer: Succès")
    void supprimer_Success() {
        // Arrange
        Long id = 1L;
        Document doc = Document.builder().id(id).cheminStorage("path/to/file").build();
        when(documentRepository.findById(id)).thenReturn(Optional.of(doc));
        when(supabaseConfig.getSupabaseUrl()).thenReturn("http://supabase.com");

        // Act
        documentService.supprimer(id);

        // Assert
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.DELETE), any(), eq(String.class));
        verify(documentRepository).delete(doc);
    }
}
