package com.sgm.SGMbackend.entity.enums;

// Workflow : BROUILLON → EMISE → PARTIELLEMENT_PAYEE → PAYEE
//                         ↘ ANNULEE (depuis BROUILLON ou EMISE uniquement)
public enum StatutFacture {
    BROUILLON, // En cours de rédaction
    EMISE, // Envoyée à la famille (ne peut plus être modifiée)
    PARTIELLEMENT_PAYEE, // Un ou plusieurs paiements partiels enregistrés
    PAYEE, // Entièrement soldée → débloque la restitution
    ANNULEE // Annulée avec avoir
}
