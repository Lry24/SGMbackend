package com.sgm.SGMbackend.entity.enums;

/**
 * Workflow : RECUE → EN_CHAMBRE_FROIDE → EN_AUTOPSIE → PREPAREE → RESTITUEE
 * Transitions autorisées uniquement dans cet ordre.
 */
public enum StatutDepouille {
    RECUE, // Vient d'être enregistrée
    EN_CHAMBRE_FROIDE, // Affectée à un emplacement
    EN_AUTOPSIE, // Autopsie en cours
    PREPAREE, // Préparation terminée, en attente de restitution
    RESTITUEE // Remise à la famille
}
