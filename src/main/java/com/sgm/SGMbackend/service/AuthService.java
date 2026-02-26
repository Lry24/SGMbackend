package com.sgm.SGMbackend.service;

import com.sgm.SGMbackend.dto.dtoResponse.UtilisateurResponseDTO;

import java.util.Map;

/**
 * Service d'authentification — délègue à Supabase Auth.
 */
public interface AuthService {

    /**
     * Authentifie l'utilisateur via Supabase et retourne access_token +
     * refresh_token.
     */
    Map<String, Object> login(String email, String password);

    /**
     * Invalide la session Supabase de l'utilisateur courant.
     */
    void logout();

    /**
     * Renouvelle le token JWT à partir d'un refresh_token.
     */
    Map<String, Object> refresh(String refreshToken);

    /**
     * Retourne le profil de l'utilisateur actuellement authentifié.
     */
    UtilisateurResponseDTO getCurrentUser();

    /**
     * Modifie le mot de passe de l'utilisateur courant via Supabase.
     */
    void changePassword(String oldPassword, String newPassword);

    /**
     * Déclenche une demande de réinitialisation de mot de passe par email.
     */
    void forgotPassword(String email);
}
