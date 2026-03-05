# Service Package

Définit les contrats (interfaces) des services métier.

## Philosophie

La séparation via interfaces permet :
1. Une meilleure testabilité (moquage facile).
2. Un découplage entre l'API (Controller) et l'implémentation technique.
3. Une clarté sur les capacités offertes par chaque module.

## Implémentation

Les implémentations concrètes se trouvent dans le sous-package `serviceImpl`.
