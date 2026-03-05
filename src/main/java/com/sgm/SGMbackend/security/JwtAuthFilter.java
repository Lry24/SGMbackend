package com.sgm.SGMbackend.security;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.source.JWKSource;
import com.nimbusds.jose.jwk.source.RemoteJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jose.proc.SecurityContext;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.proc.ConfigurableJWTProcessor;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import com.nimbusds.jose.util.DefaultResourceRetriever;
import com.nimbusds.jose.util.ResourceRetriever;
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

    private ConfigurableJWTProcessor<SecurityContext> jwtProcessor;
    private com.auth0.jwt.algorithms.Algorithm hmacAlgorithm;

    private synchronized void initJwtProcessors() {
        if (jwtProcessor != null)
            return;
        try {
            // Configuration ES256 (JWKS) avec timeout augmenté (10s)
            String jwksUrl = supabaseUrl + "/auth/v1/.well-known/jwks.json";
            ResourceRetriever retriever = new DefaultResourceRetriever(10000, 10000);
            JWKSource<SecurityContext> keySource = new RemoteJWKSet<>(new URL(jwksUrl), retriever);
            jwtProcessor = new DefaultJWTProcessor<>();
            jwtProcessor.setJWSKeySelector(new JWSVerificationKeySelector<>(JWSAlgorithm.ES256, keySource));

            // Configuration HS256 (Shared Secret)
            hmacAlgorithm = com.auth0.jwt.algorithms.Algorithm.HMAC256(jwtSecret);
            log.info("[JwtAuthFilter] Processeurs JWT initialisés pour {}", supabaseUrl);
        } catch (Exception e) {
            log.error("[JwtAuthFilter] Erreur init JWT : {}", e.getMessage());
        }
    }

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

        if (jwtProcessor == null)
            initJwtProcessors();

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
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            String roleFromDb = user.getRole().name();
            var auth = new UsernamePasswordAuthenticationToken(
                    user,
                    token,
                    List.of(new SimpleGrantedAuthority("ROLE_" + roleFromDb)));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            log.warn("[JwtAuthFilter] ❌ Erreur Auth pour {}: {}", request.getRequestURI(), e.getMessage());
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }

    /**
     * Vérifie le token JWT en essayant d'abord JWKS (ES256, Supabase récent),
     * puis HMAC256 (projets anciens). Retourne le subject (user UUID) ou null.
     */
    private String verifyTokenAndGetSubject(String token) {
        if (token == null || token.length() < 10)
            return null;

        try {
            // Décodage sans vérification pour lire l'algorithme
            com.auth0.jwt.interfaces.DecodedJWT decoded = com.auth0.jwt.JWT.decode(token);
            String alg = decoded.getAlgorithm();

            if ("ES256".equals(alg)) {
                log.info("[JwtAuthFilter] Validation ES256 via JWKS...");
                JWTClaimsSet claims = jwtProcessor.process(token, null);
                return claims.getSubject();
            } else {
                log.info("[JwtAuthFilter] Validation {} via Secret Local...", alg);
                com.auth0.jwt.interfaces.DecodedJWT verified = com.auth0.jwt.JWT
                        .require(hmacAlgorithm)
                        .build()
                        .verify(token);
                return verified.getSubject();
            }
        } catch (Exception e) {
            log.error("[JwtAuthFilter] ❌ Validation échouée: {}", e.getMessage());
            return null;
        }
    }
}
