package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.entity.Caisse;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import com.sgm.SGMbackend.dto.dtoResponse.CaisseResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.MouvementCaisseResponseDTO;
import com.sgm.SGMbackend.mapper.CaisseMapper;
import com.sgm.SGMbackend.mapper.MouvementCaisseMapper;
import com.sgm.SGMbackend.service.ComptabiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/comptabilite")
@RequiredArgsConstructor
@Tag(name = "Gestion de la Comptabilité", description = "Endpoints pour le suivi financier et journal de caisse")
public class ComptabiliteController {

    private final ComptabiliteService comptabiliteService;
    private final CaisseMapper caisseMapper;
    private final MouvementCaisseMapper mouvementMapper;

    @GetMapping("/journal")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Page<MouvementCaisseResponseDTO>> getJournal(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        Page<MouvementCaisseResponseDTO> result = comptabiliteService
                .getJournal(dateDebut, dateFin, PageRequest.of(page, size))
                .map(mouvementMapper::toResponseDTO);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/caisse")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Map<String, Object>> getCaisse(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime date) {
        Map<String, Object> report = comptabiliteService.getCaisseJournaliere(date);

        // Convert entities into DTOs in the map
        List<?> mouvements = (List<?>) report.get("mouvements");
        List<MouvementCaisseResponseDTO> movementDTOs = mouvements.stream()
                .filter(m -> m instanceof com.sgm.SGMbackend.entity.MouvementCaisse)
                .map(m -> mouvementMapper.toResponseDTO((com.sgm.SGMbackend.entity.MouvementCaisse) m))
                .collect(Collectors.toList());

        report.put("mouvements", movementDTOs);
        return ResponseEntity.ok(report);
    }

    @PostMapping("/caisse/ouvrir")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<CaisseResponseDTO> ouvrirCaisse(@RequestBody Map<String, Double> payload) {
        Caisse caisse = comptabiliteService.ouvrirCaisse(payload.get("fondCaisse"));
        return ResponseEntity.status(201).body(caisseMapper.toResponseDTO(caisse));
    }

    @PatchMapping("/caisse/fermer")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<CaisseResponseDTO> fermerCaisse(@RequestBody Map<String, Double> payload) {
        Caisse caisse = comptabiliteService.fermerCaisse(payload.get("soldeFinal"));
        return ResponseEntity.ok(caisseMapper.toResponseDTO(caisse));
    }

    @GetMapping("/grand-livre")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Map<String, Double>> getGrandLivre(@RequestParam String periode) {
        return ResponseEntity.ok(comptabiliteService.getGrandLivre(periode));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Map<String, Double>> getBalance(@RequestParam String periode) {
        return ResponseEntity.ok(comptabiliteService.getBalance(periode));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<byte[]> export(
            @RequestParam String format,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateDebut,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime dateFin) {

        byte[] data = comptabiliteService.exportJournal(dateDebut, dateFin, format);

        String filename = "journal_" + dateDebut.toLocalDate() + "_au_" + dateFin.toLocalDate() + ".csv";

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .contentType(MediaType.parseMediaType("text/csv"))
                .body(data);
    }
}
