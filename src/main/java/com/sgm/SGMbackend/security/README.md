# Security Package

Ce package contient les composants liés à la sécurité et à l'authentification JWT du système SGM.

## Composants clés

- **JwtAuthFilter**: Filtre de sécurité personnalisé qui intercepte chaque requête HTTP pour valider le jeton JWT fourni dans l'en-tête `Authorization`.
- **JwtUtils**: Utilitaire pour la génération, la validation et l'extraction d'informations à partir des jetons JWT.

## Fonctionnement

1. Le client envoie une requête avec un header `Authorization: Bearer <token>`.
2. `JwtAuthFilter` extrait le token et utilise `JwtUtils` pour valider la signature (via le secret partagé avec Supabase).
3. Si le token est valide, l'utilisateur est authentifié dans le `SecurityContextHolder` de Spring.
