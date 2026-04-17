# 🎓 Système de Gestion des Appels - Backend Enseignant

## 📋 Vue d'ensemble

Implémentation complète du backend pour la gestion des appels et des présences pour les enseignants, incluant la génération de QR codes et de codes PIN.

## ✅ Fonctionnalités Implémentées

### 🔐 Authentification & Sécurité
- JWT pour l'authentification
- RBAC (Role-Based Access Control)
- Permissions par rôle (ENSEIGNANT, SURVEILLANT, ASSISTANT_PEDAGOGIQUE, ADMIN)

### 📱 Gestion des Appels
- Démarrage d'appel avec génération automatique de QR code ou code PIN
- Clôture d'appel avec marquage automatique des absents
- Enregistrement de présences (unique ou batch)
- Validation de code PIN avec expiration
- Régénération de QR code

### 📊 Gestion des Séances
- Récupération des séances par enseignant
- Filtrage par date, semaine ou période
- Statistiques de présence en temps réel
- Liste des étudiants avec leur statut

### 🎯 Modes d'Appel
1. **QR Code** - Scan par les étudiants
2. **Code PIN** - Saisie d'un code à 6 chiffres
3. **Manuel** - Marquage par l'enseignant

## 🏗️ Architecture

```
Backend (Spring Boot)
├── Controllers (REST API)
│   ├── AppelController
│   └── SeanceEnseignantController
├── Services
│   ├── AppelService
│   ├── SeanceEnseignantService
│   └── QRCodeService
├── Repositories
│   ├── AppelRepository
│   ├── PresenceRepository
│   ├── SeanceRepository
│   └── EtudiantRepository
├── DTOs
│   ├── Request (DemarrerAppelRequest, EnregistrerPresenceRequest, etc.)
│   └── Response (AppelDTO, PresenceDTO, SeanceEnseignantDTO, etc.)
└── Models (Entités JPA)
    ├── Appel
    ├── Presence
    ├── Seance
    └── Etudiant
```

## 📚 Documentation

### Fichiers de Documentation
- **API_ENSEIGNANT_APPEL.md** - Documentation complète de l'API REST
- **IMPLEMENTATION_SUMMARY.md** - Résumé détaillé de l'implémentation
- **NEXT_STEPS.md** - Guide pour l'implémentation frontend
- **FRONTEND_CODE_EXAMPLES.md** - Exemples de code frontend

## 🚀 Démarrage Rapide

### Prérequis
- Java 17+
- Maven 3.6+
- MySQL 8.0+

### Installation

1. **Cloner le projet**
```bash
cd first_projet_springboot
```

2. **Configurer la base de données**
```properties
# application.properties
spring.datasource.url=jdbc:mysql://localhost:3306/appel_classe_db
spring.datasource.username=your_username
spring.datasource.password=your_password
```

3. **Installer les dépendances**
```bash
mvn clean install
```

4. **Lancer l'application**
```bash
mvn spring-boot:run
```

L'API sera accessible sur `http://localhost:8080`

## 📡 Endpoints Principaux

### Appels
```
POST   /api/appels                              - Démarrer un appel
PUT    /api/appels/{id}/cloturer                - Clôturer un appel
POST   /api/appels/{id}/presences               - Enregistrer une présence
POST   /api/appels/presences/batch              - Enregistrer plusieurs présences
GET    /api/appels/{id}                         - Récupérer un appel
GET    /api/appels/seance/{seanceId}/ouvert     - Appel ouvert d'une séance
GET    /api/appels/seance/{seanceId}/etudiants  - Étudiants avec statut
POST   /api/appels/{id}/valider-pin             - Valider un code PIN
POST   /api/appels/{id}/regenerer-qr            - Régénérer un QR code
```

### Séances
```
GET    /api/seances/enseignant/{id}                    - Séances d'aujourd'hui
GET    /api/seances/enseignant/{id}/date/{date}        - Séances pour une date
GET    /api/seances/enseignant/{id}/semaine            - Séances de la semaine
GET    /api/seances/enseignant/{id}/periode            - Séances pour une période
```

## 🧪 Tests

### Tester avec cURL

**Démarrer un appel**
```bash
curl -X POST http://localhost:8080/api/appels \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "seanceId": 1,
    "modeAppel": "QR"
  }'
```

**Enregistrer une présence**
```bash
curl -X POST http://localhost:8080/api/appels/1/presences \
  -H "Authorization: Bearer YOUR_TOKEN" \
  -H "Content-Type: application/json" \
  -d '{
    "etudiantId": 1,
    "statut": "présent",
    "modeEnregistrement": "manuel"
  }'
```

**Clôturer un appel**
```bash
curl -X PUT http://localhost:8080/api/appels/1/cloturer \
  -H "Authorization: Bearer YOUR_TOKEN"
```

## 🔧 Technologies Utilisées

- **Spring Boot 3.3.0** - Framework backend
- **Spring Security** - Authentification et autorisation
- **Spring Data JPA** - Accès aux données
- **MySQL** - Base de données
- **JWT** - Tokens d'authentification
- **ZXing** - Génération de QR codes
- **Lombok** - Réduction du code boilerplate
- **Maven** - Gestion des dépendances

## 📦 Dépendances Ajoutées

```xml
<!-- QR Code Generation -->
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>core</artifactId>
    <version>3.5.2</version>
</dependency>
<dependency>
    <groupId>com.google.zxing</groupId>
    <artifactId>javase</artifactId>
    <version>3.5.2</version>
</dependency>
```

## 🎨 Frontend (À Implémenter)

Le backend est prêt pour l'intégration frontend. Consultez:
- **NEXT_STEPS.md** pour le guide d'implémentation
- **FRONTEND_CODE_EXAMPLES.md** pour des exemples de code

### Technologies Recommandées
- Vite (Build tool)
- Bootstrap 5 (UI Framework)
- html5-qrcode (Scanner QR)
- IndexedDB (Stockage offline)
- Service Worker (PWA)

## 🔐 Sécurité

### Authentification
Tous les endpoints nécessitent un token JWT:
```
Authorization: Bearer {your-jwt-token}
```

### Permissions
- **ENSEIGNANT**: Gestion complète des appels et séances
- **SURVEILLANT**: Gestion des appels uniquement
- **ASSISTANT_PEDAGOGIQUE**: Accès complet
- **ADMIN**: Accès complet

## 📊 Format des Données

### QR Code
```
APPEL:{appelId}:{timestamp}
Exemple: APPEL:1:1713348000000
```

### Code PIN
- 6 chiffres aléatoires
- Durée de validité: 5 minutes (configurable)
- Exemple: 123456

## ⚠️ Notes Importantes

### Cache PIN
- **Actuel**: HashMap en mémoire
- **Production**: Utiliser Redis pour:
  - Cache distribué
  - Expiration automatique
  - Haute disponibilité

### Gestion des Absences
- À la clôture de l'appel, tous les étudiants non enregistrés sont automatiquement marqués absents
- Horodatage de toutes les présences

### Mode Hors Ligne
- Le endpoint batch (`/api/appels/presences/batch`) permet la synchronisation des présences enregistrées offline

## 🐛 Dépannage

### Erreur de connexion à la base de données
Vérifiez les paramètres dans `application.properties`

### Erreur 401 Unauthorized
Vérifiez que le token JWT est valide et non expiré

### Erreur 403 Forbidden
Vérifiez que l'utilisateur a les permissions nécessaires

## 📞 Support

Pour toute question ou problème:
1. Consultez la documentation dans les fichiers MD
2. Vérifiez les logs de l'application
3. Testez les endpoints avec Postman ou cURL

## 🎯 Prochaines Étapes

1. ✅ Backend complet
2. ⏳ Implémentation frontend
3. ⏳ Tests unitaires et d'intégration
4. ⏳ Déploiement en production
5. ⏳ Intégration Redis pour le cache
6. ⏳ Géolocalisation anti-fraude

## 📄 Licence

Ce projet est développé dans le cadre du cours "Technologie et Innovation" à l'Institut Universitaire Saint Jean.

---

**Développé par**: INGE 3 ISI - Groupe 6
- NGOUPEYOU BAKOP Landry (C)
- Yohan Manuel MBIDA
- NKOE ASSENE Jessy Karol
- NGUEDIA Esther Lucrèce
- TSOBGNI Niva Princesse

**Supervisé par**: M. BELOM

**Année académique**: 2025-2026
