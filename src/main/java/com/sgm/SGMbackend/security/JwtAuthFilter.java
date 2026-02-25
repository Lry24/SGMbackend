package com.sgm.SGMbackend.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.net.URL;
import java.util.List;

/**
 * Filtre JWT exécuté une fois par requête.
 * - Valide le token ES256/HS256 Supabase via le JWKS endpoint.
 * - Charge le rôle depuis la DB locale (pas depuis le token).
 * - Rejette les comptes désactivés même avec un token valide.
 */
@Slf4j
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Autowired
    private UtilisateurRepository utilisateurRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);
            String userId = verifyTokenAndGetSubject(token);

            if (userId == null) {
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            // ── Vérification en base de données ──────────────────────────
            Utilisateur user = utilisateurRepository.findById(userId).orElse(null);

            if (user == null || !Boolean.TRUE.equals(user.getActif())) {
                // Compte inexistant ou désactivé → rejet même avec token valide
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            // Rôle lu depuis la DB → effectif immédiatement après changement
            String roleFromDb = user.getRole().name();

            var auth = new UsernamePasswordAuthenticationToken(
                    user,
                    token, // On stocke le token brut ici pour pouvoir le réutiliser dans les services
                    List.of(new SimpleGrantedAuthority("ROLE_" + roleFromDb)));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.debug("JWT invalide ou expiré : {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    /**
     * Vérifie le token JWT en essayant d'abord JWKS (ES256, Supabase récent),
     * puis HMAC256 (projets anciens). Retourne le subject (user UUID) ou null.
     */
    private String verifyTokenAndGetSubject(String token) {
        // Tentative 1 : vérification via JWKS endpoint (ES256 — projets récents)
        try {
            String jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUrl));

            ConfigurableJWTProcessor<SecurityContext> processor = new DefaultJWTProcessor<>();
            processor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, keySource));

            JWTClaimsSet claims = processor.process(token, null);
            return claims.getSubject();
        } catch (Exception e) {
            log.debug("Échec JWKS/ES256, tentative HMAC256 : {}", e.getMessage());
        }

        // Tentative 2 : vérification HMAC256 (projets Supabase anciens)
        try {
            com.auth0.jwt.interfaces.DecodedJWT jwt = com.auth0.jwt.JWT
                    .require(com.auth0.jwt.algorithms.Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(token);
            return jwt.getSubject();
        } catch (Exception e) {
            log.debug("Échec HMAC256 : {}", e.getMessage());
        }

        return null;
    }
}
