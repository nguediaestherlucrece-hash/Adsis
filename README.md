#  API - Gestion des Appels en Classe

Projet Spring Boot pour la gestion des utilisateurs, des enseignants, des étudiants, des cours et des absences.

---

##  1. Documentation Principale

Pour garder ce projet propre et organisé, la documentation technique est divisée en **2 guides essentiels** :

###  [Guide d'Installation & Base de Données (Démarrer ici)](./DB_SETUP_GUIDE.md)
> *Comment configurer MySQL, créer votre propre fichier de configuration sécurisé et lancer le serveur sans erreur.*

###  [Architecture Technique & API](./API_ARCHITECTURE.md)
> *Comprendre la structure du code (Controller/Service/Repository), le système de sécurité RBAC (Rôles & Permissions) et la gestion des erreurs.*

---

##  2. Démarrage Rapide Backend

Si vous avez déjà tout configuré :

| Action | Commande (Windows) | Commande (Linux/Mac) |
|---|---|---|
| **Build & Run** | `.\mvnw.cmd spring-boot:run` | `./mvnw spring-boot:run` |
| **Accès API** | [http://localhost:8081/api](http://localhost:8081/api) | - |
| **Documentation** | [Swagger UI (si activé)](http://localhost:8081/swagger-ui.html) | - |

---

##  3. Démarrage Rapide Frontend
    Après avoir lancé le backend, vous pouvez ouvrir le fichier ../admin/tableaudebord.html dans votre navigateur.
    via npx vite ou bien l'extension go live de vs code 
