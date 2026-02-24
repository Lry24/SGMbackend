package com.sgm.SGMbackend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration Supabase — centralise les propriétés d'accès à Supabase.
 * Les valeurs sont injectées depuis application.properties.
 *
 * Usage : injecter SupabaseConfig dans les services qui appellent l'API
 * Supabase
 * (ex: AuthService pour login/logout via l'API REST Supabase Auth).
 */
@Configuration
public class SupabaseConfig {

    @Value("${supabase.url}")
    private String supabaseUrl;

    @Value("${supabase.anon.key}")
    private String anonKey;

    @Value("${supabase.service.key}")
    private String serviceKey;

    @Value("${supabase.jwt.secret}")
    private String jwtSecret;

    public String getSupabaseUrl() {
        return supabaseUrl;
    }

    public String getAnonKey() {
        return anonKey;
    }

    public String getServiceKey() {
        return serviceKey;
    }

    public String getJwtSecret() {
        return jwtSecret;
    }

    /**
     * URL de l'API Auth Supabase (utilisée pour login, logout, refresh, reset)
     */
    public String getAuthUrl() {
        return supabaseUrl + "/auth/v1";
    }
}
