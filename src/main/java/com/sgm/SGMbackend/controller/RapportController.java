package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.service.RapportService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/rapports")
@RequiredArgsConstructor
@Tag(name = "Gestion des Rapports & Statistiques", description = "Endpoints pour la génération des indicateurs et rapports d'activité")
public class RapportController {

    private final RapportService rapportService;

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<Map<String, Object>> dashboard() {
        return ResponseEntity.ok(rapportService.getDashboardKpis());
    }

    @GetMapping("/activite")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<Map<String, Object>> activite(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin,
            @RequestParam(defaultValue = "JSON") String format) {
        return ResponseEntity.ok(rapportService.getActivite(dateDebut, dateFin, format));
    }

    @GetMapping("/occupation")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<Map<String, Object>> occupation(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFin) {
        return ResponseEntity.ok(rapportService.getOccupation(dateDebut, dateFin));
    }

    @GetMapping("/financier")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<Map<String, Object>> financier(
            @RequestParam(defaultValue = "MENSUEL") String periode,
            @RequestParam(required = false) Integer annee,
            @RequestParam(required = false) Integer mois) {
        return ResponseEntity.ok(rapportService.getFinancier(periode, annee, mois));
    }
}
