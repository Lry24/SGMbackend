package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.RestitutionRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.RestitutionResponseDTO;
import com.sgm.SGMbackend.entity.enums.StatutRestitution;
import com.sgm.SGMbackend.service.RestitutionService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/restitutions")
@RequiredArgsConstructor
@Tag(name = "Gestion des Restitutions", description = "Endpoints pour le workflow de sortie et remise des corps")
public class RestitutionController {

    private final RestitutionService restitutionService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<RestitutionResponseDTO>> list(
            @RequestParam(required = false) StatutRestitution statut,
            Pageable pageable) {
        return ResponseEntity.ok(restitutionService.findAll(statut, pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<RestitutionResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(restitutionService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<RestitutionResponseDTO> planifier(@Valid @RequestBody RestitutionRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(restitutionService.planifier(dto));
    }

    @PatchMapping("/{id}/confirmer")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<RestitutionResponseDTO> confirmer(@PathVariable Long id) {
        return ResponseEntity.ok(restitutionService.confirmer(id));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE')")
    public ResponseEntity<Void> annuler(@PathVariable Long id, @RequestBody Map<String, String> body) {
        restitutionService.annuler(id, body != null ? body.get("motif") : null);
        return ResponseEntity.ok().build();
    }

    @PatchMapping("/{id}/effectuer")
    @PreAuthorize("hasAnyRole('ADMIN', 'RESPONSABLE', 'AGENT')")
    public ResponseEntity<RestitutionResponseDTO> effectuer(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        String pieceRef = body != null ? body.get("pieceIdentiteRef") : null;
        return ResponseEntity.ok(restitutionService.effectuer(id, pieceRef));
    }

    @GetMapping("/planning")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<RestitutionResponseDTO>> planning(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        return ResponseEntity.ok(restitutionService.getPlanning(date != null ? date : LocalDateTime.now()));
    }
}
