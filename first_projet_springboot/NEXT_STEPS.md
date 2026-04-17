# Prochaines Étapes - Implémentation Frontend

## 📋 Vue d'ensemble

Le backend est maintenant complet. Voici les étapes pour implémenter le frontend de la partie enseignant.

## 🎨 Frontend à Implémenter

### 1. Page de Login Unifiée

**Fichier**: `/login.html` (à la racine)

**Fonctionnalités**:
- Formulaire de connexion (email/mot de passe)
- Appel à l'API d'authentification
- Redirection selon le rôle:
  - `ENSEIGNANT` → `/enseignant/dashboard.html`
  - `ASSISTANT_PEDAGOGIQUE` → `/assistant/tableaudebordass.html`
  - `ADMIN` → `/admin/tableaudebord.html`
- Stockage du token JWT dans localStorage
- Gestion des erreurs de connexion

**Technologies**:
- HTML5
- Bootstrap 5
- JavaScript vanilla ou Vite

### 2. Dashboard Enseignant

**Fichier**: `/enseignant/dashboard.html`

**Fonctionnalités**:
- Affichage des statistiques rapides:
  - Nombre de cours aujourd'hui
  - Nombre de cours cette semaine
  - Taux de présence moyen
- Bouton principal "Faire l'appel" pour le prochain cours
- Liste des cours du jour avec statut d'appel
- Navigation vers emploi du temps

**API à utiliser**:
```javascript
// Récupérer les séances d'aujourd'hui
GET /api/seances/enseignant/{enseignantId}

// Récupérer les séances de la semaine
GET /api/seances/enseignant/{enseignantId}/semaine
```

### 3. Emploi du Temps

**Fichier**: `/enseignant/emploi.html`

**Fonctionnalités**:
- Vue calendrier ou cartes des séances
- Filtres par jour/semaine
- Affichage des informations:
  - Heure de début/fin
  - Nom du cours
  - Classe
  - Statut de l'appel (ouvert/clôturé/non démarré)
- Clic sur une séance → Ouvre le module d'appel

**API à utiliser**:
```javascript
// Récupérer les séances de la semaine
GET /api/seances/enseignant/{enseignantId}/semaine

// Récupérer les séances pour une période
GET /api/seances/enseignant/{enseignantId}/periode?dateDebut=2026-04-14&dateFin=2026-04-20
```

### 4. Interface d'Appel (Page Principale)

**Fichier**: `/enseignant/appel.html`

**Fonctionnalités**:

#### Onglet 1: Scan QR
- Activation de la caméra avec `html5-qrcode`
- Scan des QR codes des étudiants
- Affichage en temps réel des étudiants scannés
- Validation du QR code via API

**Bibliothèque**:
```html
<script src="https://unpkg.com/html5-qrcode"></script>
```

**Code exemple**:
```javascript
const html5QrCode = new Html5Qrcode("reader");
html5QrCode.start(
  { facingMode: "environment" },
  { fps: 10, qrbox: 250 },
  (decodedText) => {
    // Traiter le QR code scanné
    validerQRCode(decodedText);
  }
);
```

#### Onglet 2: Liste de Classe
- Affichage de tous les étudiants de la classe
- Switches pour chaque étudiant:
  - Présent (vert)
  - Retard (orange)
  - Absent (rouge)
- Mise à jour en temps réel
- Recherche/filtre d'étudiants

**API à utiliser**:
```javascript
// Récupérer la liste des étudiants avec statut
GET /api/appels/seance/{seanceId}/etudiants?appelId={appelId}

// Enregistrer une présence
POST /api/appels/{appelId}/presences
{
  "etudiantId": 1,
  "statut": "présent",
  "modeEnregistrement": "manuel"
}
```

#### Fonctionnalités Communes
- Bouton "Démarrer l'appel" (si pas encore démarré)
- Bouton "Clôturer l'appel"
- Affichage des statistiques en temps réel:
  - Nombre de présents
  - Nombre d'absents
  - Nombre de retards
- Mode hors ligne avec IndexedDB

**API à utiliser**:
```javascript
// Démarrer un appel
POST /api/appels
{
  "seanceId": 1,
  "modeAppel": "QR"
}

// Clôturer un appel
PUT /api/appels/{appelId}/cloturer

// Récupérer l'appel ouvert
GET /api/appels/seance/{seanceId}/ouvert
```

## 🔧 Technologies Recommandées

### Build Tool
- **Vite** - Build rapide et HMR
```bash
npm create vite@latest enseignant-app -- --template vanilla
```

### UI Framework
- **Bootstrap 5** - Composants responsive
```html
<link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
```

### QR Code Scanner
- **html5-qrcode** - Scanner QR code dans le navigateur
```bash
npm install html5-qrcode
```

### Offline Storage
- **IndexedDB** - Stockage local pour mode hors ligne
- **Dexie.js** (optionnel) - Wrapper simplifié pour IndexedDB
```bash
npm install dexie
```

### HTTP Client
- **Axios** ou **Fetch API**
```bash
npm install axios
```

## 📱 Progressive Web App (PWA)

### Service Worker
Créer un service worker pour le mode hors ligne:

```javascript
// sw.js
self.addEventListener('install', (event) => {
  event.waitUntil(
    caches.open('appel-cache-v1').then((cache) => {
      return cache.addAll([
        '/enseignant/dashboard.html',
        '/enseignant/emploi.html',
        '/enseignant/appel.html',
        '/css/style.css',
        '/js/app.js'
      ]);
    })
  );
});

self.addEventListener('fetch', (event) => {
  event.respondWith(
    caches.match(event.request).then((response) => {
      return response || fetch(event.request);
    })
  );
});
```

### Manifest
```json
{
  "name": "Gestion des Appels",
  "short_name": "Appels",
  "start_url": "/enseignant/dashboard.html",
  "display": "standalone",
  "background_color": "#ffffff",
  "theme_color": "#0f22f1",
  "icons": [
    {
      "src": "/icons/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    },
    {
      "src": "/icons/icon-512.png",
      "sizes": "512x512",
      "type": "image/png"
    }
  ]
}
```

## 🔐 Gestion de l'Authentification

### Intercepteur Axios
```javascript
import axios from 'axios';

const api = axios.create({
  baseURL: 'http://localhost:8080/api'
});

// Ajouter le token à chaque requête
api.interceptors.request.use((config) => {
  const token = localStorage.getItem('token');
  if (token) {
    config.headers.Authorization = `Bearer ${token}`;
  }
  return config;
});

// Gérer les erreurs 401
api.interceptors.response.use(
  (response) => response,
  (error) => {
    if (error.response?.status === 401) {
      localStorage.removeItem('token');
      window.location.href = '/login.html';
    }
    return Promise.reject(error);
  }
);

export default api;
```

## 📊 Gestion du Mode Hors Ligne

### Stratégie de Synchronisation

1. **Enregistrement hors ligne**:
   - Stocker les présences dans IndexedDB
   - Marquer comme "non synchronisé"

2. **Détection de la connexion**:
```javascript
window.addEventListener('online', () => {
  synchroniserPresences();
});
```

3. **Synchronisation**:
```javascript
async function synchroniserPresences() {
  const presencesNonSync = await db.presences
    .where('synchronise')
    .equals(false)
    .toArray();
  
  if (presencesNonSync.length > 0) {
    try {
      await api.post('/appels/presences/batch', {
        appelId: appelId,
        presences: presencesNonSync
      });
      
      // Marquer comme synchronisé
      await db.presences
        .where('synchronise')
        .equals(false)
        .modify({ synchronise: true });
    } catch (error) {
      console.error('Erreur de synchronisation:', error);
    }
  }
}
```

## 🎯 Structure des Fichiers Frontend

```
/
├── login.html
├── enseignant/
│   ├── dashboard.html
│   ├── emploi.html
│   └── appel.html
├── css/
│   ├── style.css
│   └── appel.css
├── js/
│   ├── api.js (client HTTP)
│   ├── auth.js (gestion auth)
│   ├── dashboard.js
│   ├── emploi.js
│   ├── appel.js
│   └── offline.js (gestion offline)
├── sw.js (service worker)
└── manifest.json
```

## ✅ Checklist d'Implémentation

### Phase 1: Base
- [ ] Créer la page de login
- [ ] Implémenter l'authentification JWT
- [ ] Créer le client API avec intercepteurs
- [ ] Tester la connexion et la redirection

### Phase 2: Dashboard
- [ ] Créer le dashboard enseignant
- [ ] Afficher les statistiques
- [ ] Lister les cours du jour
- [ ] Navigation vers emploi du temps

### Phase 3: Emploi du Temps
- [ ] Créer la page emploi du temps
- [ ] Afficher les séances en cartes
- [ ] Implémenter les filtres
- [ ] Navigation vers l'appel

### Phase 4: Interface d'Appel
- [ ] Créer la structure avec onglets
- [ ] Implémenter le scan QR
- [ ] Implémenter la liste de classe
- [ ] Démarrer/clôturer l'appel
- [ ] Statistiques en temps réel

### Phase 5: Mode Hors Ligne
- [ ] Configurer IndexedDB
- [ ] Implémenter le stockage local
- [ ] Créer le service worker
- [ ] Implémenter la synchronisation
- [ ] Tester le mode offline

### Phase 6: PWA
- [ ] Créer le manifest.json
- [ ] Ajouter les icônes
- [ ] Tester l'installation PWA
- [ ] Optimiser les performances

## 🧪 Tests à Effectuer

1. **Authentification**
   - Login avec différents rôles
   - Redirection correcte
   - Gestion des erreurs

2. **Gestion des Appels**
   - Démarrer un appel (QR et PIN)
   - Scanner des QR codes
   - Enregistrer des présences manuellement
   - Clôturer un appel
   - Vérifier les absents automatiques

3. **Mode Hors Ligne**
   - Enregistrer des présences offline
   - Vérifier le stockage IndexedDB
   - Tester la synchronisation
   - Gérer les conflits

4. **Responsive Design**
   - Tester sur mobile
   - Tester sur tablette
   - Tester sur desktop

## 📚 Ressources Utiles

- [Bootstrap 5 Documentation](https://getbootstrap.com/docs/5.3/)
- [html5-qrcode Documentation](https://github.com/mebjas/html5-qrcode)
- [IndexedDB API](https://developer.mozilla.org/en-US/docs/Web/API/IndexedDB_API)
- [Service Worker API](https://developer.mozilla.org/en-US/docs/Web/API/Service_Worker_API)
- [PWA Documentation](https://web.dev/progressive-web-apps/)

## 🚀 Commandes Utiles

```bash
# Installer les dépendances
npm install

# Démarrer le serveur de développement
npm run dev

# Build pour production
npm run build

# Prévisualiser le build
npm run preview
```

## 💡 Conseils

1. **Mobile-First**: Commencer par le design mobile
2. **Progressive Enhancement**: Fonctionnalités de base d'abord, puis améliorations
3. **Feedback Utilisateur**: Toujours donner un retour visuel (loading, success, error)
4. **Gestion d'Erreurs**: Prévoir tous les cas d'erreur possibles
5. **Performance**: Optimiser les images et le code
6. **Accessibilité**: Respecter les standards WCAG

Bonne implémentation! 🎉
