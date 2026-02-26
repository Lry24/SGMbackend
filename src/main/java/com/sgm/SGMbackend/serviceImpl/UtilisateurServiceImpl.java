package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.config.SupabaseConfig;
import com.sgm.SGMbackend.dto.dtoRequest.UtilisateurRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.entity.enums.Role;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.UtilisateurMapper;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import com.sgm.SGMbackend.service.UtilisateurService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UtilisateurServiceImpl implements UtilisateurService {

    private final UtilisateurRepository utilisateurRepository;
    private final UtilisateurMapper utilisateurMapper;
    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate;

    // ─── List ─────────────────────────────────────────────────────────────────

    @Override
    public Page<UtilisateurResponseDTO> findAll(Pageable pageable, Role role) {
        Page<Utilisateur> page = (role != null)
                ? utilisateurRepository.findByRole(role, pageable)
                : utilisateurRepository.findAll(pageable);
        return page.map(utilisateurMapper::toResponseDTO);
    }

    // ─── FindById ─────────────────────────────────────────────────────────────

    @Override
    public UtilisateurResponseDTO findById(String id) {
        return utilisateurMapper.toResponseDTO(
                utilisateurRepository.findById(id)
                        .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id)));
    }

    // ─── Create ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    @SuppressWarnings("unchecked")
    public UtilisateurResponseDTO create(UtilisateurRequestDTO dto) {
        // Vérifier l'unicité de l'email
        if (utilisateurRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new BusinessRuleException("Un utilisateur avec cet email existe déjà.");
        }

        // 1. Créer le compte dans Supabase Auth via Admin API
        String supabaseUserId = createSupabaseUser(dto.getEmail(), dto.getRole().name(), dto.getPassword());

        // 2. Persister le profil en base locale
        Utilisateur utilisateur = Utilisateur.builder()
                .id(supabaseUserId)
                .nom(dto.getNom())
                .prenom(dto.getPrenom())
                .email(dto.getEmail())
                .role(dto.getRole())
                .actif(true)
                .doitChangerMotDePasse(true)
                .build();

        return utilisateurMapper.toResponseDTO(utilisateurRepository.save(utilisateur));
    }

    // ─── Update ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public UtilisateurResponseDTO update(String id, UtilisateurRequestDTO dto) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));

        u.setNom(dto.getNom());
        u.setPrenom(dto.getPrenom());
        u.setEmail(dto.getEmail());
        u.setRole(dto.getRole());

        return utilisateurMapper.toResponseDTO(utilisateurRepository.save(u));
    }

    // ─── SetActif ─────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void setActif(String id, Boolean actif) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));
        u.setActif(actif);
        utilisateurRepository.save(u);

        // Bloquer/débloquer également dans Supabase
        updateSupabaseUserBan(id, !actif);
    }

    // ─── Reset Password ───────────────────────────────────────────────────────

    @Override
    public void resetPassword(String id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));

        // On utilise l'endpoint public /recover qui déclenche l'envoi d'email par
        // Supabase. On ajoute redirectTo pour forcer le retour sur notre page de reset.
        String url = supabaseConfig.getAuthUrl() + "/recover?redirectTo=http://localhost:4200/reset-password";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getAnonKey());

        Map<String, String> body = Map.of("email", u.getEmail());

        try {
            restTemplate.exchange(url, HttpMethod.POST, new HttpEntity<>(body, headers), Void.class);
            log.info("Email de récupération envoyé à {}", u.getEmail());
        } catch (HttpClientErrorException e) {
            String errorMsg = extractSupabaseMessage(e);
            log.error("Erreur Supabase lors du recover : {}", errorMsg);
            throw new BusinessRuleException("Impossible d'envoyer l'email de récupération : " + errorMsg);
        }
    }

    // ─── Delete ───────────────────────────────────────────────────────────────

    @Override
    @Transactional
    public void delete(String id) {
        Utilisateur u = utilisateurRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Utilisateur introuvable : " + id));

        // 1. Supprimer de Supabase Auth
        deleteSupabaseUser(id);

        // 2. Supprimer de la base locale
        utilisateurRepository.delete(u);
    }

    // ─── Helpers Supabase ─────────────────────────────────────────────────────

    @SuppressWarnings("unchecked")
    private String createSupabaseUser(String email, String role, String password) {
        String url = supabaseConfig.getAuthUrl() + "/admin/users";
        HttpHeaders headers = buildAdminHeaders();

        // Si aucun mot de passe n'est fourni, on en génère un aléatoire (fallback)
        String finalPassword = (password != null && !password.isBlank())
                ? password
                : UUID.randomUUID().toString();

        Map<String, Object> body = Map.of(
                "email", email,
                "password", finalPassword,
                "email_confirm", true,
                "user_metadata", Map.of("role", role));

        try {
            ResponseEntity<Map> response = restTemplate.exchange(
                    url, HttpMethod.POST, new HttpEntity<>(body, headers), Map.class);
            Map<String, Object> responseBody = response.getBody();
            return (String) responseBody.get("id");
        } catch (HttpClientErrorException e) {
            String errorMsg = extractSupabaseMessage(e);
            throw new BusinessRuleException("Impossible de créer le compte Supabase : " + errorMsg);
        }
    }

    private void deleteSupabaseUser(String userId) {
        String url = supabaseConfig.getAuthUrl() + "/admin/users/" + userId;
        try {
            restTemplate.exchange(url, HttpMethod.DELETE, new HttpEntity<>(buildAdminHeaders()), Void.class);
        } catch (HttpClientErrorException e) {
            log.warn("Impossible de supprimer l'utilisateur Supabase {} : {}", userId, e.getMessage());
        }
    }

    private void updateSupabaseUserBan(String userId, boolean ban) {
        String url = supabaseConfig.getAuthUrl() + "/admin/users/" + userId;
        HttpHeaders headers = buildAdminHeaders();
        Map<String, Object> body = Map.of("ban_duration", ban ? "876600h" : "none");
        try {
            restTemplate.exchange(url, HttpMethod.PUT, new HttpEntity<>(body, headers), Void.class);
        } catch (HttpClientErrorException e) {
            log.warn("Impossible de modifier le statut ban de l'utilisateur {} : {}", userId, e.getMessage());
        }
    }

    private HttpHeaders buildAdminHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("apikey", supabaseConfig.getServiceKey());
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());
        return headers;
    }

    private String extractSupabaseMessage(HttpClientErrorException e) {
        StringBuilder sb = new StringBuilder();
        sb.append(e.getStatusCode()).append(" ");
        try {
            String body = e.getResponseBodyAsString();
            if (body != null && !body.isEmpty()) {
                sb.append("- Body: ").append(body);
            } else {
                sb.append("- (No body)");
            }
        } catch (Exception ex) {
            sb.append("- (Error reading body: ").append(ex.getMessage()).append(")");
        }
        return sb.toString();
    }
}
