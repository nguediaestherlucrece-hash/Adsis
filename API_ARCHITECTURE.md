# 🏗️ Architecture de l'API Appel Classe

Ce document décrit l'organisation technique du projet Spring Boot.

## 1. Structure en Couches (Layered Architecture)

L'application suit une architecture classique en N-tiers :

1.  **Controller (`com.ISI.Appel_classe.controller`)** :
    -   Point d'entrée des requêtes HTTP (REST).
    -   Valide les entrées (`@Valid`).
    -   Délègue le traitement aux Services.
    -   Retourne des DTOs (Data Transfer Objects).

2.  **Service (`com.ISI.Appel_classe.service`)** :
    -   Contient la logique métier (Business Logic).
    -   Manipule les Entités via les Repositories.
    -   Gère les transactions (`@Transactional`).
    -   Transforme les Entités en DTOs.

3.  **Repository (`com.ISI.Appel_classe.repository`)** :
    -   Interface avec la base de données (Spring Data JPA).
    -   Exécute les requêtes SQL/HQL.

4.  **Entity (`com.ISI.Appel_classe.entity`)** :
    -   Représente les tables de la base de données (JPA/Hibernate).
    -   Contient les relations (ManyToOne, OneToMany, etc.).

---

## 2. Gestion des Données (DTO vs Entity)

Pour découpler l'API de la base de données, nous utilisons le pattern DTO :

-   **CreateDTO / UpdateDTO** : Objets reçus en entrée (Request Body). Ils ne contiennent que les champs nécessaires à la création/modification.
-   **ResponseDTO** : Objets renvoyés au client. Ils peuvent contenir des champs calculés ou masquer des données sensibles (ex: mot de passe).

*Exemple : `UtilisateurCreateDTO` -> `Utilisateur` (Entité) -> `UtilisateurDTO` (Réponse).*

---

## 3. Sécurité et RBAC (Role-Based Access Control)

La sécurité est gérée par **Spring Security**.

-   **Utilisateur** : L'entité principale stockant les identifiants.
-   **Rôle** : Groupe de permissions (ex: `ADMIN`, `ENSEIGNANT`). Un utilisateur peut avoir plusieurs rôles.
-   **Permission** : Action atomique (ex: `COURSE_READ`, `USER_CREATE`).

Chaque requête est interceptée pour vérifier si l'utilisateur possède les rôles/permissions requis via les annotations `@PreAuthorize`.

---

## 4. Gestion des Erreurs

Une gestion centralisée des exceptions est implémentée dans `advisor.ControllerAdvisor`.

-   Capture les exceptions métiers (ex: `ResourceNotFoundException`).
-   Retourne une réponse JSON standardisée avec le code HTTP approprié (404, 400, 500).

---

## 5. Configuration CORS

Pour permettre le développement frontend local, une configuration CORS globale (`CorsConfig.java`) autorise toutes les origines (`*`) sur tous les endpoints.
