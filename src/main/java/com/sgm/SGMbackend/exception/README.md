# Exception Package

Centralise la gestion des erreurs du système.

## Exceptions Personnalisées

- **BusinessRuleException**: Lancée pour toute violation d'une règle métier (ex: retrait d'un corps non payé).
- **ResourceNotFoundException**: Lancée quand un ID ne correspond à aucune donnée en base (404).

## Gestion Globale

Le système utilise un mécanisme global (souvent un `@ControllerAdvice`) pour intercepter ces exceptions et retourner des réponses JSON formatées avec le code HTTP approprié.
