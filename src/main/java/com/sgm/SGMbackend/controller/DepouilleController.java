package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.DepouilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.enums.StatutDepouille;
import com.sgm.SGMbackend.mapper.DepouilleMapper;
import com.sgm.SGMbackend.service.BonReceptionService;
import com.sgm.SGMbackend.service.DepouilleService;
import com.sgm.SGMbackend.service.RestitutionService;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.MouvementDepouilleResponseDTO;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.MouvementDepouilleMapper;
import com.sgm.SGMbackend.repository.MouvementDepouilleRepository;
import com.sgm.SGMbackend.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
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
    private final MouvementDepouilleRepository mouvementRepo;
    private final MouvementDepouilleMapper mouvementMapper;
    private final BonReceptionService bonReceptionService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN','COMPTABLE')")
    public ResponseEntity<Page<DepouilleResponseDTO>> findAll(
            Pageable pageable,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) StatutDepouille statut) {
        Page<DepouilleResponseDTO> result = depouilleService
                .findAll(pageable, search, statut)
                .map(d -> {
                    DepouilleResponseDTO dto = depouilleMapper.toResponseDTO(d);
                    com.sgm.SGMbackend.entity.Facture f = factureService.findByDepouille(d.getId());
                    if (f != null) {
                        dto.setStatutPaiement(f.getStatut().name());
                    } else {
                        dto.setStatutPaiement("AUCUNE");
                    }
                    return dto;
                });
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN')")
    public ResponseEntity<DepouilleResponseDTO> findById(@PathVariable Long id) {
        Depouille d = depouilleService.findById(id);
        DepouilleResponseDTO dto = depouilleMapper.toResponseDTO(d);
        com.sgm.SGMbackend.entity.Facture f = factureService.findByDepouille(d.getId());
        if (f != null) {
            dto.setStatutPaiement(f.getStatut().name());
        } else {
            dto.setStatutPaiement("AUCUNE");
        }
        return ResponseEntity.ok(dto);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','RESPONSABLE')")
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
    public ResponseEntity<java.util.List<MouvementDepouilleResponseDTO>> getHistorique(@PathVariable Long id) {
        return ResponseEntity.ok(mouvementRepo.findByDepouilleIdOrderByDateMouvementDesc(id)
                .stream()
                .map(mouvementMapper::toResponseDTO)
                .toList());
    }

    @GetMapping(value = "/{id}/qrcode", produces = MediaType.IMAGE_PNG_VALUE)
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','RESPONSABLE')")
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

    @GetMapping("/{id}/bon-reception")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT','MEDECIN','COMPTABLE')")
    public ResponseEntity<byte[]> getBonReception(@PathVariable Long id) {
        byte[] pdf = bonReceptionService.genererBonReception(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDisposition(
                ContentDisposition.attachment()
                        .filename("bon-reception-" + id + ".pdf")
                        .build());
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
