package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.config.SupabaseConfig;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.UtilisateurMapper;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private SupabaseConfig supabaseConfig;

    @Mock
    private UtilisateurRepository utilisateurRepository;

    @Mock
    private UtilisateurMapper utilisateurMapper;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    @BeforeEach
    void setUp() {
        lenient().when(supabaseConfig.getAuthUrl()).thenReturn("http://supabase-auth.com");
        lenient().when(supabaseConfig.getAnonKey()).thenReturn("anon-key");
    }

    @Test
    @DisplayName("login: Succès")
    void login_Success() {
        // Arrange
        String email = "test@test.com";
        String password = "password";
        Map<String, Object> mockResponse = new HashMap<>();
        mockResponse.put("access_token", "abc");

        Utilisateur user = Utilisateur.builder().email(email).doitChangerMotDePasse(false).build();

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));
        when(utilisateurRepository.findByEmail(email)).thenReturn(Optional.of(user));

        // Act
        Map<String, Object> result = authService.login(email, password);

        // Assert
        assertNotNull(result);
        assertEquals("abc", result.get("access_token"));
        assertEquals(false, result.get("must_change_password"));
        verify(utilisateurRepository).save(user);
    }

    @Test
    @DisplayName("login: Échec (Identifiants invalides)")
    void login_Failure() {
        // Arrange
        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        // Act & Assert
        assertThrows(BusinessRuleException.class, () -> authService.login("wrong@test.com", "wrong"));
    }

    @Test
    @DisplayName("refresh: Succès")
    void refresh_Success() {
        // Arrange
        String refreshToken = "refresh-token";
        Map<String, Object> mockResponse = Map.of("access_token", "new-abc");

        when(restTemplate.exchange(anyString(), eq(HttpMethod.POST), any(HttpEntity.class), eq(Map.class)))
                .thenReturn(ResponseEntity.ok(mockResponse));

        // Act
        Map<String, Object> result = authService.refresh(refreshToken);

        // Assert
        assertNotNull(result);
        assertEquals("new-abc", result.get("access_token"));
    }

    @Test
    @DisplayName("getCurrentUser: Succès")
    void getCurrentUser_Success() {
        // Arrange
        String userId = "user-123";
        Utilisateur user = Utilisateur.builder().id(userId).email("test@test.com").build();
        UtilisateurResponseDTO responseDTO = UtilisateurResponseDTO.builder().id(userId).email("test@test.com").build();

        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getPrincipal()).thenReturn(user);

        when(utilisateurRepository.findById(userId)).thenReturn(Optional.of(user));
        when(utilisateurMapper.toResponseDTO(user)).thenReturn(responseDTO);

        // Act
        UtilisateurResponseDTO result = authService.getCurrentUser();

        // Assert
        assertNotNull(result);
        assertEquals(userId, result.getId());
        SecurityContextHolder.clearContext();
    }

    @Test
    @DisplayName("getCurrentUser: Utilisateur introuvable")
    void getCurrentUser_NotFound() {
        // Arrange
        String userId = "unknown";
        Authentication auth = mock(Authentication.class);
        SecurityContext securityContext = mock(SecurityContext.class);
        when(securityContext.getAuthentication()).thenReturn(auth);
        SecurityContextHolder.setContext(securityContext);
        when(auth.getPrincipal()).thenReturn(userId);

        when(utilisateurRepository.findById(userId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> authService.getCurrentUser());
        SecurityContextHolder.clearContext();
    }
}
