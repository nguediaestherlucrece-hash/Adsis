| Type         | Quand l’utiliser                             | Exemple                                                           |
| ------------ | -------------------------------------------- | ----------------------------------------------------------------- |
| **feat**     | Nouvelle fonctionnalité                      | `feat(presence): ajouter l’appel par QR Code`                     |
| **fix**      | Correction de bug                            | `fix(api): corriger erreur 500 lors de l’appel`                   |
| **docs**     | Documentation uniquement                     | `docs(readme): ajouter procédure d’installation`                  |
| **style**    | Formatage, indentation, pas de logique       | `style(front): harmoniser les classes Bootstrap`                  |
| **refactor** | Refactorisation sans changer le comportement | `refactor(service): simplifier la logique de calcul des absences` |
| **perf**     | Amélioration de performance                  | `perf(sync): optimiser la synchronisation offline`                |
| **test**     | Ajout ou modification de tests               | `test(user): ajouter tests unitaires service utilisateur`         |
| **build**    | Build, dépendances, Maven, npm               | `build: ajouter dépendance spring-security`                       |
| **ci**       | CI/CD (GitHub Actions, GitLab CI)            | `ci: ajouter pipeline de build automatique`                       |
| **chore**    | Tâches techniques diverses                   | `chore: nettoyer les fichiers inutilisés`                         |
| **revert**   | Annulation d’un commit                       | `revert: annuler feat(presence): appel par QR Code`               |


🎯 Objectif

À partir d’un fichier : appel_classe_db.sql


➡️ recréer la base, les tables (ex : utilisateur) et les données sur une autre machine.


1️⃣ Ouvrir MySQL Workbench

Lance MySQL Workbench

Clique sur :

Local instance MySQL 3306


Entre le mot de passe root

2️⃣ Ouvrir l’outil d’importation

Dans le menu en haut :

Server → Data Import

3️⃣ Sélectionner le fichier SQL

Dans la fenêtre Data Import :

🔹 Étape A — Source d’import

Coche : Import from Self-Contained File


Clique sur Browse

Sélectionne le fichier : appel_classe_db.sql

🔹 Étape B — Destination

Coche :

Import to Default Target Schema


💡 Pas besoin de créer la base à la main si ton fichier contient CREATE DATABASE.

4️⃣ Lancer l’importation

Clique sur :

Start Import


➡️ Attends le message :

Import completed successfully

5️⃣ Vérifier le résultat

Dans le panneau Schemas (à gauche) :

Clique droit → Refresh All

Tu dois voir :

appel_classe_db
  └── Tables
      └── utilisateur


Clique droit sur utilisateur →
Select Rows – Limit 1000
➡️ tu verras les données importées ✅