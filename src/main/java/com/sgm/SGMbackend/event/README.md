# Event Package

Gère la communication asynchrone entre les différents modules du système.

## Événements Actuels

- **RestitutionPlanifieeEvent**: Déclenché lorsqu'une restitution est planifiée, permettant par exemple l'envoi de notifications ou la mise à jour automatique des stocks.

## Avantages

- Faible couplage entre les services.
- Extensibilité facilitée (ajout de nouveaux écouteurs sans modifier le code source de l'événement).
