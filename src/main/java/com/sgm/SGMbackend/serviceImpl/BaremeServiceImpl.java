package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Bareme;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.BaremeRepository;
import com.sgm.SGMbackend.service.BaremeService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class BaremeServiceImpl implements BaremeService {

    private final BaremeRepository baremeRepository;

    @Override
    public Bareme creer(Bareme bareme) {
        bareme.setActif(true);
        return baremeRepository.save(bareme);
    }

    @Override
    public Bareme modifier(Long id, Bareme bareme) {
        Bareme existing = findById(id);
        existing.setNom(bareme.getNom());
        existing.setPrix(bareme.getPrix());
        existing.setUnite(bareme.getUnite());
        existing.setActif(bareme.getActif());
        return baremeRepository.save(existing);
    }

    @Override
    public void softDelete(Long id) {
        Bareme existing = findById(id);
        existing.setActif(false); // Soft delete
        baremeRepository.save(existing);
    }

    @Override
    public List<Bareme> findAllActifs() {
        return baremeRepository.findByActifTrue();
    }

    @Override
    public Bareme findById(Long id) {
        return baremeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Prestation introuvable dans le barème : " + id));
    }
}
