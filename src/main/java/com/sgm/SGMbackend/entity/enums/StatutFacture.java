package com.sgm.SGMbackend.entity.enums;

/**
 * Statuts possibles d'une facture (module DEV D — Facturation).
 * Stub utilisé par RestitutionService pour vérifier le paiement.
 */
public enum StatutFacture {
    EN_ATTENTE, // Facture créée, en attente de paiement
    PARTIELLEMENT_PAYEE, // Paiement partiel reçu
    PAYEE, // Facture entièrement soldée
    ANNULEE // Facture annulée
}
