# 🧪 EXEMPLES DE REQUÊTES - Tests API Rapides

## 📋 TABLE DES MATIÈRES
1. [Étudiants](#-étudiants)
2. [Enseignants](#-enseignants)
3. [Surveillants](#-surveillants)
4. [Assistants Pédagogiques](#-assistants-pédagogiques)
5. [Permissions & Rôles](#-permissions--rôles)

---

## 🎓 ÉTUDIANTS

### Créer un Étudiant Complet

**POST** `/api/etudiants`

```json
{
  "nom": "Dupont",
  "prenom": "Jean",
  "email": "jean.dupont@ecole.fr",
  "password": "Password123!",
  "statut": "ACTIF",
  "roleNames": ["ETUDIANT"],
  "matricule": "ETU2024001",
  "dateNaissance": "2000-05-15",
  "telephone": "+33612345678",
  "classeId": 1
}
```

### Créer un Étudiant (Minimal)

```json
{
  "nom": "Martin",
  "prenom": "Sophie",
  "email": "sophie.martin@ecole.fr",
  "password": "Password456!",
  "matricule": "ETU2024002",
  "dateNaissance": "2001-08-22"
}
```

### Mettre à Jour un Étudiant

**PUT** `/api/etudiants/1`

```json
{
  "nom": "Dupont-Legrand",
  "telephone": "+33611223344",
  "classeId": 2
}
```

### Changer de Classe

**PATCH** `/api/etudiants/1/classe/3`

(Pas de body nécessaire)

---

## 👨‍🏫 ENSEIGNANTS

### Créer un Enseignant

**POST** `/api/enseignants`

```json
{
  "nom": "Leroy",
  "prenom": "Marie",
  "email": "marie.leroy@ecole.fr",
  "password": "Prof2024!",
  "statut": "ACTIF",
  "roleNames": ["ENSEIGNANT"],
  "specialite": "Informatique",
  "grade": "Professeur",
  "coursIds": [1, 2]
}
```

### Créer un Enseignant (Sans Cours)

```json
{
  "nom": "Bernard",
  "prenom": "Paul",
  "email": "paul.bernard@ecole.fr",
  "password": "Prof2025!",
  "specialite": "Mathématiques",
  "grade": "Maître de conférences"
}
```

### Mettre à Jour un Enseignant

**PUT** `/api/enseignants/1`

```json
{
  "grade": "Professeur titulaire",
  "coursIds": [1, 2, 3, 4]
}
```

### Assigner des Cours

**PATCH** `/api/enseignants/1/cours`

```json
[1, 2, 3]
```

---

## 👮 SURVEILLANTS

### Créer un Surveillant

**POST** `/api/surveillants`

```json
{
  "nom": "Dubois",
  "prenom": "Pierre",
  "email": "pierre.dubois@ecole.fr",
  "password": "Surv2024!",
  "statut": "ACTIF",
  "roleNames": ["SURVEILLANT"],
  "affectation": "Bâtiment A"
}
```

### Créer un Surveillant (Minimal)

```json
{
  "nom": "Petit",
  "prenom": "Lucie",
  "email": "lucie.petit@ecole.fr",
  "password": "Surv2025!",
  "affectation": "Département Informatique"
}
```

### Mettre à Jour un Surveillant

**PUT** `/api/surveillants/1`

```json
{
  "affectation": "Bâtiment B",
  "statut": "ACTIF"
}
```

---

## 📚 ASSISTANTS PÉDAGOGIQUES

### Créer un Assistant Pédagogique

**POST** `/api/assistants-pedagogiques`

```json
{
  "nom": "Roux",
  "prenom": "Claire",
  "email": "claire.roux@ecole.fr",
  "password": "Asst2024!",
  "statut": "ACTIF",
  "roleNames": ["ASSISTANT_PEDAGOGIQUE"],
  "departement": "Département Informatique"
}
```

### Créer un Assistant (Minimal)

```json
{
  "nom": "Blanc",
  "prenom": "Thomas",
  "email": "thomas.blanc@ecole.fr",
  "password": "Asst2025!",
  "departement": "Département Sciences"
}
```

### Mettre à Jour un Assistant

**PUT** `/api/assistants-pedagogiques/1`

```json
{
  "departement": "Département Génie Civil",
  "statut": "ACTIF"
}
```

---

## 🔐 PERMISSIONS & RÔLES

### Créer les Permissions

**POST** `/api/permissions`

```json
// Étudiant
{"name": "ETUDIANT_READ", "description": "Lire les étudiants"}
{"name": "ETUDIANT_CREATE", "description": "Créer des étudiants"}
{"name": "ETUDIANT_UPDATE", "description": "Modifier des étudiants"}
{"name": "ETUDIANT_DELETE", "description": "Supprimer des étudiants"}

// Enseignant
{"name": "ENSEIGNANT_READ", "description": "Lire les enseignants"}
{"name": "ENSEIGNANT_CREATE", "description": "Créer des enseignants"}
{"name": "ENSEIGNANT_UPDATE", "description": "Modifier des enseignants"}
{"name": "ENSEIGNANT_DELETE", "description": "Supprimer des enseignants"}

// Surveillant
{"name": "SURVEILLANT_READ", "description": "Lire les surveillants"}
{"name": "SURVEILLANT_CREATE", "description": "Créer des surveillants"}
{"name": "SURVEILLANT_UPDATE", "description": "Modifier des surveillants"}
{"name": "SURVEILLANT_DELETE", "description": "Supprimer des surveillants"}

// Assistant
{"name": "ASSISTANT_READ", "description": "Lire les assistants pédagogiques"}
{"name": "ASSISTANT_CREATE", "description": "Créer des assistants pédagogiques"}
{"name": "ASSISTANT_UPDATE", "description": "Modifier des assistants pédagogiques"}
{"name": "ASSISTANT_DELETE", "description": "Supprimer des assistants pédagogiques"}
```

### Créer le Rôle ETUDIANT

**POST** `/api/roles`

```json
{
  "name": "ETUDIANT",
  "description": "Rôle standard pour les étudiants",
  "permissionNames": [
    "ETUDIANT_READ"
  ]
}
```

### Créer le Rôle ENSEIGNANT

```json
{
  "name": "ENSEIGNANT",
  "description": "Rôle pour les enseignants",
  "permissionNames": [
    "ENSEIGNANT_READ",
    "ETUDIANT_READ"
  ]
}
```

### Créer le Rôle SURVEILLANT

```json
{
  "name": "SURVEILLANT",
  "description": "Rôle pour les surveillants",
  "permissionNames": [
    "SURVEILLANT_READ",
    "ETUDIANT_READ"
  ]
}
```

### Créer le Rôle ASSISTANT_PEDAGOGIQUE

```json
{
  "name": "ASSISTANT_PEDAGOGIQUE",
  "description": "Rôle pour les assistants pédagogiques",
  "permissionNames": [
    "ASSISTANT_READ",
    "ETUDIANT_READ",
    "ENSEIGNANT_READ",
    "ENSEIGNANT_UPDATE"
  ]
}
```

### Créer le Rôle ADMIN

```json
{
  "name": "ADMIN",
  "description": "Administrateur système complet",
  "permissionNames": [
    "ETUDIANT_READ",
    "ETUDIANT_CREATE",
    "ETUDIANT_UPDATE",
    "ETUDIANT_DELETE",
    "ENSEIGNANT_READ",
    "ENSEIGNANT_CREATE",
    "ENSEIGNANT_UPDATE",
    "ENSEIGNANT_DELETE",
    "SURVEILLANT_READ",
    "SURVEILLANT_CREATE",
    "SURVEILLANT_UPDATE",
    "SURVEILLANT_DELETE",
    "ASSISTANT_READ",
    "ASSISTANT_CREATE",
    "ASSISTANT_UPDATE",
    "ASSISTANT_DELETE"
  ]
}
```

---

## 🔄 SCÉNARIOS COMPLETS

### Scénario 1 : Onboarding d'un Nouvel Étudiant

```bash
# 1. Créer la classe
POST /api/classes
{
  "nomClasse": "ING3-ISI-FR",
  "niveau": "L3"
}
# Réponse: {"id": 1, ...}

# 2. Créer l'étudiant avec la classe
POST /api/etudiants
{
  "nom": "Nouveau",
  "prenom": "Etudiant",
  "email": "etudiant@ecole.fr",
  "password": "Welcome2024!",
  "matricule": "ETU2024050",
  "dateNaissance": "2002-03-10",
  "telephone": "+33687654321",
  "classeId": 1
}

# 3. Vérifier l'étudiant créé
GET /api/etudiants/1
```

### Scénario 2 : Attribution de Cours à un Enseignant

```bash
# 1. Créer l'enseignant
POST /api/enseignants
{
  "nom": "Professeur",
  "prenom": "Expert",
  "email": "expert@ecole.fr",
  "password": "Prof2024!",
  "specialite": "Algorithmique",
  "grade": "Professeur"
}
# Réponse: {"id": 1, ...}

# 2. Assigner des cours
PATCH /api/enseignants/1/cours
[1, 2, 3]

# 3. Vérifier l'enseignant
GET /api/enseignants/1
# Réponse inclut: "coursEnseignes": [...]
```

### Scénario 3 : Changement de Classe d'un Étudiant

```bash
# 1. Vérifier la classe actuelle
GET /api/etudiants/1
# Réponse: {"classe": {"id": 1, "nomClasse": "ING3-ISI-FR"}}

# 2. Changer de classe
PATCH /api/etudiants/1/classe/2

# 3. Vérifier le changement
GET /api/etudiants/1
# Réponse: {"classe": {"id": 2, "nomClasse": "ING3-ISI-EN"}}
```

---

## 🧪 COMMANDES CURL COMPLÈTES

### Créer un Étudiant (avec Authentication)

```bash
curl -X POST http://localhost:8080/api/etudiants \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "nom": "Test",
    "prenom": "Etudiant",
    "email": "test@ecole.fr",
    "password": "Test2024!",
    "matricule": "ETU2024999",
    "dateNaissance": "2000-01-01",
    "telephone": "+33600000000",
    "classeId": 1
  }'
```

### Récupérer un Étudiant

```bash
curl -X GET http://localhost:8080/api/etudiants/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

### Mettre à Jour un Étudiant

```bash
curl -X PUT http://localhost:8080/api/etudiants/1 \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE" \
  -d '{
    "telephone": "+33611111111"
  }'
```

### Supprimer un Étudiant

```bash
curl -X DELETE http://localhost:8080/api/etudiants/1 \
  -H "Authorization: Bearer YOUR_JWT_TOKEN_HERE"
```

---

## 📝 NOTES IMPORTANTES

### Statuts Disponibles
- `ACTIF`
- `INACTIF`

### Rôles à Créer en Premier
1. ETUDIANT
2. ENSEIGNANT
3. SURVEILLANT
4. ASSISTANT_PEDAGOGIQUE
5. ADMIN

### Ordre de Création Recommandé
1. Créer les permissions
2. Créer les rôles
3. Créer les classes (si nécessaire)
4. Créer les cours (si nécessaire)
5. Créer les utilisateurs

### Champs Obligatoires Minimum
**Pour tous les types** :
- `nom`
- `prenom`
- `email`
- `password`

**Spécifiques** :
- Étudiant : `matricule`
- Enseignant : `specialite`, `grade`
- Surveillant : `affectation`
- Assistant : `departement`

---

## ✅ CHECKLIST DE TEST

### ✅ Étudiants
- [ ] Créer un étudiant complet
- [ ] Créer un étudiant minimal
- [ ] Modifier un étudiant
- [ ] Changer de classe
- [ ] Supprimer un étudiant
- [ ] Rechercher par matricule
- [ ] Rechercher par email
- [ ] Lister par classe

### ✅ Enseignants
- [ ] Créer un enseignant complet
- [ ] Créer un enseignant sans cours
- [ ] Modifier un enseignant
- [ ] Assigner des cours
- [ ] Supprimer un enseignant
- [ ] Rechercher par spécialité
- [ ] Rechercher par grade

### ✅ Surveillants
- [ ] Créer un surveillant
- [ ] Modifier un surveillant
- [ ] Supprimer un surveillant
- [ ] Rechercher par affectation

### ✅ Assistants Pédagogiques
- [ ] Créer un assistant
- [ ] Modifier un assistant
- [ ] Supprimer un assistant
- [ ] Rechercher par département

---

## 🎯 RÉSUMÉ

Ce document contient :
- ✅ Exemples JSON pour TOUS les types d'utilisateurs
- ✅ Endpoints complets avec méthodes HTTP
- ✅ Exemples de permissions et rôles
- ✅ Scénarios complets de test
- ✅ Commandes CURL prêtes à l'emploi
- ✅ Checklist de tests

**Prêt à tester l'API !** 🚀
