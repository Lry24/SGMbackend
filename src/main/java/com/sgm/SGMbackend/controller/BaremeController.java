package com.sgm.SGMbackend.controller;

import com.sgm.SGMbackend.dto.dtoRequest.BaremeRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.BaremeResponseDTO;
import com.sgm.SGMbackend.entity.Bareme;
import com.sgm.SGMbackend.mapper.BaremeMapper;
import com.sgm.SGMbackend.service.BaremeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/baremes")
@RequiredArgsConstructor
@Tag(name = "Gestion des Barèmes", description = "Endpoints pour la gestion de la tarification")
public class BaremeController {

    private final BaremeService baremeService;
    private final BaremeMapper baremeMapper;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE','COMPTABLE')")
    public ResponseEntity<List<BaremeResponseDTO>> getActifs() {
        return ResponseEntity.ok(baremeService.findAllActifs().stream()
                .map(baremeMapper::toResponseDTO)
                .collect(Collectors.toList()));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<BaremeResponseDTO> creer(@RequestBody BaremeRequestDTO dto) {
        Bareme b = baremeMapper.toEntity(dto);
        return ResponseEntity.status(201).body(baremeMapper.toResponseDTO(baremeService.creer(b)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RESPONSABLE')")
    public ResponseEntity<BaremeResponseDTO> modifier(@PathVariable Long id, @RequestBody BaremeRequestDTO dto) {
        Bareme b = baremeMapper.toEntity(dto);
        return ResponseEntity.ok(baremeMapper.toResponseDTO(baremeService.modifier(id, b)));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> supprimer(@PathVariable Long id) {
        baremeService.softDelete(id);
        return ResponseEntity.noContent().build();
    }
}
