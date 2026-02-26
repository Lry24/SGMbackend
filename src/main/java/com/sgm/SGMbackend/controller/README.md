# Controller Package

Ce package contient les contrôleurs REST qui exposent les API du système SGM.

## Structure

Chaque contrôleur est responsable d'un domaine métier spécifique et délègue la logique métier aux services correspondants.

- **AlerteController**: Gestion des notifications et alertes automatiques.
- **AuthController**: Authentification, déconnexion et gestion des mots de passe.
- **AutopsieController**: Gestion des rapports d'autopsie.
- **BaremeController**: Gestion de la tarification des prestations.
- **ChambreFroideController**: Gestion des chambres et de leur état.
- **ComptabiliteController**: États financiers et journal des caisses.
- **DepouilleController**: Gestion du cycle de vie des corps.
- **DocumentController**: Gestion des documents PDF via Supabase Storage.
- **EmplacementController**: Attribution et libération des casiers.
- **FactureController**: Facturation et paiements.
- **FamilleController**: Gestion des contacts familiaux.
- **RapportController**: Statistiques et rapports d'activité.
- **RestitutionController**: Workflow de sortie des corps.
- **UtilisateurController**: Administration des comptes utilisateurs.

## Standards

- Les réponses utilisent systématiquement des DTOs.
- Codes HTTP standards : 200 (OK), 201 (Created), 204 (No Content), 400 (Bad Request), 404 (Not Found).
