package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.config.SupabaseConfig;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.UtilisateurMapper;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import com.sgm.SGMbackend.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Implémentation du service d'authentification.
 * Ce service s'appuie sur Supabase Auth pour la gestion des sessions et des
 * identifiants.
 * Il assure également la synchronisation des données de connexion avec la base
 * de données locale (PostgreSQL).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final SupabaseConfig supabaseConfig;
    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final RestTemplate restTemplate;

    /**
     * Authentifie un utilisateur via l'API REST de Supabase Auth.
     * En cas de succès, met à jour la date de dernière connexion en base locale.
     * 
     * @param email    Email de l'utilisateur
     * @param password Mot de passe
     * @return Map contenant le access_token, refresh_token et les métadonnées
     *         utilisateur
     * @throws BusinessRuleException si les identifiants sont invalides
     */
    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> login(String email, String password) {
        String url = supabaseConfig.getAuthUrl() + "/token?grant_type=password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getAnonKey());

        Map<String, String> body = Map.of("email", email, "password", password);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            Map<String, Object> responseBody = response.getBody();

            // Mettre à jour derniereConnexion et récupérer doitChangerMotDePasse
            utilisateurRepository.findByEmail(email).ifPresent(u -> {
                u.setDerniereConnexion(LocalDateTime.now());
                utilisateurRepository.save(u);
                if (responseBody != null) {
                    responseBody.put("must_change_password", u.getDoitChangerMotDePasse());
                }
            });

            return responseBody;
        } catch (HttpClientErrorException e) {
            throw new BusinessRuleException("Identifiants invalides.");
        }
    }

    /**
     * Déconnecte l'utilisateur en invalidant son token auprès de Supabase
     * et en nettoyant le contexte de sécurité local.
     */
    @Override
    public void logout() {
        String token = getCurrentToken();
        String url = supabaseConfig.getAuthUrl() + "/logout";

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + token);
        headers.set("apikey", supabaseConfig.getAnonKey());

        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(headers), Void.class);
        } catch (Exception e) {
            log.warn("Erreur lors du logout Supabase : {}", e.getMessage());
        }
        SecurityContextHolder.clearContext();
    }

    // ─── Refresh ──────────────────────────────────────────────────────────────

    @Override
    @SuppressWarnings("unchecked")
    public Map<String, Object> refresh(String refreshToken) {
        String url = supabaseConfig.getAuthUrl() + "/token?grant_type=refresh_token";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getAnonKey());

        Map<String, String> body = Map.of("refresh_token", refreshToken);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            ResponseEntity<Map> response = restTemplate.exchange(url, HttpMethod.POST, request, Map.class);
            return response.getBody();
        } catch (HttpClientErrorException e) {
            throw new BusinessRuleException("Token de rafraîchissement invalide ou expiré.");
        }
    }

    // ─── Me ───────────────────────────────────────────────────────────────────

    @Override
    public UtilisateurResponseDTO getCurrentUser() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        String userId;
        if (principal instanceof Utilisateur) {
            userId = ((Utilisateur) principal).getId();
        } else {
            userId = principal.toString();
        }

        Utilisateur u = utilisateurRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable en base : " + userId));
        return utilisateurMapper.toResponseDTO(u);
    }

    // ─── Change Password ──────────────────────────────────────────────────────

    @Override
    public void changePassword(String oldPassword, String newPassword) {
        String token = getCurrentToken();
        log.debug("Tentative de changement de mot de passe avec token (début) : {}...",
                token.length() > 10 ? token.substring(0, 10) : "trop court");

        String url = supabaseConfig.getAuthUrl() + "/user";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Authorization", "Bearer " + token);
        headers.set("apikey", supabaseConfig.getAnonKey());

        Map<String, String> body = Map.of("password", newPassword);
        HttpEntity<Map<String, String>> request = new HttpEntity<>(body, headers);

        try {
            restTemplate.exchange(url, HttpMethod.PUT, request, Void.class);
            log.info("Mot de passe mis à jour avec succès dans Supabase.");

            // Reset du flag doitChangerMotDePasse en DB locale
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof Utilisateur) {
                Utilisateur u = (Utilisateur) principal;
                u.setDoitChangerMotDePasse(false);
                utilisateurRepository.save(u);
            }
        } catch (HttpClientErrorException e) {
            log.error("Erreur Supabase lors du changement de mot de passe : {} - Body: {}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new BusinessRuleException("Impossible de modifier le mot de passe : " + e.getMessage());
        }
    }

    // ─── Helper ───────────────────────────────────────────────────────────────

    private String getCurrentToken() {
        try {
            HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes())
                    .getRequest();
            String header = request.getHeader("Authorization");
            if (header != null && header.startsWith("Bearer ")) {
                return header.substring(7);
            }
        } catch (Exception e) {
            log.error("Impossible de récupérer le token depuis la requête : {}", e.getMessage());
        }
        return "";
    }
}
