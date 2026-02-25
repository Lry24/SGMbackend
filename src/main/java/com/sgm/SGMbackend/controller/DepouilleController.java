package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.*;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.mapper.DepouilleMapper;
import com.sgm.SGMbackend.service.DepouilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/depouilles")
@RequiredArgsConstructor
public class DepouilleController {

    private final DepouilleService service;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<DepouilleResponseDTO> create(
            @RequestBody DepouilleRequestDTO dto) {

        var entity = service.enregistrer(
                DepouilleMapper.toEntity(dto));

        return ResponseEntity.status(201)
                .body(DepouilleMapper.toDTO(entity));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DepouilleResponseDTO> get(@PathVariable Long id) {

        return ResponseEntity.ok(
                DepouilleMapper.toDTO(service.findById(id))
        );
    }

    @PatchMapping("/{id}/statut")
    public ResponseEntity<DepouilleResponseDTO> changeStatut(
            @PathVariable Long id,
            @RequestParam StatutDepouille statut) {

        return ResponseEntity.ok(
                DepouilleMapper.toDTO(
                        service.changerStatut(id, statut)
                )
        );
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<?> update(@PathVariable Long id,
                                    @RequestBody DepouilleRequestDTO dto) {

        var entity = DepouilleMapper.toEntity(dto);

        return ResponseEntity.ok(
                DepouilleMapper.toDTO(
                        service.modifier(id, entity)
                )
        );
    }
    @GetMapping("/{id}/historique")
    public ResponseEntity<?> historique(@PathVariable Long id) {
        return ResponseEntity.ok(service.historique(id));
    }
}