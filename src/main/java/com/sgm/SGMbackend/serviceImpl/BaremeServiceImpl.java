package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.BaremeRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.BaremeResponseDTO;
import com.sgm.SGMbackend.entity.Bareme;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.BaremeRepository;
import com.sgm.SGMbackend.service.BaremeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BaremeServiceImpl implements BaremeService {

    private final BaremeRepository baremeRepository;

    @Override
    public List<BaremeResponseDTO> getActifs() {
        return baremeRepository.findByActifTrue().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public BaremeResponseDTO creer(BaremeRequestDTO dto) {
        Bareme bareme = Bareme.builder()
                .nom(dto.getNom())
                .prix(dto.getPrix())
                .unite(dto.getUnite())
                .description(dto.getDescription())
                .actif(true)
                .build();
        return toDto(baremeRepository.save(bareme));
    }

    @Override
    @Transactional
    public BaremeResponseDTO modifier(Long id, BaremeRequestDTO dto) {
        Bareme bareme = baremeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barème introuvable avec id " + id));
        if (dto.getPrix() != null)
            bareme.setPrix(dto.getPrix());
        if (dto.getDescription() != null)
            bareme.setDescription(dto.getDescription());
        // Normalement seul le prix et description peuvent être modifiés via ce
        // endpoint,
        // mais on peut aussi mapper nom et unite si besoin (selon specs
        // "prix,description")
        return toDto(baremeRepository.save(bareme));
    }

    @Override
    @Transactional
    public void desactiver(Long id) {
        Bareme bareme = baremeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barème introuvable avec id " + id));
        bareme.setActif(false);
        baremeRepository.save(bareme);
    }

    private BaremeResponseDTO toDto(Bareme entity) {
        return BaremeResponseDTO.builder()
                .id(entity.getId())
                .nom(entity.getNom())
                .prix(entity.getPrix())
                .unite(entity.getUnite())
                .description(entity.getDescription())
                .actif(entity.getActif())
                .build();
    }
}
