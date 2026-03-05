package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.FactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.LigneFacture;
import com.sgm.SGMbackend.dto.dtoResponse.MouvementCaisseResponseDTO;
import com.sgm.SGMbackend.mapper.FactureMapper;
import com.sgm.SGMbackend.mapper.LigneFactureMapper;
import com.sgm.SGMbackend.mapper.MouvementCaisseMapper;
import com.sgm.SGMbackend.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/factures")
@RequiredArgsConstructor
@Tag(name = "Gestion de la Facturation", description = "Endpoints pour la création et le suivi des factures et paiements")
public class FactureController {

    private final FactureService factureService;
    private final FactureMapper factureMapper;
    private final LigneFactureMapper ligneMapper;
    private final MouvementCaisseMapper mouvementMapper;

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> creer(@Valid @RequestBody FactureRequestDTO requestDTO) {
        List<LigneFacture> lignes = requestDTO.getLignes().stream()
                .map(ligneMapper::toEntity)
                .collect(Collectors.toList());

        Facture f = factureService.creer(
                requestDTO.getDepouilleId(),
                requestDTO.getFamilleId(),
                lignes,
                requestDTO.getRemise());
        return ResponseEntity.status(201).body(factureMapper.toResponseDTO(f));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.findById(id)));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<org.springframework.data.domain.Page<FactureResponseDTO>> getAll(
            org.springframework.data.domain.Pageable pageable,
            @RequestParam(required = false) com.sgm.SGMbackend.entity.enums.StatutFacture statut) {
        return ResponseEntity.ok(factureService.findAll(pageable, statut)
                .map(factureMapper::toResponseDTO));
    }

    @PostMapping("/calculer")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Double> calculer(@RequestBody Map<String, Long> payload) {
        return ResponseEntity.ok(factureService.calculerEstimation(payload.get("depouilleId")));
    }

    @PostMapping("/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> enregistrerPaiement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {

        Object montantObj = payload.get("montant");
        Object modeObj = payload.get("mode");

        if (montantObj == null) {
            throw new com.sgm.SGMbackend.exception.BusinessRuleException("Le montant est obligatoire.");
        }

        Double montant = Double.valueOf(montantObj.toString());
        String mode = modeObj != null ? modeObj.toString() : "ESPECES";
        String reference = payload.getOrDefault("reference", "").toString();

        Facture f = factureService.enregistrerPaiement(id, montant, mode, reference);
        return ResponseEntity.status(201).body(factureMapper.toResponseDTO(f));
    }

    @PatchMapping("/{id}/emettre")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> emettre(@PathVariable Long id) {
        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.emettre(id)));
    }

    @PatchMapping("/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> annuler(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {

        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.annuler(id, payload.get("motif"))));
    }

    @PatchMapping("/{id}/statut")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> updateStatut(
            @PathVariable Long id,
            @RequestBody Map<String, com.sgm.SGMbackend.entity.enums.StatutFacture> body) {
        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.updateStatut(id, body.get("statut"))));
    }

    @GetMapping("/depouilles/{id}/facture")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE','AGENT')")
    public ResponseEntity<FactureResponseDTO> getByDepouille(@PathVariable Long id) {
        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.findByDepouille(id)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> modifier(
            @PathVariable Long id,
            @Valid @RequestBody FactureRequestDTO requestDTO) {
        List<LigneFacture> lignes = requestDTO.getLignes().stream()
                .map(ligneMapper::toEntity)
                .collect(Collectors.toList());
        Facture f = factureService.modifier(id, lignes, requestDTO.getRemise());
        return ResponseEntity.ok(factureMapper.toResponseDTO(f));
    }

    @GetMapping("/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<List<MouvementCaisseResponseDTO>> getPaiements(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.findPaiementsByFacture(id).stream()
                .map(mouvementMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }

    @GetMapping("/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<byte[]> getPdf(@PathVariable Long id) {
        byte[] pdf = factureService.generatePdf(id);
        Facture f = factureService.findById(id);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"facture_" + f.getNumero() + ".pdf\"")
                .contentType(MediaType.APPLICATION_PDF)
                .body(pdf);
    }

    @GetMapping("/numero/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE','AGENT')")
    public ResponseEntity<FactureResponseDTO> getByNumero(@PathVariable String numero) {
        return ResponseEntity.ok(factureMapper.toResponseDTO(factureService.findByNumero(numero)));
    }
}
