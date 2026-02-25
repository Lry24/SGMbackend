package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.config.SupabaseConfig;
import com.sgm.SGMbackend.dto.dtoRequest.UtilisateurRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.entity.enums.Role;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.UtilisateurMapper;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UtilisateurServiceImplTest {

    @InjectMocks
    private UtilisateurServiceImpl utilisateurService;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @Mock
    private SupabaseConfig supabaseConfig;

    @Mock
    private RestTemplate restTemplate;

    @Test
    @DisplayName("findById doit retourner un DTO quand l'utilisateur existe")
    void findById_Success() {
        // Arrange
        String id = "user-123";
        Utilisateur utilisateur = Utilisateur.builder().id(id).email("test@test.com").build();
        UtilisateurResponseDTO expectedDTO = UtilisateurResponseDTO.builder().id(id).email("test@test.com").build();

        when(utilisateurRepository.findById(id)).thenReturn(Optional.of(utilisateur));
        when(utilisateurMapper.toResponseDTO(utilisateur)).thenReturn(expectedDTO);

        // Act
        UtilisateurResponseDTO result = utilisateurService.findById(id);

        // Assert
        assertNotNull(result);
        assertEquals(id, result.getId());
        verify(utilisateurRepository).findById(id);
    }

    @Test
    @DisplayName("findById doit lever ResourceNotFoundException quand l'utilisateur n'existe pas")
    void findById_NotFound() {
        // Arrange
        String id = "unknown";
        when(utilisateurRepository.findById(id)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> utilisateurService.findById(id));
    }

    @Test
    @DisplayName("create doit créer un utilisateur dans Supabase et en DB")
    @SuppressWarnings("unchecked")
    void create_Success() {
        // Arrange
        UtilisateurRequestDTO dto = UtilisateurRequestDTO.builder()
                .nom("Nom")
                .prenom("Prenom")
                .email("new@test.com")
                .role(Role.AGENT)
                .password("Pass123!")
                .build();

        String supabaseId = "uuid-supabase";
        when(utilisateurRepository.findByEmail(dto.getEmail())).thenReturn(Optional.empty());
        when(supabaseConfig.getAuthUrl()).thenReturn("http://supabase.com/auth");
        when(supabaseConfig.getServiceKey()).thenReturn("secret-key");

        // Mock RestTemplate pour l'appel Supabase Admin
        Map<String, Object> mockResponse = Map.of("id", supabaseId);
        ResponseEntity<Map> responseEntity = ResponseEntity.ok(mockResponse);
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(responseEntity);

        Utilisateur savedUser = Utilisateur.builder().id(supabaseId).email(dto.getEmail()).build();
        when(utilisateurRepository.save(any(Utilisateur.class))).thenReturn(savedUser);
        when(utilisateurMapper.toResponseDTO(any(Utilisateur.class)))
                .thenReturn(UtilisateurResponseDTO.builder().id(supabaseId).build());

        // Act
        UtilisateurResponseDTO result = utilisateurService.create(dto);

        // Assert
        assertNotNull(result);
        assertEquals(supabaseId, result.getId());
        verify(restTemplate).exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class));
        verify(utilisateurRepository).save(any(Utilisateur.class));
    }
}
