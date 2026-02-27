package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.DepouilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.mapper.DepouilleMapper;
import com.sgm.SGMbackend.service.DepouilleService;
import com.sgm.SGMbackend.service.RestitutionService;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Map;

@RestController
@RequestMapping("/api/depouilles")
@RequiredArgsConstructor
@Tag(name = "Gestion des Dépouilles", description = "Endpoints pour l'enregistrement et le suivi des dépouilles")
public class DepouilleController {

    private final DepouilleService depouilleService;
    private final DepouilleMapper depouilleMapper;
    private final RestitutionService restitutionService;
    private final FactureService factureService;
    private final FactureMapper factureMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<Page<DepouilleResponseDTO>> findAll(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatutDepouille statut) {
        Page<DepouilleResponseDTO> result = depouilleService
                .findAll(pageable, search, statut)
                .map(depouilleMapper::toResponseDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<DepouilleResponseDTO> findById(@PathVariable Long id) {
        return ResponseEntity.ok(depouilleMapper.toResponseDTO(depouilleService.findById(id)));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<DepouilleResponseDTO> enregistrer(@RequestBody @Valid DepouilleRequestDTO dto) {
        Depouille depouille = depouilleMapper.toEntity(dto);
        Depouille saved = depouilleService.enregistrer(depouille);
        return ResponseEntity.status(HttpStatus.CREATED).body(depouilleMapper.toResponseDTO(saved));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<DepouilleResponseDTO> update(@PathVariable Long id,
            @RequestBody @Valid DepouilleRequestDTO dto) {
        Depouille existing = depouilleService.findById(id);
        depouilleMapper.updateEntity(dto, existing);
        // On pourrait ajouter une méthode update dans le service, mais save suffit ici
        return ResponseEntity.ok(depouilleMapper.toResponseDTO(depouilleService.enregistrer(existing)));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<DepouilleResponseDTO> changerStatut(@PathVariable Long id,
            @RequestBody Map<String, StatutDepouille> body) {
        StatutDepouille statut = body.get("statut");
        return ResponseEntity.ok(depouilleMapper.toResponseDTO(depouilleService.changerStatut(id, statut)));
    }

    @GetMapping("/{id}/historique")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<DepouilleResponseDTO> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(depouilleMapper.toResponseDTO(depouilleService.findById(id)));
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<byte[]> getQRCode(@PathVariable Long id) {
        return ResponseEntity.ok(depouilleService.getQRCode(id));
    }

    @GetMapping("/{id}/facture")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> getFacture(@PathVariable Long id) {
        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.findByDepouille(id)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        depouilleService.supprimer(id);
        return ResponseEntity.noContent().build();
    }
}
