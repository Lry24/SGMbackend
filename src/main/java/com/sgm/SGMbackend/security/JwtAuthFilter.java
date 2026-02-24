package com.sgm.SGMbackend.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.interfaces.DecodedJWT;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
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
 * Extrait le token Bearer, le valide via la clé secrète Supabase,
 * puis charge l'authentification dans le SecurityContext de Spring.
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response,
            FilterChain chain)
            throws IOException, ServletException {

        String header = request.getHeader("Authorization");

        // Si pas de header Bearer, on passe au filtre suivant (Spring Security gérera
        // l'accès)
        if (header == null || !header.startsWith("Bearer ")) {
            chain.doFilter(request, response);
            return;
        }

        try {
            String token = header.substring(7);

            // Validation du JWT avec la clé secrète Supabase (HMAC256)
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .build()
                    .verify(token);

            // Supabase met l'identifiant utilisateur dans 'sub'
            String userId = jwt.getSubject();

            // Supabase stocke le rôle dans un claim personnalisé 'role'
            String role = jwt.getClaim("role").asString();
            if (role == null)
                role = "AGENT"; // rôle par défaut si non précisé

            var auth = new UsernamePasswordAuthenticationToken(
                    userId,
                    null,
                    List.of(new SimpleGrantedAuthority("ROLE_" + role.toUpperCase())));

            SecurityContextHolder.getContext().setAuthentication(auth);

        } catch (Exception e) {
            // Token invalide ou expiré : on vide le contexte, Spring Security retournera
            // 401
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(request, response);
    }
}
