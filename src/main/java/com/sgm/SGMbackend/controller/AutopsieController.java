package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import com.sgm.SGMbackend.service.AutopsieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Controller REST pour le module Autopsies.
 * Base URL : /api/autopsies
 *
 * Matrice des droits :
 * ADMIN → R / W / D (tout)
 * RESPONSABLE → R / W (pas de suppression)
 * AGENT → R seulement
 * MEDECIN → R / W (demarrer, terminer, analyses)
 */
@RestController
@RequestMapping("/api/autopsies")
@RequiredArgsConstructor
public class AutopsieController {

    private final AutopsieService autoService;

    // ─────────────────────────────────────────────────────────────
    // GET /api/autopsies — Liste paginée avec filtre optionnel ?statut
    // ─────────────────────────────────────────────────────────────
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<?> list(Pageable pageable,
            @RequestParam(required = false) StatutAutopsie statut) {
        return ResponseEntity.ok(autoService.findAll(pageable, statut));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/autopsies/{id} — Détail complet avec rapport
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(autoService.findById(id));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/autopsies — Planifier une autopsie
    // Body : { "depouillId": 1, "medecinId": "uuid", "datePlanifiee":
    // "2026-03-01T09:00:00" }
    // ─────────────────────────────────────────────────────────────
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<?> planifier(@RequestBody Map<String, Object> body) {
        Long depouillId = Long.valueOf(body.get("depouillId").toString());
        String medecinId = body.get("medecinId").toString();
        LocalDateTime date = LocalDateTime.parse(body.get("datePlanifiee").toString());
        return ResponseEntity.status(201).body(autoService.planifier(depouillId, medecinId, date));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/autopsies/{id}/demarrer — Passe à EN_COURS
    // ─────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<?> demarrer(@PathVariable Long id) {
        return ResponseEntity.ok(autoService.demarrer(id));
    }

    // ─────────────────────────────────────────────────────────────
    // PATCH /api/autopsies/{id}/terminer — Enregistre rapport + TERMINEE
    // Body : { "rapport": "...", "conclusion": "..." }
    // ─────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<?> terminer(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(
                autoService.terminer(id, body.get("rapport"), body.get("conclusion")));
    }

    // ─────────────────────────────────────────────────────────────
    // POST /api/autopsies/{id}/analyses — Ajouter une analyse complémentaire
    // Body : { "description": "Toxicologie positive : ..." }
    // ─────────────────────────────────────────────────────────────
    @PostMapping("/{id}/analyses")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<?> ajouterAnalyse(@PathVariable Long id,
            @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(autoService.ajouterAnalyse(id, body.get("description")));
    }

    // ─────────────────────────────────────────────────────────────
    // DELETE /api/autopsies/{id} — Annuler (seulement si PLANIFIEE)
    // ─────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        autoService.annuler(id);
        return ResponseEntity.noContent().build();
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/autopsies/medecin/{medecinId} — Autopsies d'un médecin
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','MEDECIN')")
    public ResponseEntity<?> parMedecin(@PathVariable String medecinId) {
        return ResponseEntity.ok(autoService.findByMedecin(medecinId));
    }

    // ─────────────────────────────────────────────────────────────
    // GET /api/autopsies/planning — Planning du jour ou d'une date donnée
    // ?date=2026-03-01T00:00:00
    // ─────────────────────────────────────────────────────────────
    @GetMapping("/planning")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','MEDECIN')")
    public ResponseEntity<?> planning(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(autoService.getPlanning(date != null ? date : LocalDateTime.now()));
    }
}
