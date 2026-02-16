# 🛠️ Guide de Configuration Rapide (Base de Données)

Ce guide s'adresse aux développeurs qui lancent le projet pour la première fois.

## 1. Prérequis

-   **MySQL 8.0+**
-   **Maven** (inclus dans le wrapper `mvnw`)
-   **Java 17+**

---

## 2. Configuration Standard (Par Défaut)

Le projet utilise les paramètres standard pour l'environnement de développement :

-   **Base de Données** : `appel_classe_db` (créée automatiquement si elle n'existe pas)
-   **Utilisateur** : `root`
-   **Mot de passe** : `mot2passe@`
-   **Port MySQL** : `3306`

*Note : Ces paramètres sont situés dans `src/main/resources/application.properties`.*

Si cette configuration correspond à votre installation locale, **VOUS N'AVEZ RIEN À FAIRE**.

---

## 3. Configuration Avancée (Mot de Passe Personnalisé)

### SI votre mot de passe n'est pas "root" :

⚠️ **NE MODIFIEZ PAS** le fichier `src/main/resources/application.properties` directement.
(Cela éviterait les conflits Git et le partage de mots de passe sensibles).

**Solution recommandée :**
1.  Créez un dossier `config` à la racine du projet (au même niveau que ce fichier).
2.  Dans ce dossier, créez un fichier `application.properties`.
3.  Ajoutez vos identifiants spécifiques :

**Fichier : `config/application.properties`**
```properties
# Surchargez seulement ce qui change
spring.datasource.password=MON_MOT_DE_PASSE_SECRET
spring.datasource.username=mon_user_specifique
```

Ce fichier est automatiquement chargé par Spring Boot et ignoré par `.gitignore`.

---

## 4. Démarrage du Serveur

Pour lancer l'application (et la DB) :

```bash
# Windows
.\mvnw.cmd spring-boot:run

# Linux / Mac
./mvnw spring-boot:run
```

Le serveur démarrera sur le port **8081**.
L'URL de l'API sera : `http://localhost:8081/api`

---

## 5. Dépannage Courant

### Erreur : `Connection refused` (MySQL)
- Vérifiez que MySQL tourne bien (Service Windows ou Docker).
- Vérifiez que le port est 3306.

### Erreur : `Access denied for user 'root'`
- Vérifiez votre mot de passe dans `config/application.properties`.
- Si vous utilisez Docker, vérifiez que le conteneur a bien démarré.

### Erreur : `Port 8081 already in use`
- Une autre instance de l'application tourne déjà.
- Arrêtez-la via le Gestionnaire des tâches ou `CTRL+C` dans le terminal.
