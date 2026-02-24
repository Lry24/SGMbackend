package com.sgm.SGMbackend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.sgm.SGMbackend.entity.Utilisateur;
import com.sgm.SGMbackend.repository.UtilisateurRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Filtre JWT exécuté une fois par requête.
 * 1. Extrait et valide le token Bearer Supabase (HMAC256)
 * 2. Vérifie que l'utilisateur existe ET est actif en base de données
 * 3. Charge le rôle depuis la DB (plus fiable que le token)
 * 4. Met l'objet Utilisateur complet dans le SecurityContext
 *
 * Avantages :
 * - Un compte désactivé (actif=false) est rejeté même avec un token valide
 * - Un changement de rôle en DB est effectif immédiatement
 * - L'objet Utilisateur est accessible via SecurityContextHolder dans tous les
 * controllers
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

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

        // Si pas de header Bearer, on passe (Spring Security gérera le 401)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            // Validation du JWT avec la clé secrète Supabase (Legacy HS256)
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(token);

            String userId = jwt.getSubject();

            // ── Vérification en base de données ──────────────────────────────
            Utilisateur user = utilisateurRepository.findById(userId).orElse(null);

            if (user == null || !Boolean.TRUE.equals(user.getActif())) {
                // Compte inexistant ou désactivé → rejet même avec token valide
                SecurityContextHolder.clearContext();
                chain.doFilter(request, response);
                return;
            }

            // Rôle depuis la DB (pas depuis le token) → effectif immédiatement après
            // changement
            String roleFromDb = user.getRole().name();

            // L'objet Utilisateur complet est le principal → accessible dans tous les
            // controllers
            var auth = new UsernamePasswordAuthenticationToken(
                    user,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + roleFromDb)));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Token invalide ou expiré → Spring Security retournera 401
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
