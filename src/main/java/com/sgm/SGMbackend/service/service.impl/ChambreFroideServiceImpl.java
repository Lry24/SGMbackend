package com.sgm.SGMbackend.service.service.impl;


@Service
@RequiredArgsConstructor
@Transactional
public class ChambreFroideServiceImpl implements ChambreFroideService {

    private final ChambreFroideRepository chambreRepo;
    private final EmplacementRepository emplacementRepo;
    private final DepouillRepository depouillRepo;

    @Override
    public ChambreFroide creer(String numero,
                               int capacite,
                               float temperatureCible) {

        ChambreFroide chambre = ChambreFroide.builder()
                .numero(numero)
                .capacite(capacite)
                .temperatureCible(temperatureCible)
                .statut("OPERATIONNELLE")
                .build();

        chambre = chambreRepo.save(chambre);

        for (int i = 1; i <= capacite; i++) {

            emplacementRepo.save(
                    Emplacement.builder()
                            .numero("E" + i)
                            .occupe(false)
                            .chambreFroide(chambre)
                            .build()
            );
        }

        return chambre;
    }

    @Override
    public Emplacement affecter(Long depouilleId,
                                Long emplacementId) {

        Emplacement emp = emplacementRepo.findById(emplacementId)
                .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable"));

        if (emp.getOccupe())
            throw new BusinessRuleException("Emplacement déjà occupé");

        Depouille depouille = depouillRepo.findById(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable"));

        emp.setDepouille(depouille);
        emp.setOccupe(true);
        emp.setDateOccupation(LocalDateTime.now());

        return emplacementRepo.save(emp);
    }

    @Override
    public void liberer(Long emplacementId) {

        Emplacement emp = emplacementRepo.findById(emplacementId)
                .orElseThrow(() -> new ResourceNotFoundException("Emplacement introuvable"));

        emp.setDepouille(null);
        emp.setOccupe(false);
        emp.setDateOccupation(null);

        emplacementRepo.save(emp);
    }

    @Override
    public void enregistrerTemperature(Long chambreId,
                                       float temperature) {

        ChambreFroide chambre = chambreRepo.findById(chambreId)
                .orElseThrow(() -> new ResourceNotFoundException("Chambre introuvable"));

        chambre.setTemperatureActuelle(temperature);
        chambreRepo.save(chambre);
    }

    @Override
    public double calculerTauxOccupation(Long chambreId) {

        long total = emplacementRepo.findByChambreFroide_Id(chambreId).size();
        long occupes = emplacementRepo.countByChambreFroide_IdAndOccupeTrue(chambreId);

        return total == 0 ? 0 : (double) occupes / total * 100;
    }
}