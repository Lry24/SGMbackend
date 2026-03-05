package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.entity.Document;
import com.sgm.SGMbackend.exception.BusinessRuleException;
import com.sgm.SGMbackend.exception.ResourceNotFoundException;
import com.sgm.SGMbackend.repository.DocumentRepository;
import com.sgm.SGMbackend.config.SupabaseConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.UUID;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final SupabaseConfig supabaseConfig;
    private final RestTemplate restTemplate;

    private static final String BUCKET = SupabaseConfig.BUCKET_DOCUMENTS;
    private static final long MAX_SIZE = 10 * 1024 * 1024; // 10 MB
    private static final List<String> TYPES_AUTORISES = List.of("application/pdf", "image/jpeg", "image/jpg",
            "image/png");

    @Transactional
    public Document upload(MultipartFile file, String typeDocument, String entiteType, Long entiteId) {
        // Validation taille et type
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessRuleException("Fichier trop volumineux (max 10 MB).");
        }
        if (!TYPES_AUTORISES.contains(file.getContentType())) {
            throw new BusinessRuleException("Type de fichier non autorisé. Formats acceptés : PDF, JPG, PNG.");
        }

        // Générer un chemin unique dans le bucket
        String chemin = entiteType.toLowerCase() + "/" + entiteId + "/" + UUID.randomUUID() + "_"
                + file.getOriginalFilename();

        // Upload vers Supabase Storage via REST API
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());
            headers.setContentType(MediaType.parseMediaType(file.getContentType()));

            HttpEntity<byte[]> entity = new HttpEntity<>(file.getBytes(), headers);
            String uploadUrl = supabaseConfig.getSupabaseUrl() + "/storage/v1/object/" + BUCKET + "/" + chemin;

            restTemplate.exchange(uploadUrl, HttpMethod.POST, entity, String.class);
        } catch (Exception e) {
            throw new BusinessRuleException("Erreur lors de l'upload : " + e.getMessage());
        }

        // Sauvegarder les métadonnées en base
        Document doc = Document.builder()
                .nomFichier(file.getOriginalFilename())
                .typeDocument(typeDocument)
                .cheminStorage(chemin)
                .tailleOctets(file.getSize())
                .mimeType(file.getContentType())
                .entiteType(entiteType)
                .entiteId(entiteId)
                .build();

        return documentRepository.save(doc);
    }

    public String genererLienTelecharge(Long id) {
        Document doc = findById(id);

        // Générer un lien signé valide 1 heure via Supabase Storage API
        String signUrl = supabaseConfig.getSupabaseUrl() + "/storage/v1/object/sign/" + BUCKET + "/"
                + doc.getCheminStorage();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<String> entity = new HttpEntity<>("{\"expiresIn\":3600}", headers);

        @SuppressWarnings("unchecked")
        ResponseEntity<Map> response = restTemplate.exchange(signUrl, HttpMethod.POST, entity, Map.class);

        if (response.getBody() != null && response.getBody().containsKey("signedURL")) {
            return supabaseConfig.getSupabaseUrl() + "/storage/v1" + response.getBody().get("signedURL");
        } else {
            throw new BusinessRuleException("Erreur lors de la génération du lien de téléchargement.");
        }
    }

    @Transactional
    public void supprimer(Long id) {
        Document doc = findById(id);

        // Supprimer du storage Supabase
        String deleteUrl = supabaseConfig.getSupabaseUrl() + "/storage/v1/object/" + BUCKET + "/"
                + doc.getCheminStorage();

        HttpHeaders headers = new HttpHeaders();
        headers.set("Authorization", "Bearer " + supabaseConfig.getServiceKey());

        try {
            restTemplate.exchange(deleteUrl, HttpMethod.DELETE, new HttpEntity<>(headers), String.class);
        } catch (Exception e) {
            // On peut logguer l'erreur mais on continue la suppression en DB si besoin
            // Ou on throw selon la criticité. Ici on suit les instructions.
        }

        // Supprimer de la DB
        documentRepository.delete(doc);
    }

    public Document findById(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document introuvable : " + id));
    }

    public List<Document> findByEntite(String type, Long id) {
        return documentRepository.findByEntiteTypeAndEntiteId(type, id);
    }
}
