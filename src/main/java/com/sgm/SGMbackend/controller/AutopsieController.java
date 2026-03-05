package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.entity.enums.StatutAutopsie;
import com.sgm.SGMbackend.service.AutopsieService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Map;

@RestController
@RequestMapping("/api/autopsies")
@RequiredArgsConstructor
@Tag(name = "Gestion des Autopsies", description = "Endpoints pour la gestion des rapports d'autopsie")
public class AutopsieController {

    private final AutopsieService autoService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<?> list(Pageable pageable,
            @RequestParam(required = false) StatutAutopsie statut) {
        return ResponseEntity.ok(autoService.findAll(pageable, statut));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<?> getById(@PathVariable Long id) {
        return ResponseEntity.ok(autoService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<?> planifier(@RequestBody Map<String, Object> body) {
        Long depouillId = Long.valueOf(body.get("depouillId").toString());
        String medecinId = body.get("medecinId").toString();
        LocalDateTime date = LocalDateTime.parse(body.get("datePlanifiee").toString());
        String salle = body.get("salle") != null ? body.get("salle").toString() : null;
        return ResponseEntity.status(201).body(autoService.planifier(depouillId, medecinId, date, salle));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<?> modifier(@PathVariable Long id, @RequestBody Map<String, Object> body) {
        String medecinId = body.get("medecinId").toString();
        LocalDateTime date = LocalDateTime.parse(body.get("datePlanifiee").toString());
        String salle = body.get("salle") != null ? body.get("salle").toString() : null;
        return ResponseEntity.ok(autoService.modifier(id, medecinId, date, salle));
    }

    @PatchMapping("/{id}/demarrer")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<?> demarrer(@PathVariable Long id) {
        return ResponseEntity.ok(autoService.demarrer(id));
    }

    @PatchMapping("/{id}/terminer")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<?> terminer(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(autoService.terminer(id, body.get("rapport"), body.get("conclusion")));
    }

    @PostMapping("/{id}/analyses")
    @PreAuthorize("hasAnyRole('ADMIN','MEDECIN')")
    public ResponseEntity<?> ajouterAnalyse(@PathVariable Long id, @RequestBody Map<String, String> body) {
        return ResponseEntity.ok(autoService.ajouterAnalyse(id, body.get("description")));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<?> annuler(@PathVariable Long id) {
        autoService.annuler(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/medecin/{medecinId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','MEDECIN')")
    public ResponseEntity<?> parMedecin(@PathVariable String medecinId) {
        return ResponseEntity.ok(autoService.findByMedecin(medecinId));
    }

    @GetMapping("/planning")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','MEDECIN')")
    public ResponseEntity<?> planning(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(autoService.getPlanning(date != null ? date : LocalDateTime.now()));
    }
}
