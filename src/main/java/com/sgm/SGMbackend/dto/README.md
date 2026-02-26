# DTO Package

Contient les Data Transfer Objects utilisés pour les échanges entre l'API et le client.

## Structure

- **dtoRequest**: Reçoit les données brutes du client (validation via `@Valid`).
- **dtoResponse**: Formate les données envoyées au client (protection des champs sensibles comme les mots de passe).

## Avantages

- Découplage du modèle de base de données.
- Contrôle précis de la structure JSON.
- Validation automatique des entrées.
