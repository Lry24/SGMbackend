package com.sgm.SGMbackend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * Endpoint de vérification du statut de l'application.
 * Public (voir SecurityConfig) — utile pour tester que la sécurité est
 * opérationnelle :
 * GET /api/health → 200 OK (sans token requis)
 * GET /api/utilisateurs → 401 si pas de token (sécurité OK)
 */
@RestController
public class HealthController {

    @GetMapping("/api/health")
    public Map<String, String> health() {
        return Map.of(
                "status", "UP",
                "app", "SGM Backend",
                "version", "1.0.0");
    }
}
