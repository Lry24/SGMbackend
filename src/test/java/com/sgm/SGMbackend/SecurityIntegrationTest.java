package com.sgm.SGMbackend;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.dto.dtoRequest.LoginRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.entity.enums.Role;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import com.sgm.SGMbackend.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @MockBean
    private AuthService authService;

    @BeforeEach
    void setup() {
        utilisateurRepository.deleteAll();

        // Créer un ADMIN
        Utilisateur admin = Utilisateur.builder()
                .id("admin-id")
                .email("admin@sgm.com")
                .nom("Admin")
                .prenom("Super")
                .role(Role.ADMIN)
                .actif(true)
                .build();
        utilisateurRepository.save(admin);

        // Créer un AGENT
        Utilisateur agent = Utilisateur.builder()
                .id("agent-id")
                .email("agent@sgm.com")
                .nom("Agent")
                .prenom("Simple")
                .role(Role.AGENT)
                .actif(true)
                .build();
        utilisateurRepository.save(agent);
    }

    @Test
    void testFullAuthFlow() throws Exception {
        // Mocking AuthService behavior
        when(authService.login(anyString(), anyString()))
                .thenReturn(Map.of("accessToken", "mock-token"));

        when(authService.getCurrentUser())
                .thenReturn(UtilisateurResponseDTO.builder()
                        .email("admin@sgm.com")
                        .role(Role.ADMIN)
                        .build());

        // 1. Login
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail("admin@sgm.com");
        loginReq.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").exists())
                .andReturn();

        String responseStr = result.getResponse().getContentAsString();
        Map<String, String> responseMap = objectMapper.readValue(responseStr, Map.class);
        String token = responseMap.get("accessToken");

        // 2. GET /api/auth/me
        mockMvc.perform(get("/api/auth/me")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("admin@sgm.com"))
                .andExpect(jsonPath("$.role").value("ADMIN"));
    }

    @Test
    void testUnauthorizedAccess() throws Exception {
        // Appel sans token
        mockMvc.perform(get("/api/auth/me"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    void testForbiddenUsage() throws Exception {
        // 1. Login en tant qu'AGENT
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail("agent@sgm.com");
        loginReq.setPassword("password123");

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andReturn();

        Map<String, String> responseMap = objectMapper.readValue(result.getResponse().getContentAsString(), Map.class);
        String token = responseMap.get("accessToken");

        // 2. Tenter d'accéder à la liste des utilisateurs (ADMIN only)
        mockMvc.perform(get("/api/utilisateurs")
                .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }
}
