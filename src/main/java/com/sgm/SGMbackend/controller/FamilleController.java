package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.FamilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FamilleResponseDTO;
import com.sgm.SGMbackend.service.FamilleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;

@RestController
@RequestMapping("/api/familles")
@RequiredArgsConstructor
@Tag(name = "Gestion des Familles", description = "Endpoints pour la gestion des contacts familiaux")
public class FamilleController {

    private final FamilleService familleService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','COMPTABLE')")
    public ResponseEntity<Page<FamilleResponseDTO>> list(Pageable pageable) {
        return ResponseEntity.ok(familleService.findAll(pageable));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','COMPTABLE')")
    public ResponseEntity<FamilleResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(familleService.findById(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','RESPONSABLE')")
    public ResponseEntity<FamilleResponseDTO> create(@Valid @RequestBody FamilleRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(familleService.create(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<FamilleResponseDTO> update(@PathVariable Long id, @Valid @RequestBody FamilleRequestDTO dto) {
        return ResponseEntity.ok(familleService.update(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        familleService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/depouilles")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','COMPTABLE')")
    public ResponseEntity<List<?>> getDepouilles(@PathVariable Long id) {
        return ResponseEntity.ok(familleService.getDepouillesByFamille(id));
    }

    @PostMapping("/{id}/depouilles/{depId}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<Void> lierDepouille(@PathVariable Long id, @PathVariable Long depId) {
        familleService.lierDepouille(id, depId);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/recherche")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','COMPTABLE')")
    public ResponseEntity<Page<FamilleResponseDTO>> recherche(@RequestParam String q, Pageable pageable) {
        return ResponseEntity.ok(familleService.recherche(q, pageable));
    }
}
