package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.FactureRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FactureResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.PaiementResponseDTO;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.service.FactureService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class FactureController {

    private final FactureService factureService;

    @GetMapping("/factures")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<Page<FactureResponseDTO>> list(
            @RequestParam(required = false) StatutFacture statut,
            @RequestParam(required = false) Long familleId,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            Pageable pageable) {
        return ResponseEntity.ok(factureService.findAll(statut, familleId, dateDebut, dateFin, pageable));
    }

    @GetMapping("/factures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> getById(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.findById(id));
    }

    @GetMapping("/depouilles/{id}/facture")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> getByDepouilleId(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.getByDepouilleId(id));
    }

    @PostMapping("/factures/calculer")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> calculer(@RequestBody Map<String, Long> payload) {
        Long depouillId = payload.get("depouillId");
        return ResponseEntity.ok(factureService.calculer(depouillId));
    }

    @PostMapping("/factures")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> create(@Valid @RequestBody FactureRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(factureService.creer(dto));
    }

    @PutMapping("/factures/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> modifier(@PathVariable Long id,
            @Valid @RequestBody FactureRequestDTO dto) {
        return ResponseEntity.ok(factureService.modifier(id, dto));
    }

    @PatchMapping("/factures/{id}/emettre")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Void> emettre(@PathVariable Long id) {
        factureService.emettre(id);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/factures/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<PaiementResponseDTO> enregistrerPaiement(
            @PathVariable Long id,
            @RequestBody Map<String, Object> payload) {
        Double montant = Double.valueOf(payload.get("montant").toString());
        String mode = (String) payload.get("mode");
        String reference = (String) payload.get("reference");
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(factureService.enregistrerPaiement(id, montant, mode, reference));
    }

    @GetMapping("/factures/{id}/paiements")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<List<PaiementResponseDTO>> getPaiements(@PathVariable Long id) {
        return ResponseEntity.ok(factureService.getPaiements(id));
    }

    @PatchMapping("/factures/{id}/annuler")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<FactureResponseDTO> annuler(
            @PathVariable Long id,
            @RequestBody Map<String, String> payload) {
        return ResponseEntity.ok(factureService.annuler(id, payload.get("motif")));
    }

    @GetMapping("/factures/{id}/pdf")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<byte[]> genererPdf(@PathVariable Long id) {
        byte[] pdf = factureService.genererPdf(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "facture_" + id + ".pdf");
        return new ResponseEntity<>(pdf, headers, HttpStatus.OK);
    }
}
