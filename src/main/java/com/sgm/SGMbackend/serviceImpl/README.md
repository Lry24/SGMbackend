# Service Implementation Package

Contient la logique métier concrète du système.

## Règles de Gestion

- Les services gèrent les transactions JPA au niveau des méthodes.
- Ils assurent la conversion Entity <-> DTO via les mappers.
- Ils lancent des `BusinessRuleException` ou `ResourceNotFoundException` en cas d'erreur métier.

## Intégrations Externes

- **AuthServiceImpl**: Communique avec Supabase Auth via `RestTemplate`.
- **DocumentServiceImpl**: Interagit avec Supabase Storage pour le stockage binaire.
