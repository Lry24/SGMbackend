package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoRequest.UtilisateurRequestDTO;
import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;
import com.sgm.SGMbackend.entity.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Service de gestion des utilisateurs (CRUD + gestion compte Supabase).
 */
public interface UtilisateurService {

    Page<UtilisateurResponseDTO> findAll(Pageable pageable, Role role);

    UtilisateurResponseDTO findById(String id);

    /**
     * Crée un utilisateur dans Supabase Auth ET en base locale.
     */
    UtilisateurResponseDTO create(UtilisateurRequestDTO dto);

    UtilisateurResponseDTO update(String id, UtilisateurRequestDTO dto);

    /**
     * Active ou désactive le compte sans le supprimer.
     */
    void setActif(String id, Boolean actif);

    /**
     * Envoie un email de reset password via Supabase Admin API.
     */
    void resetPassword(String id);

    /**
     * Supprime le compte de la DB locale et de Supabase Auth.
     */
    void delete(String id);
}
