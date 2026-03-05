# SGM Backend — Système de Gestion de Morgue

Ce projet est le backend Spring Boot pour le Système de Gestion de Morgue (SGM).

## Prérequis

- Java 17
- Maven 3.6+
- PostgreSQL (ou Supabase)
- Compte Supabase (pour le Storage)

## Installation

1. Cloner le repository.
2. Configurer les variables d'environnement ou modifier `src/main/resources/application.properties`.
3. Lancer la compilation :
   ```bash
   mvn clean install
   ```

## Utilisation

Lancer l'application :
```bash
mvn spring-boot:run
```

L'API est accessible sur `http://localhost:8181`.
La documentation Swagger est disponible sur `http://localhost:8181/swagger-ui.html`.

## Modules

- **Auth** : Authentification JWT, rôles (ADMIN, COMPTABLE, RESPONSABLE, AGENT, MEDECIN).
- **Dépouilles** : Gestion des corps, ID unique SGM-AAAA-NNNNN, QR Code.
- **Chambres Froides** : Gestion des emplacements, alertes température.
- **Facturation** : Devis, factures, paiements partiels.
- **Restitutions** : Workflow de sortie des corps.
- **Documents** : Stockage Supabase privé.
- **Alertes & Rapports** : Monitoring en temps réel.
