package com.sgm.SGMbackend.serviceImpl;

import com.sgm.SGMbackend.entity.Facture;
import com.sgm.SGMbackend.entity.LigneFacture;
import com.sgm.SGMbackend.entity.MouvementCaisse;
import com.sgm.SGMbackend.entity.enums.StatutFacture;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.FactureRepository;
import com.sgm.SGMbackend.repository.LigneFactureRepository;
import com.sgm.SGMbackend.repository.DepouilleRepository;
import com.sgm.SGMbackend.repository.FamilleRepository;
import com.sgm.SGMbackend.repository.MouvementCaisseRepository;
import com.sgm.SGMbackend.repository.BaremeRepository;
import com.sgm.SGMbackend.repository.AutopsieRepository;
import com.sgm.SGMbackend.entity.Bareme;
import com.sgm.SGMbackend.entity.enums.GraviteAudit;
import com.sgm.SGMbackend.entity.Autopsie;
import com.sgm.SGMbackend.service.AuditLogService;
import com.sgm.SGMbackend.service.FactureService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

@Service
@RequiredArgsConstructor
public class FactureServiceImpl implements FactureService {

    private final FactureRepository factureRepository;
    private final LigneFactureRepository ligneRepository;
    private final DepouilleRepository depouilleRepository;
    private final FamilleRepository familleRepository;
    private final MouvementCaisseRepository mouvementRepository;
    private final BaremeRepository baremeRepository;
    private final AutopsieRepository autopsieRepository;
    private final AuditLogService auditLogService;

    @Override
    @Transactional
    public Facture creer(Long depouilleId, Long familleId, List<LigneFacture> lignes, Double remise) {
        var depouille = depouilleRepository.findById(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable"));

        var famille = familleRepository.findById(familleId)
                .orElseThrow(() -> new ResourceNotFoundException("Famille introuvable"));

        // Calculer le montant total
        double total = lignes.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire()).sum();

        double totalAvecRemise = total - (remise != null ? remise : 0.0);

        Facture f = Facture.builder()
                .numero(genererNumero())
                .depouille(depouille)
                .famille(famille)
                .montantTotal(totalAvecRemise)
                .montantPaye(0.0)
                .remise(remise != null ? remise : 0.0)
                .statut(StatutFacture.BROUILLON)
                .build();

        f = factureRepository.save(f);

        // Sauvegarder les lignes avec la référence à la facture
        final Facture factureFinal = f;
        lignes.forEach(l -> l.setFacture(factureFinal));
        ligneRepository.saveAll(lignes);

        return f;
    }

    @Override
    @Transactional
    public Facture enregistrerPaiement(Long id, Double montant, String mode, String reference) {
        Facture f = findById(id);

        if (f.getStatut() == StatutFacture.ANNULEE) {
            throw new BusinessRuleException("Impossible de payer une facture annulée.");
        }
        if (f.getStatut() == StatutFacture.PAYEE) {
            throw new BusinessRuleException("Cette facture est déjà entièrement payée.");
        }

        double nouveauPaye = (f.getMontantPaye() != null ? f.getMontantPaye() : 0.0) + montant;

        if (nouveauPaye > f.getMontantTotal()) {
            throw new BusinessRuleException("Le montant payé dépasse le total de la facture.");
        }

        f.setMontantPaye(nouveauPaye);

        // Mise à jour automatique du statut
        if (nouveauPaye >= f.getMontantTotal()) {
            f.setStatut(StatutFacture.PAYEE);
        } else {
            f.setStatut(StatutFacture.PARTIELLEMENT_PAYEE);
        }

        // Si la facture n'avait pas encore de date d'émission (ex: passage direct de
        // BROUILLON à PAYEE)
        if (f.getDateEmission() == null) {
            f.setDateEmission(LocalDateTime.now());
        }

        // Enregistrer le mouvement de caisse
        MouvementCaisse mv = MouvementCaisse.builder()
                .date(LocalDateTime.now())
                .montant(montant)
                .type("ENCAISSEMENT")
                .modePaiement(mode)
                .libelle("Paiement Facture " + f.getNumero() + " (" + reference + ")")
                .facture(f)
                .build();
        mouvementRepository.save(mv);

        auditLogService.log(
                "COMPTABLE",
                "ENREGISTREMENT_PAIEMENT",
                "FACTURATION",
                "Paiement de " + montant + " FCFA pour la facture " + f.getNumero(),
                GraviteAudit.INFO);

        return factureRepository.save(f);
    }

    @Override
    @Transactional
    public Facture emettre(Long id) {
        Facture f = findById(id);

        if (f.getStatut() != StatutFacture.BROUILLON) {
            throw new BusinessRuleException("Seul un BROUILLON peut être émis.");
        }

        f.setStatut(StatutFacture.EMISE);
        f.setDateEmission(LocalDateTime.now());

        Facture saved = factureRepository.save(f);

        auditLogService.log(
                "COMPTABLE",
                "ÉMISSION_FACTURE",
                "FACTURATION",
                "Facture émise: " + f.getNumero(),
                GraviteAudit.INFO);

        return saved;
    }

    @Override
    @Transactional
    public Facture annuler(Long id, String motif) {
        Facture f = findById(id);

        if (f.getStatut() == StatutFacture.PAYEE) {
            throw new BusinessRuleException("Une facture entièrement payée ne peut pas être annulée.");
        }

        f.setStatut(StatutFacture.ANNULEE);
        f.setMotifAnnulation(motif);

        Facture saved = factureRepository.save(f);

        auditLogService.log(
                "COMPTABLE",
                "ANNULATION_FACTURE",
                "FACTURATION",
                "Facture " + f.getNumero() + " annulée. Motif: " + motif,
                GraviteAudit.WARNING);

        return saved;
    }

    @Override
    public Facture findById(Long id) {
        return factureRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + id));
    }

    @Override
    @Transactional
    public Facture updateStatut(Long id, StatutFacture statut) {
        Facture f = findById(id);
        f.setStatut(statut);
        return factureRepository.save(f);
    }

    @Override
    public Page<Facture> findAll(Pageable pageable, StatutFacture statut) {
        if (statut != null) {
            return factureRepository.findByStatut(statut, pageable);
        }
        return factureRepository.findAll(pageable);
    }

    @Override
    public Facture findByDepouille(Long depouilleId) {
        List<Facture> factures = factureRepository.findByDepouilleId(depouilleId);
        if (factures.isEmpty()) {
            return null;
        }
        // On cherche d'abord une facture non annulée
        return factures.stream()
                .filter(f -> f.getStatut() != StatutFacture.ANNULEE)
                .max((f1, f2) -> f1.getId().compareTo(f2.getId())) // On prend la plus récente par ID
                .orElseGet(() -> factures.get(factures.size() - 1)); // Sinon la toute dernière
    }

    @Override
    public Double calculerEstimation(Long depouilleId) {
        var depouille = depouilleRepository.findById(depouilleId)
                .orElseThrow(() -> new ResourceNotFoundException("Dépouille introuvable"));

        // 1. Durée du séjour
        long jours = java.time.Duration.between(depouille.getDateArrivee(), LocalDateTime.now()).toDays();
        if (jours == 0)
            jours = 1;

        double total = 0.0;
        List<Bareme> baremesActifs = baremeRepository.findByActifTrue();
        boolean hasAutopsie = autopsieRepository.existsByDepouille_Id(depouilleId);

        for (Bareme b : baremesActifs) {
            String nom = b.getNom().toLowerCase();

            // Cas 1 : Tarifs journaliers (Séjour / Chambre Froide)
            if ("jour".equalsIgnoreCase(b.getUnite()) || nom.contains("séjour") || nom.contains("chambre")) {
                total += (jours * b.getPrix());
            }
            // Cas 2 : Autopsie (seulement si effectuée)
            else if (nom.contains("autopsie")) {
                if (hasAutopsie) {
                    total += b.getPrix();
                }
            }
            // Cas 3 : Autres prestations fixes (Toilette, Mise en bière, etc.)
            else {
                total += b.getPrix();
            }
        }

        if (total == 0.0) {
            throw new BusinessRuleException(
                    "Aucun tarif actif n'a été trouvé dans le barème. Veuillez configurer les prix (Séjour, Toilette, etc.).");
        }

        return total;
    }

    @Override
    @Transactional
    public Facture modifier(Long id, List<LigneFacture> lignes, Double remise) {
        Facture f = findById(id);
        if (f.getStatut() != StatutFacture.BROUILLON) {
            throw new BusinessRuleException("Seule une facture en BROUILLON peut être modifiée.");
        }

        // Supprimer anciennes lignes
        ligneRepository.deleteAll(f.getLignes());

        // Mettre à jour les lignes
        lignes.forEach(l -> l.setFacture(f));
        ligneRepository.saveAll(lignes);

        // Recalculer total
        double total = lignes.stream()
                .mapToDouble(l -> l.getQuantite() * l.getPrixUnitaire()).sum();
        double totalAvecRemise = total - (remise != null ? remise : 0.0);

        f.setLignes(lignes);
        f.setRemise(remise != null ? remise : 0.0);
        f.setMontantTotal(totalAvecRemise);

        return factureRepository.save(f);
    }

    @Override
    public List<MouvementCaisse> findPaiementsByFacture(Long id) {
        return mouvementRepository.findByFacture_Id(id);
    }

    @Override
    public byte[] generatePdf(Long id) {
        Facture f = findById(id);

        try (java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream()) {
            com.lowagie.text.Document doc = new com.lowagie.text.Document(com.lowagie.text.PageSize.A4, 40, 40, 60, 40);
            com.lowagie.text.pdf.PdfWriter.getInstance(doc, baos);
            doc.open();

            // ── Couleurs ─────────────────────────────────────────────────────
            java.awt.Color bleuFonce = new java.awt.Color(30, 58, 138);
            java.awt.Color bleuClaire = new java.awt.Color(219, 234, 254);
            java.awt.Color gris = new java.awt.Color(107, 114, 128);
            java.awt.Color noir = java.awt.Color.BLACK;

            // ── Polices ──────────────────────────────────────────────────────
            com.lowagie.text.Font fontTitre = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 18,
                    com.lowagie.text.Font.BOLD, bleuFonce);
            com.lowagie.text.Font fontSousTitre = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                    com.lowagie.text.Font.NORMAL, gris);
            com.lowagie.text.Font fontSection = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11,
                    com.lowagie.text.Font.BOLD, bleuFonce);
            com.lowagie.text.Font fontLabel = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 9,
                    com.lowagie.text.Font.BOLD, gris);
            com.lowagie.text.Font fontValeur = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 10,
                    com.lowagie.text.Font.NORMAL, noir);
            com.lowagie.text.Font fontSmall = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 8,
                    com.lowagie.text.Font.ITALIC, gris);
            com.lowagie.text.Font fontTotal = new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 12,
                    com.lowagie.text.Font.BOLD, bleuFonce);

            // ── En-tête ───────────────────────────────────────────────────────
            com.lowagie.text.pdf.PdfPTable header = new com.lowagie.text.pdf.PdfPTable(2);
            header.setWidthPercentage(100);
            header.setWidths(new float[] { 65, 35 });

            com.lowagie.text.pdf.PdfPCell cellLeft = new com.lowagie.text.pdf.PdfPCell();
            cellLeft.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            cellLeft.addElement(new com.lowagie.text.Paragraph("SYSTÈME DE GESTION DE MORGUE", fontTitre));
            cellLeft.addElement(new com.lowagie.text.Paragraph("Facture Officielle — " + f.getNumero(), fontSousTitre));
            header.addCell(cellLeft);

            com.lowagie.text.pdf.PdfPCell cellRight = new com.lowagie.text.pdf.PdfPCell();
            cellRight.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            cellRight.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

            LocalDateTime dEmission = f.getDateEmission() != null ? f.getDateEmission() : f.getCreatedAt();
            com.lowagie.text.Paragraph dateInfo = new com.lowagie.text.Paragraph("Date émission : " +
                    (dEmission != null
                            ? dEmission.format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm"))
                            : "N/A"),
                    fontSmall);

            dateInfo.setAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
            cellRight.addElement(dateInfo);
            header.addCell(cellRight);

            doc.add(header);

            // Ligne séparatrice bleue
            com.lowagie.text.pdf.PdfPTable sep = new com.lowagie.text.pdf.PdfPTable(1);
            sep.setWidthPercentage(100);
            sep.setSpacingBefore(4);
            sep.setSpacingAfter(14);
            com.lowagie.text.pdf.PdfPCell sepCell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(" "));
            sepCell.setBackgroundColor(bleuFonce);
            sepCell.setFixedHeight(2);
            sepCell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
            sep.addCell(sepCell);
            doc.add(sep);

            // ── Informations Client & Défunt ──────────────────────────────────
            doc.add(createSectionTitle("Informations Bénéficiaire", fontSection, bleuClaire));

            com.lowagie.text.pdf.PdfPTable infoTable = new com.lowagie.text.pdf.PdfPTable(4);
            infoTable.setWidthPercentage(100);
            infoTable.setSpacingAfter(14);

            addInfoRow(infoTable, "Tuteur (Famille)", f.getFamille().getTuteurLegal(),
                    "Téléphone", f.getFamille().getTelephone(), fontLabel, fontValeur);
            addInfoRow(infoTable, "Nom Défunt",
                    f.getDepouille().getNomDefunt() + " " + f.getDepouille().getPrenomDefunt(),
                    "Statut Facture", f.getStatut().name(), fontLabel, fontValeur);

            doc.add(infoTable);

            // ── Tableau des prestations ───────────────────────────────────────
            doc.add(createSectionTitle("Détail des Prestations", fontSection, bleuClaire));

            com.lowagie.text.pdf.PdfPTable table = new com.lowagie.text.pdf.PdfPTable(4);
            table.setWidthPercentage(100);
            table.setWidths(new float[] { 45, 15, 20, 20 });
            table.setSpacingBefore(6);
            table.setSpacingAfter(14);

            addTableHeaderCell(table, "Désignation", fontSection, bleuClaire);
            addTableHeaderCell(table, "Qté", fontSection, bleuClaire);
            addTableHeaderCell(table, "P. Unitaire", fontSection, bleuClaire);
            addTableHeaderCell(table, "Total HT", fontSection, bleuClaire);

            for (LigneFacture l : f.getLignes()) {
                table.addCell(createCell(l.getPrestation(), fontValeur, false));
                table.addCell(createCell(String.valueOf(l.getQuantite()), fontValeur, true));
                table.addCell(createCell(String.format("%,.0f", l.getPrixUnitaire()), fontValeur, true));
                table.addCell(createCell(String.format("%,.0f", l.getMontantLigne()), fontValeur, true));
            }
            doc.add(table);

            // ── Totaux ────────────────────────────────────────────────────────
            com.lowagie.text.pdf.PdfPTable totalTable = new com.lowagie.text.pdf.PdfPTable(2);
            totalTable.setWidthPercentage(40);
            totalTable.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);

            addTotalRow(totalTable, "REMISE", String.format("%,.0f FCFA", f.getRemise()), fontLabel, fontValeur);
            addTotalRow(totalTable, "TOTAL À PAYER", String.format("%,.0f FCFA", f.getMontantTotal()), fontLabel,
                    fontTotal);
            addTotalRow(totalTable, "DÉJÀ PAYÉ", String.format("%,.0f FCFA", f.getMontantPaye()), fontLabel,
                    fontValeur);
            addTotalRow(totalTable, "RESTE À RECOUVRER",
                    String.format("%,.0f FCFA", (f.getMontantTotal() - f.getMontantPaye())), fontLabel, fontTotal);

            doc.add(totalTable);

            // ── Historique des paiements ──────────────────────────────────────
            List<MouvementCaisse> paiements = mouvementRepository.findByFacture_Id(f.getId());
            if (!paiements.isEmpty()) {
                doc.add(createSectionTitle("Historique des Règlements", fontSection, bleuClaire));

                com.lowagie.text.pdf.PdfPTable payTable = new com.lowagie.text.pdf.PdfPTable(3);
                payTable.setWidthPercentage(100);
                payTable.setSpacingBefore(6);

                addTableHeaderCell(payTable, "Date & Heure", fontSection, bleuClaire);
                addTableHeaderCell(payTable, "Mode de paiement", fontSection, bleuClaire);
                addTableHeaderCell(payTable, "Montant Versé", fontSection, bleuClaire);

                for (MouvementCaisse m : paiements) {
                    payTable.addCell(createCell(
                            m.getDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm")),
                            fontValeur, false));
                    payTable.addCell(createCell(m.getModePaiement(), fontValeur, false));
                    payTable.addCell(createCell(String.format("%,.0f FCFA", m.getMontant()), fontValeur, true));
                }
                doc.add(payTable);
            }

            // ── Message Restitution & Pied de page ────────────────────────────
            if (f.getStatut() == StatutFacture.PAYEE) {
                com.lowagie.text.Paragraph msg = new com.lowagie.text.Paragraph(
                        "\n\nVous pouvez vous rendre chez le responsable ou l'agent pour la restitution de la dépouille Merci.",
                        new com.lowagie.text.Font(com.lowagie.text.Font.HELVETICA, 11, com.lowagie.text.Font.BOLDITALIC,
                                bleuFonce));
                msg.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
                doc.add(msg);
            }

            com.lowagie.text.Paragraph footer = new com.lowagie.text.Paragraph(
                    "\n\nCette facture est un document officiel généré par le SGM. " +
                            "Toute rature ou modification manuelle annule la validité du document.",
                    fontSmall);
            footer.setAlignment(com.lowagie.text.Element.ALIGN_CENTER);
            doc.add(footer);

            doc.close();
            return baos.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException("Erreur lors de la génération du PDF facture", e);
        }
    }

    private com.lowagie.text.pdf.PdfPTable createSectionTitle(String title, com.lowagie.text.Font font,
            java.awt.Color bgColor) throws com.lowagie.text.DocumentException {
        com.lowagie.text.pdf.PdfPTable t = new com.lowagie.text.pdf.PdfPTable(1);
        t.setWidthPercentage(100);
        t.setSpacingBefore(10);
        t.setSpacingAfter(4);
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(title, font));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        t.addCell(cell);
        return t;
    }

    private void addInfoRow(com.lowagie.text.pdf.PdfPTable table, String l1, String v1, String l2, String v2,
            com.lowagie.text.Font fL, com.lowagie.text.Font fV) {
        table.addCell(createLabelCell(l1, fL));
        table.addCell(createValueCell(v1, fV));
        table.addCell(createLabelCell(l2, fL));
        table.addCell(createValueCell(v2, fV));
    }

    private com.lowagie.text.pdf.PdfPCell createLabelCell(String text, com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(text != null ? text : "—", font));
        cell.setBorder(com.lowagie.text.Rectangle.BOTTOM);
        cell.setBorderColor(new java.awt.Color(229, 231, 235));
        cell.setPadding(6);
        cell.setBackgroundColor(new java.awt.Color(249, 250, 251));
        return cell;
    }

    private com.lowagie.text.pdf.PdfPCell createValueCell(String text, com.lowagie.text.Font font) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(text != null ? text : "—", font));
        cell.setBorder(com.lowagie.text.Rectangle.BOTTOM);
        cell.setBorderColor(new java.awt.Color(229, 231, 235));
        cell.setPadding(6);
        return cell;
    }

    private void addTableHeaderCell(com.lowagie.text.pdf.PdfPTable table, String text, com.lowagie.text.Font font,
            java.awt.Color bg) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(text, font));
        cell.setBackgroundColor(bg);
        cell.setPadding(6);
        cell.setBorderColor(new java.awt.Color(209, 213, 219));
        table.addCell(cell);
    }

    private com.lowagie.text.pdf.PdfPCell createCell(String text, com.lowagie.text.Font font, boolean alignRight) {
        com.lowagie.text.pdf.PdfPCell cell = new com.lowagie.text.pdf.PdfPCell(
                new com.lowagie.text.Phrase(text != null ? text : "—", font));
        cell.setPadding(6);
        cell.setBorderColor(new java.awt.Color(229, 231, 235));
        if (alignRight)
            cell.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        return cell;
    }

    private void addTotalRow(com.lowagie.text.pdf.PdfPTable table, String label, String value, com.lowagie.text.Font fL,
            com.lowagie.text.Font fV) {
        com.lowagie.text.pdf.PdfPCell cL = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(label, fL));
        cL.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cL.setPadding(4);
        table.addCell(cL);

        com.lowagie.text.pdf.PdfPCell cV = new com.lowagie.text.pdf.PdfPCell(new com.lowagie.text.Phrase(value, fV));
        cV.setBorder(com.lowagie.text.Rectangle.NO_BORDER);
        cV.setHorizontalAlignment(com.lowagie.text.Element.ALIGN_RIGHT);
        cV.setPadding(4);
        table.addCell(cV);
    }

    @Override
    public Facture findByNumero(String numero) {
        return factureRepository.findByNumero(numero)
                .orElseThrow(() -> new ResourceNotFoundException("Facture introuvable : " + numero));
    }

    private String genererNumero() {
        int annee = java.time.Year.now().getValue();
        long count = factureRepository.count() + 1;
        String suffix = java.util.UUID.randomUUID().toString().substring(0, 4).toUpperCase();
        return String.format("FAC-%d-%04d-%s", annee, count, suffix);
    }
}
