# Configuration Package

Contient toutes les classes de configuration du projet.

## Principales Configurations

- **CorsConfig**: Gère les politiques d'accès Cross-Origin pour le frontend.
- **SupabaseConfig**: Centralise les clés d'API et les URLs pour l'intégration avec Supabase (Auth & Storage).
- **JpaConfig**: Active l'audit JPA (`@CreatedDate`, `@LastModifiedDate`).
- **OpenApiConfig**: Configuration Swagger/OpenAPI pour la documentation des points d'accès.

## Intégration Supabase

Le système utilise Supabase pour :
1. **Authentification** : Gestion des utilisateurs et des jetons via l'API REST Supabase Auth.
2. **Stockage** : Gestion des documents (fichiers PDF) dans le bucket `sgm-documents`.
