# Repository Package

Contient les interfaces Spring Data JPA pour l'accès aux données.

## Fonctionnement

Toutes les interfaces étendent `JpaRepository` pour fournir les opérations CRUD standard.
Des méthodes personnalisées (ex: `findByEmail`, `findAllActive`) sont définies pour les besoins spécifiques du métier.
