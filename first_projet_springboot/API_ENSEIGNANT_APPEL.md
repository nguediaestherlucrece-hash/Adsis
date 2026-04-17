# API Backend - Gestion des Appels pour Enseignants

## Vue d'ensemble

Cette documentation décrit les endpoints REST pour la gestion des appels et des séances pour les enseignants.

## Endpoints Principaux

### 1. Gestion des Appels

#### Démarrer un appel
```http
POST /api/appels
Content-Type: application/json
Authorization: Bearer {token}

{
  "seanceId": 1,
  "modeAppel": "QR",  // "QR", "PIN", ou "MANUEL"
  "dureeValiditePIN": 5  // En minutes (optionnel, par défaut 5)
}
```

**Réponse:**
```json
{
  "id": 1,
  "idAppel": "uuid-string",
  "dateHeureDebut": "2026-04-17T10:00:00",
  "dateHeureFin": null,
  "statut": "ouvert",
  "seanceId": 1,
  "qrCodeBase64": "base64-encoded-qr-code",
  "codePIN": "123456",
  "expirationPIN": "2026-04-17T10:05:00"
}
```

#### Clôturer un appel
```http
PUT /api/appels/{id}/cloturer
Authorization: Bearer {token}
```

**Réponse:**
```json
{
  "id": 1,
  "idAppel": "uuid-string",
  "dateHeureDebut": "2026-04-17T10:00:00",
  "dateHeureFin": "2026-04-17T11:00:00",
  "statut": "clôturé",
  "seanceId": 1
}
```

#### Enregistrer une présence
```http
POST /api/appels/{id}/presences
Content-Type: application/json
Authorization: Bearer {token}

{
  "etudiantId": 1,
  "statut": "présent",  // "présent", "absent", "retard"
  "modeEnregistrement": "QR",  // "QR", "PIN", "manuel"
  "localisation": "48.8566,2.3522"  // Optionnel
}
```

#### Enregistrer plusieurs présences (batch)
```http
POST /api/appels/presences/batch
Content-Type: application/json
Authorization: Bearer {token}

{
  "appelId": 1,
  "presences": [
    {
      "etudiantId": 1,
      "statut": "présent",
      "modeEnregistrement": "manuel"
    },
    {
      "etudiantId": 2,
      "statut": "retard",
      "modeEnregistrement": "manuel"
    }
  ]
}
```

#### Récupérer un appel
```http
GET /api/appels/{id}
Authorization: Bearer {token}
```

#### Récupérer l'appel ouvert d'une séance
```http
GET /api/appels/seance/{seanceId}/ouvert
Authorization: Bearer {token}
```

#### Récupérer les étudiants d'une séance avec leur statut
```http
GET /api/appels/seance/{seanceId}/etudiants?appelId={appelId}
Authorization: Bearer {token}
```

**Réponse:**
```json
[
  {
    "id": 1,
    "matricule": "20ISI001",
    "nom": "NGOUPEYOU",
    "prenom": "Landry",
    "email": "landry@example.com",
    "statut": "présent",
    "presenceId": 1
  },
  {
    "id": 2,
    "matricule": "20ISI002",
    "nom": "MBIDA",
    "prenom": "Yohan",
    "email": "yohan@example.com",
    "statut": "non_enregistré",
    "presenceId": null
  }
]
```

#### Valider un code PIN
```http
POST /api/appels/{id}/valider-pin
Content-Type: application/json
Authorization: Bearer {token}

{
  "codePIN": "123456"
}
```

**Réponse:**
```json
{
  "valide": true
}
```

#### Régénérer un QR code
```http
POST /api/appels/{id}/regenerer-qr
Authorization: Bearer {token}
```

### 2. Gestion des Séances Enseignant

#### Récupérer les séances d'aujourd'hui
```http
GET /api/seances/enseignant/{enseignantId}
Authorization: Bearer {token}
```

**Réponse:**
```json
[
  {
    "id": 1,
    "idSeance": "uuid-string",
    "date": "2026-04-17",
    "heureDebut": "10:00:00",
    "heureFin": "12:00:00",
    "statut": true,
    "couleur": "#0f22f1",
    "coursId": 1,
    "coursCode": "ISI301",
    "coursIntitule": "Technologie et Innovation",
    "classeId": 1,
    "classeNom": "INGE 3 ISI",
    "appelId": 1,
    "appelStatut": "ouvert",
    "nombrePresents": 25,
    "nombreAbsents": 3,
    "nombreRetards": 2,
    "nombreTotal": 30
  }
]
```

#### Récupérer les séances pour une date spécifique
```http
GET /api/seances/enseignant/{enseignantId}/date/{date}
Authorization: Bearer {token}
```

#### Récupérer les séances de la semaine
```http
GET /api/seances/enseignant/{enseignantId}/semaine
Authorization: Bearer {token}
```

#### Récupérer les séances pour une période
```http
GET /api/seances/enseignant/{enseignantId}/periode?dateDebut=2026-04-14&dateFin=2026-04-20
Authorization: Bearer {token}
```

## Codes d'erreur

- `400 Bad Request`: Requête invalide (validation échouée)
- `401 Unauthorized`: Token d'authentification manquant ou invalide
- `403 Forbidden`: Accès refusé (permissions insuffisantes)
- `404 Not Found`: Ressource non trouvée
- `500 Internal Server Error`: Erreur serveur

## Authentification

Tous les endpoints nécessitent un token JWT valide dans le header Authorization:
```
Authorization: Bearer {your-jwt-token}
```

## Permissions

- **ENSEIGNANT**: Accès complet aux endpoints de gestion des appels et séances
- **SURVEILLANT**: Accès aux endpoints de gestion des appels
- **ASSISTANT_PEDAGOGIQUE**: Accès complet à tous les endpoints
- **ADMIN**: Accès complet à tous les endpoints

## Format QR Code

Le QR code généré contient les informations suivantes:
```
APPEL:{appelId}:{timestamp}
```

Exemple: `APPEL:1:1713348000000`

## Génération du Code PIN

- Code PIN de 6 chiffres
- Durée de validité configurable (par défaut 5 minutes)
- Stocké en cache temporaire (en production, utiliser Redis)

## Workflow Typique

1. **Enseignant arrive en cours**
   - GET `/api/seances/enseignant/{id}` pour voir ses séances du jour

2. **Démarrer l'appel**
   - POST `/api/appels` avec le mode choisi (QR ou PIN)
   - Le système génère le QR code ou le code PIN

3. **Enregistrer les présences**
   - Mode QR: Les étudiants scannent le QR code
   - Mode PIN: Les étudiants saisissent le code PIN
   - Mode Manuel: L'enseignant marque manuellement via POST `/api/appels/{id}/presences`

4. **Consulter la liste**
   - GET `/api/appels/seance/{seanceId}/etudiants` pour voir qui est présent

5. **Clôturer l'appel**
   - PUT `/api/appels/{id}/cloturer`
   - Les étudiants non enregistrés sont automatiquement marqués absents

## Notes Importantes

- Un seul appel ouvert par séance à la fois
- Les présences peuvent être modifiées tant que l'appel est ouvert
- La clôture de l'appel marque automatiquement les absents
- Le QR code peut être régénéré à tout moment
- Le code PIN expire après la durée configurée
