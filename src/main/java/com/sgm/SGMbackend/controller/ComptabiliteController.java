package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoResponse.CaisseResponseDTO;
import com.sgm.SGMbackend.service.ComptabiliteService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/comptabilite")
@RequiredArgsConstructor
public class ComptabiliteController {

    private final ComptabiliteService comptabiliteService;

    @GetMapping("/journal")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Page<Map<String, Object>>> getJournal(
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin,
            Pageable pageable) {
        return ResponseEntity.ok(comptabiliteService.getJournal(dateDebut, dateFin, pageable));
    }

    @GetMapping("/grand-livre")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Map<String, Object>> getGrandLivre(@RequestParam(required = false) String periode) {
        return ResponseEntity.ok(comptabiliteService.getGrandLivre(periode));
    }

    @GetMapping("/balance")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<Map<String, Object>> getBalance(@RequestParam(required = false) String periode) {
        return ResponseEntity.ok(comptabiliteService.getBalance(periode));
    }

    @GetMapping("/caisse")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<CaisseResponseDTO> getCaisseJour(@RequestParam(required = false) String date) {
        return ResponseEntity.ok(comptabiliteService.getCaisseJour(date));
    }

    @PostMapping("/caisse/ouvrir")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<CaisseResponseDTO> ouvrirCaisse(@RequestBody Map<String, Double> payload) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(comptabiliteService.ouvrirCaisse(payload.get("fondCaisse")));
    }

    @PatchMapping("/caisse/fermer")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<CaisseResponseDTO> fermerCaisse(@RequestBody Map<String, Double> payload) {
        return ResponseEntity.ok(comptabiliteService.fermerCaisse(payload.get("soldeFinal")));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN','COMPTABLE')")
    public ResponseEntity<byte[]> genererExport(
            @RequestParam(required = false, defaultValue = "CSV") String format,
            @RequestParam(required = false) String dateDebut,
            @RequestParam(required = false) String dateFin) {
        byte[] fileBytes = comptabiliteService.genererExport(format, dateDebut, dateFin);
        HttpHeaders headers = new HttpHeaders();

        if ("CSV".equalsIgnoreCase(format)) {
            headers.setContentType(MediaType.parseMediaType("text/csv"));
            headers.setContentDispositionFormData("attachment", "export.csv");
        } else {
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", "export." + format.toLowerCase());
        }

        return new ResponseEntity<>(fileBytes, headers, HttpStatus.OK);
    }
}
