package com.sgm.SGMbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

/**
 * Configuration CORS pour autoriser les origines frontend définies dans
 * application.properties.
 * Appliqué sur tous les endpoints /api/**.
 */
@Configuration
public class CorsConfig {

    @Value("${cors.allowed.origins}")
    private String allowedOrigins;

    @Bean
    public CorsFilter corsFilter() {
        CorsConfiguration config = new CorsConfiguration();

        // Origines autorisées depuis la config (ex:
        // http://localhost:3000,http://localhost:5173)
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));

        // Méthodes HTTP autorisées
        config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // Tous les headers autorisés (Authorization, Content-Type, etc.)
        config.setAllowedHeaders(List.of("*"));

        // Autoriser l'envoi de cookies/credentials
        config.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", config);

        return new CorsFilter(source);
    }

    /**
     * Bean RestTemplate pour les appels à l'API Supabase Auth.
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
