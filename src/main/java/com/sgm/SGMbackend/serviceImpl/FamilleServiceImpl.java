package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.dto.dtoRequest.FamilleRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.FamilleResponseDTO;
import com.sgm.SGMbackend.entity.Depouille;
import com.sgm.SGMbackend.entity.Famille;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.mapper.FamilleMapper;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.FamilleRepository;
import com.sgm.SGMbackend.service.FamilleService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class FamilleServiceImpl implements FamilleService {

    private final FamilleRepository familleRepository;
    private final DepouilleRepository depouilleRepository;
    private final FamilleMapper familleMapper;

    @Override
    public Page<FamilleResponseDTO> findAll(Pageable pageable) {
        return familleRepository.findAll(pageable).map(familleMapper::toResponseDTO);
    }

    @Override
    public FamilleResponseDTO findById(Long id) {
        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable : " + id));
        return familleMapper.toResponseDTO(famille);
    }

    @Override
    @Transactional
    public FamilleResponseDTO create(FamilleRequestDTO dto) {
        // RÈGLE : Unicité du téléphone
        if (familleRepository.existsByTelephone(dto.getTelephone())) {
            throw new BusinessRuleException("Un dossier famille existe déjà avec ce numéro de téléphone.");
        }

        Famille famille = familleMapper.toEntity(dto);
        famille.setActif(true);
        return familleMapper.toResponseDTO(familleRepository.save(famille));
    }

    @Override
    @Transactional
    public FamilleResponseDTO update(Long id, FamilleRequestDTO dto) {
        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable : " + id));

        // Si on change le téléphone, vérifier l'unicité
        if (!famille.getTelephone().equals(dto.getTelephone()) &&
                familleRepository.existsByTelephone(dto.getTelephone())) {
            throw new BusinessRuleException("Ce numéro de téléphone est déjà utilisé par une autre famille.");
        }

        familleMapper.updateEntity(dto, famille);
        return familleMapper.toResponseDTO(familleRepository.save(famille));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Famille famille = familleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable : " + id));
        // RÈGLE : Soft delete uniquement
        famille.setActif(false);
        familleRepository.save(famille);
    }

    @Override
    public Page<FamilleResponseDTO> recherche(String q, Pageable pageable) {
        return familleRepository.rechercherActives(q, pageable).map(familleMapper::toResponseDTO);
    }

    @Override
    @Transactional
    public void lierDepouille(Long familleId, Long depouilleId) {
        Famille famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable : " + familleId));

        Depouille depouille = depouilleRepository.findById(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable : " + depouilleId));

        if (depouille.getFamille() != null && !depouille.getFamille().getId().equals(familleId)) {
            throw new BusinessRuleException("Cette dépouille est déjà liée à une autre famille.");
        }

        depouille.setFamille(famille);
        depouilleRepository.save(depouille);
    }

    @Override
    public List<com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO> getDepouillesByFamille(Long id) {
        return depouilleRepository.findByFamille_Id(id)
                .stream()
                .map(d -> com.sgm.SGMbackend.dto.dtoResponse.DepouilleResponseDTO.builder()
                        .id(d.getId())
                        .identifiantUnique(d.getIdentifiantUnique())
                        .nomDefunt(d.getNomDefunt())
                        .prenomDefunt(d.getPrenomDefunt())
                        .statut(d.getStatut())
                        .build())
                .toList();
    }
}
