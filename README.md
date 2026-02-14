# 📚 API - Gestion des Appels en Classe

Projet Spring Boot pour la gestion des utilisateurs, des enseignants, des étudiants, des cours et des absences.

---

## 🚀 1. Documentation Principale

Pour garder ce projet propre et organisé, la documentation technique est divisée en **2 guides essentiels** :

### 📘 [Guide d'Installation & Base de Données (Démarrer ici)](./DB_SETUP_GUIDE.md)
> *Comment configurer MySQL, créer votre propre fichier de configuration sécurisé et lancer le serveur sans erreur.*

### 🏗️ [Architecture Technique & API](./API_ARCHITECTURE.md)
> *Comprendre la structure du code (Controller/Service/Repository), le système de sécurité RBAC (Rôles & Permissions) et la gestion des erreurs.*

---

## ✅ 2. Démarrage Rapide

Si vous avez déjà tout configuré :

| Action | Commande (Windows) | Commande (Linux/Mac) |
|---|---|---|
| **Build & Run** | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| **Accès API** | [http://localhost:8081/api](http://localhost:8081/api) | - |
| **Documentation** | [Swagger UI (si activé)](http://localhost:8081/swagger-ui.html) | - |

---

## 🧪 3. Tests Intégrés

Des fichiers HTML interactifs sont inclus à la racine pour tester l'API sans outil externe :

- **[`test-api.html`](./test-api.html)** : Tests CRUD de base (Utilisateurs / Cours / Etudiants).
- **[`test-rbac.html`](./test-rbac.html)** : Tests spécifiques aux Rôles et Permissions.

Ouvrez simplement ces fichiers dans votre navigateur après avoir lancé le serveur !
