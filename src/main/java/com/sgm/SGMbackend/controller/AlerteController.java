package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.AlerteConfigRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteConfigResponseDTO;
import com.sgm.SGMbackend.dto.dtoResponse.AlerteResponseDTO;
import com.sgm.SGMbackend.entity.enums.TypeAlerte;
import com.sgm.SGMbackend.service.AlerteService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/alertes")
@RequiredArgsConstructor
@Tag(name = "Gestion des Alertes", description = "Endpoints pour la gestion des alertes et configurations")
public class AlerteController {

    private final AlerteService alerteService;

    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<AlerteResponseDTO>> list(
            @RequestParam(required = false) TypeAlerte type,
            Pageable pageable) {
        return ResponseEntity.ok(alerteService.findAll(type, pageable));
    }

    @GetMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AlerteConfigResponseDTO>> listConfigs() {
        return ResponseEntity.ok(alerteService.findAllConfigs());
    }

    @PostMapping("/config")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AlerteConfigResponseDTO> saveConfig(@Valid @RequestBody AlerteConfigRequestDTO req) {
        return ResponseEntity.status(201).body(alerteService.saveConfig(req));
    }

    @PatchMapping("/{id}/acquitter")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','AGENT')")
    public ResponseEntity<Void> acquitter(
            @PathVariable Long id,
            @RequestBody Map<String, String> body) {
        alerteService.acquitter(id, body != null ? body.get("commentaire") : null);
        return ResponseEntity.ok().build();
    }
}
