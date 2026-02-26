package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.BaremeRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.BaremeResponseDTO;
import com.sgm.SGMbackend.service.BaremeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/bareme")
@RequiredArgsConstructor
public class BaremeController {

    private final BaremeService baremeService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<List<BaremeResponseDTO>> getBareme() {
        return ResponseEntity.ok(baremeService.getActifs());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaremeResponseDTO> create(@Valid @RequestBody BaremeRequestDTO dto) {
        return ResponseEntity.status(HttpStatus.CREATED).body(baremeService.creer(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<BaremeResponseDTO> update(@PathVariable Long id, @RequestBody BaremeRequestDTO dto) {
        return ResponseEntity.ok(baremeService.modifier(id, dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        baremeService.desactiver(id);
        return ResponseEntity.noContent().build();
    }
}
