package com.sgm.SGMbackend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sgm.SGMbackend.security.JwtAuthFilter;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Configuration principale Spring Security.
 * - Désactive CSRF (API REST stateless)
 * - Pas de session HTTP (JWT)
 * - Autorise /api/auth/login, /swagger-ui, /api-docs et /api/health
 * publiquement
 * - Toute autre requête requiert un JWT valide
 * - Active @PreAuthorize sur les controllers via @EnableMethodSecurity
 * - Retourne 401 (et non 403) pour les requêtes sans token
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(ex -> ex
                        // 401 quand pas de token (au lieu du 403 par défaut de Spring Security)
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.setContentType("application/json;charset=UTF-8");
                            Map<String, Object> body = Map.of(
                                    "timestamp", LocalDateTime.now().toString(),
                                    "status", 401,
                                    "message", "Token manquant ou invalide",
                                    "path", request.getRequestURI());
                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        })
                        // 403 quand token valide mais rôle insuffisant
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.setContentType("application/json;charset=UTF-8");
                            Map<String, Object> body = Map.of(
                                    "timestamp", LocalDateTime.now().toString(),
                                    "status", 403,
                                    "message", "Accès refusé — rôle insuffisant",
                                    "path", request.getRequestURI());
                            response.getWriter().write(objectMapper.writeValueAsString(body));
                        }))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/login").permitAll()
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**").permitAll()
                        .requestMatchers("/api/health").permitAll()
                        .anyRequest().authenticated())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

}
