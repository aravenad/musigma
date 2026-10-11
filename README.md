# Musigma

Application de bureau (JavaFX) d'aide à l'organisation de festivals de musique, réalisée en 2024 pour la « Méga SAÉ » du BUT Informatique (IUT2 Grenoble, groupe E1-06).

Un fichier `.mgm` contient un festival : ses dates et scènes, ses artistes et leurs représentations, ses types de tickets et leurs avantages, ses stocks, et le bilan prévisionnel qui en découle.

## Fonctionnalités

| Espace | Cas d'utilisation | Contenu |
|---|---|---|
| Accueil | UC1 Créer festival | Nom, dates, lieu, surface, prix de location, scènes |
| Artistes | UC4.1 Engager artiste | Artistes, genres, cachets, représentations de chaque artiste |
| Planning | UC4.2 Planifier représentation | Un calendrier par scène ; déplacement, durée et suppression vérifiés (pas de chevauchement sur une scène) |
| Stocks | UC3.3 Modéliser stocks | Ressources limitées ou non, consommation par les tickets, manques |
| Tickets | UC3.1 Créer type de ticket | Prix, quantité, surface par personne, avantages puisés dans les stocks |
| Prévision | UC3.2 Simuler recettes | Bilan recettes/charges, vente optimale des tickets (programmation linéaire en nombres entiers) |

Affichage : thème clair ou sombre, zoom (Ctrl + / Ctrl - / Ctrl 0), aide contextuelle sous la barre de menus, infobulles sur le menu de gauche. Le guide d'utilisation s'ouvre avec F1.

## Utilisation

Prérequis : un JDK 17 ou plus récent. Maven est fourni par le wrapper `mvnw`.

```bash
./mvnw javafx:run
```

| Commande | Rôle |
|---|---|
| `./mvnw test` | Tests unitaires (les tests JavaFX sont ignorés sans affichage) |
| `./mvnw test -Pbugs` | Tests des défauts connus, marqués `@Tag("bug")`, qui échouent tant qu'ils ne sont pas corrigés |
| `./mvnw compile spotbugs:check` | Analyse statique SpotBugs |
| `./mvnw pmd:check pmd:cpd-check` | Analyse statique PMD et détection de code dupliqué |
| `./mvnw javadoc:javadoc` | Javadoc dans `target/reports/apidocs` |

Les préférences (fichiers récents, thème, zoom, aide) sont enregistrées dans le profil de l'utilisateur via `java.util.prefs`.
Les logs sont écrits dans le dossier temporaire (`musigma-*.log`) ; passer `com.musigma.level` à `FINE` dans `src/main/resources/logging.properties` pour le détail des modifications du modèle.

## Architecture

Architecture MVC :

- `com.musigma.models` : le modèle. `Festival` est la racine : toute modification qui engage plusieurs objets passe par lui (planification, suppressions en cascade, modification atomique d'un ticket, optimisation, bilan) afin de garder des données cohérentes.
- `com.musigma.controllers` : la fenêtre principale, un contrôleur par espace de travail (`workspaces`) et des champs de saisie validés (`components`).
- `src/main/resources/com/musigma/views` : les vues FXML.

## Écarts avec le dossier de conception

| Dossier de conception | Réalisation | Raison |
|---|---|---|
| `Festival` singleton | Instance ordinaire, une par fenêtre | Testable, et ouvrir un autre festival remplace simplement l'instance |
| `Representation.startDate: LocalDateTime` | `startDelta` en minutes depuis le début du festival | Déplacer le festival déplace ses représentations |
| `Stock.absoluteQuantity: Integer \| null` | `quantity` + `fixed` (limité) + quantité nécessaire calculée | L'écran Stocks a besoin d'une quantité prévue même pour un stock illimité (coût) |
| `TypeTicket.id` | Nom unique dans le festival | Le nom suffit à identifier un type de ticket pour l'utilisateur |
| — | `TypeTicket.surface`, `Festival.end`, `Festival.scenes` | Surface par ticket du modèle mathématique (rapport R2.09) ; dates et scènes de l'UC1 |
| Contrainte de surface à 0,42 m²/personne | Surface propre à chaque type de ticket (0,42 m² par défaut) | Exemple du rapport : VIP 4 m², Non-VIP 2 m² → 205 000 € |

## Convention de commits

Un commit par changement cohérent, sur `master`. Message en anglais à l'impératif, court (≤ 72 caractères) : `Fix ...`, `Add ...`, `Remove ...`. Le corps, séparé par une ligne vide, explique le pourquoi et liste les points importants.

## Équipe

ARAVENA BRAVO Damien (chef de projet), BENDRIS Naël, GIMBERT Bastien, GIBELLO Grégoire, BARTHES Cyril, MAILLET Lucas.
