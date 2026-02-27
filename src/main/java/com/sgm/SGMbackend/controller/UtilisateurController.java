package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.UtilisateurRequestDTO;
import com.sgm.SGMbackend.entity.enums.Role;
import com.sgm.SGMbackend.service.UtilisateurService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/api/utilisateurs")
@RequiredArgsConstructor
@Tag(name = "Gestion des Utilisateurs", description = "Endpoints pour l'administration des comptes et des rôles")
public class UtilisateurController {

    private final UtilisateurService utilisateurService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> list(Pageable pageable,
            @RequestParam(required = false) Role role) {
        return ResponseEntity.ok(utilisateurService.findAll(pageable, role));
    }

    @GetMapping("/medecins")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<?> getMedecins() {
        return ResponseEntity.ok(utilisateurService.findAll(
                org.springframework.data.domain.PageRequest.of(0, 500),
                Role.MEDECIN).getContent());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> getById(@PathVariable String id) {
        return ResponseEntity.ok(utilisateurService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> create(@Valid @RequestBody UtilisateurRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(utilisateurService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> update(@PathVariable String id,
            @Valid @RequestBody UtilisateurRequestDTO dto) {
        return ResponseEntity.ok(utilisateurService.update(id, dto));
    }

    @PatchMapping("/{id}/activer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> activer(@PathVariable String id,
            @RequestBody Map<String, Boolean> body) {
        utilisateurService.setActif(id, body.get("actif"));
        return ResponseEntity.ok().build();
    }

    @PostMapping("/{id}/reset-password")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> resetPassword(@PathVariable String id) {
        utilisateurService.resetPassword(id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<?> delete(@PathVariable String id) {
        utilisateurService.delete(id);
        return ResponseEntity.noContent().build();
    }
}
