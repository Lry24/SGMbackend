package com.sgm.SGMbackend.entity.enums;


public enum StatutFacture {
    BROUILLON,            // En cours de rédaction
    EMISE,                // Envoyée à la famille (ne peut plus être modifiée)
    PARTIELLEMENT_PAYEE,  // Un ou plusieurs paiements partiels enregistrés
    PAYEE,                // Entièrement soldée → débloque la restitution
    ANNULEE               // Annulée avec avoir
}