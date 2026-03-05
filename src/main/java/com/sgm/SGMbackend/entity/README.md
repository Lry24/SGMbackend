# Entity Package

Contient les entités JPA représentant le modèle de données du système SGM.

## Caractéristiques

- **Audit**: La plupart des entités héritent des mécanismes d'audit (`@CreatedDate`, `@LastModifiedDate`).
- **Lombok**: Utilisation intensive de `@Data`, `@Builder`, `@NoArgsConstructor` et `@AllArgsConstructor` pour réduire le code boilerplate.
- **Relations**: Gestion des relations OneToMany/ManyToOne (ex: Depouille -> Famille, Chambre -> Emplacement).

## Énumérations

Les enums (sous-package `enums`) définissent les états possibles des entités (StatutDepouille, Role, etc.).
