# Exemples de Code Frontend

## 🔐 Client API (api.js)

```javascript
// api.js
import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080/api';

class ApiClient {
  constructor() {
    this.client = axios.create({
      baseURL: API_BASE_URL,
      headers: {
        'Content-Type': 'application/json'
      }
    });

    // Intercepteur pour ajouter le token
    this.client.interceptors.request.use(
      (config) => {
        const token = localStorage.getItem('token');
        if (token) {
          config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
      },
      (error) => Promise.reject(error)
    );

    // Intercepteur pour gérer les erreurs
    this.client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 401) {
          localStorage.removeItem('token');
          window.location.href = '/login.html';
        }
        return Promise.reject(error);
      }
    );
  }

  // Authentification
  async login(email, password) {
    const response = await this.client.post('/auth/login', { email, password });
    return response.data;
  }

  // Séances
  async getSeancesAujourdhui(enseignantId) {
    const response = await this.client.get(`/seances/enseignant/${enseignantId}`);
    return response.data;
  }

  async getSeancesSemaine(enseignantId) {
    const response = await this.client.get(`/seances/enseignant/${enseignantId}/semaine`);
    return response.data;
  }

  // Appels
  async demarrerAppel(seanceId, modeAppel = 'QR') {
    const response = await this.client.post('/appels', {
      seanceId,
      modeAppel,
      dureeValiditePIN: 5
    });
    return response.data;
  }

  async cloturerAppel(appelId) {
    const response = await this.client.put(`/appels/${appelId}/cloturer`);
    return response.data;
  }

  async getAppelOuvert(seanceId) {
    const response = await this.client.get(`/appels/seance/${seanceId}/ouvert`);
    return response.data;
  }

  async getEtudiantsSeance(seanceId, appelId) {
    const params = appelId ? { appelId } : {};
    const response = await this.client.get(`/appels/seance/${seanceId}/etudiants`, { params });
    return response.data;
  }

  async enregistrerPresence(appelId, etudiantId, statut, modeEnregistrement = 'manuel') {
    const response = await this.client.post(`/appels/${appelId}/presences`, {
      etudiantId,
      statut,
      modeEnregistrement
    });
    return response.data;
  }

  async enregistrerPresencesBatch(appelId, presences) {
    const response = await this.client.post('/appels/presences/batch', {
      appelId,
      presences
    });
    return response.data;
  }

  async validerCodePIN(appelId, codePIN) {
    const response = await this.client.post(`/appels/${appelId}/valider-pin`, { codePIN });
    return response.data;
  }
}

export default new ApiClient();
```

## 🔑 Gestion de l'Authentification (auth.js)

```javascript
// auth.js
import api from './api.js';

class AuthService {
  async login(email, password) {
    try {
      const data = await api.login(email, password);
      
      // Stocker le token et les infos utilisateur
      localStorage.setItem('token', data.token);
      localStorage.setItem('user', JSON.stringify(data.user));
      
      return data;
    } catch (error) {
      throw new Error('Identifiants invalides');
    }
  }

  logout() {
    localStorage.removeItem('token');
    localStorage.removeItem('user');
    window.location.href = '/login.html';
  }

  getUser() {
    const userStr = localStorage.getItem('user');
    return userStr ? JSON.parse(userStr) : null;
  }

  isAuthenticated() {
    return !!localStorage.getItem('token');
  }

  redirectByRole() {
    const user = this.getUser();
    if (!user) return;

    switch (user.role) {
      case 'ENSEIGNANT':
        window.location.href = '/enseignant/dashboard.html';
        break;
      case 'ASSISTANT_PEDAGOGIQUE':
        window.location.href = '/assistant/tableaudebordass.html';
        break;
      case 'ADMIN':
        window.location.href = '/admin/tableaudebord.html';
        break;
      default:
        window.location.href = '/';
    }
  }
}

export default new AuthService();
```

## 📱 Page de Login (login.html)

```html
<!DOCTYPE html>
<html lang="fr">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>Connexion - Gestion des Appels</title>
  <link href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css" rel="stylesheet">
</head>
<body class="bg-light">
  <div class="container">
    <div class="row justify-content-center align-items-center min-vh-100">
      <div class="col-md-6 col-lg-4">
        <div class="card shadow">
          <div class="card-body p-4">
            <h2 class="text-center mb-4">Connexion</h2>
            
            <div id="error-message" class="alert alert-danger d-none"></div>
            
            <form id="login-form">
              <div class="mb-3">
                <label for="email" class="form-label">Email</label>
                <input type="email" class="form-control" id="email" required>
              </div>
              
              <div class="mb-3">
                <label for="password" class="form-label">Mot de passe</label>
                <input type="password" class="form-control" id="password" required>
              </div>
              
              <button type="submit" class="btn btn-primary w-100">
                <span id="login-text">Se connecter</span>
                <span id="login-spinner" class="spinner-border spinner-border-sm d-none"></span>
              </button>
            </form>
          </div>
        </div>
      </div>
    </div>
  </div>

  <script type="module">
    import auth from './js/auth.js';

    const form = document.getElementById('login-form');
    const errorMessage = document.getElementById('error-message');
    const loginText = document.getElementById('login-text');
    const loginSpinner = document.getElementById('login-spinner');

    form.addEventListener('submit', async (e) => {
      e.preventDefault();
      
      const email = document.getElementById('email').value;
      const password = document.getElementById('password').value;
      
      // Afficher le spinner
      loginText.classList.add('d-none');
      loginSpinner.classList.remove('d-none');
      errorMessage.classList.add('d-none');
      
      try {
        await auth.login(email, password);
        auth.redirectByRole();
      } catch (error) {
        errorMessage.textContent = error.message;
        errorMessage.classList.remove('d-none');
      } finally {
        loginText.classList.remove('d-none');
        loginSpinner.classList.add('d-none');
      }
    });
  </script>
</body>
</html>
```

## 📊 Dashboard Enseignant (dashboard.js)

```javascript
// dashboard.js
import api from './api.js';
import auth from './auth.js';

class Dashboard {
  constructor() {
    this.user = auth.getUser();
    if (!this.user) {
      window.location.href = '/login.html';
      return;
    }
    
    this.init();
  }

  async init() {
    await this.loadSeancesAujourdhui();
    await this.loadStatistiques();
  }

  async loadSeancesAujourdhui() {
    try {
      const seances = await api.getSeancesAujourdhui(this.user.id);
      this.renderSeances(seances);
    } catch (error) {
      console.error('Erreur chargement séances:', error);
    }
  }

  renderSeances(seances) {
    const container = document.getElementById('seances-container');
    
    if (seances.length === 0) {
      container.innerHTML = '<p class="text-muted">Aucun cours aujourd\'hui</p>';
      return;
    }

    container.innerHTML = seances.map(seance => `
      <div class="card mb-3">
        <div class="card-body">
          <div class="d-flex justify-content-between align-items-start">
            <div>
              <h5 class="card-title">${seance.coursIntitule}</h5>
              <p class="card-text text-muted">
                ${seance.heureDebut} - ${seance.heureFin}<br>
                Classe: ${seance.classeNom}
              </p>
            </div>
            <span class="badge ${this.getAppelBadgeClass(seance.appelStatut)}">
              ${this.getAppelStatusText(seance.appelStatut)}
            </span>
          </div>
          
          ${seance.appelId ? `
            <div class="mt-2">
              <small class="text-muted">
                Présents: ${seance.nombrePresents} | 
                Absents: ${seance.nombreAbsents} | 
                Retards: ${seance.nombreRetards}
              </small>
            </div>
          ` : ''}
          
          <button class="btn btn-primary btn-sm mt-2" 
                  onclick="window.location.href='/enseignant/appel.html?seanceId=${seance.id}'">
            ${seance.appelStatut === 'ouvert' ? 'Continuer l\'appel' : 'Faire l\'appel'}
          </button>
        </div>
      </div>
    `).join('');
  }

  getAppelBadgeClass(statut) {
    switch (statut) {
      case 'ouvert': return 'bg-success';
      case 'clôturé': return 'bg-secondary';
      default: return 'bg-warning';
    }
  }

  getAppelStatusText(statut) {
    switch (statut) {
      case 'ouvert': return 'En cours';
      case 'clôturé': return 'Terminé';
      default: return 'Non démarré';
    }
  }

  async loadStatistiques() {
    try {
      const seances = await api.getSeancesSemaine(this.user.id);
      
      const stats = {
        coursAujourdhui: seances.filter(s => s.date === new Date().toISOString().split('T')[0]).length,
        coursSemaine: seances.length,
        tauxPresence: this.calculerTauxPresence(seances)
      };
      
      this.renderStatistiques(stats);
    } catch (error) {
      console.error('Erreur chargement statistiques:', error);
    }
  }

  calculerTauxPresence(seances) {
    const seancesAvecAppel = seances.filter(s => s.appelId);
    if (seancesAvecAppel.length === 0) return 0;
    
    const totalPresents = seancesAvecAppel.reduce((sum, s) => sum + s.nombrePresents, 0);
    const totalEtudiants = seancesAvecAppel.reduce((sum, s) => sum + s.nombreTotal, 0);
    
    return totalEtudiants > 0 ? Math.round((totalPresents / totalEtudiants) * 100) : 0;
  }

  renderStatistiques(stats) {
    document.getElementById('cours-aujourdhui').textContent = stats.coursAujourdhui;
    document.getElementById('cours-semaine').textContent = stats.coursSemaine;
    document.getElementById('taux-presence').textContent = `${stats.tauxPresence}%`;
  }
}

// Initialiser le dashboard
new Dashboard();
```

## 📸 Scanner QR Code (qr-scanner.js)

```javascript
// qr-scanner.js
import { Html5Qrcode } from 'html5-qrcode';
import api from './api.js';

class QRScanner {
  constructor(appelId, onSuccess) {
    this.appelId = appelId;
    this.onSuccess = onSuccess;
    this.scanner = null;
    this.isScanning = false;
  }

  async start() {
    if (this.isScanning) return;

    try {
      this.scanner = new Html5Qrcode("qr-reader");
      
      await this.scanner.start(
        { facingMode: "environment" },
        {
          fps: 10,
          qrbox: { width: 250, height: 250 }
        },
        (decodedText) => this.handleScan(decodedText),
        (errorMessage) => {
          // Ignorer les erreurs de scan (pas de QR code détecté)
        }
      );
      
      this.isScanning = true;
    } catch (error) {
      console.error('Erreur démarrage scanner:', error);
      alert('Impossible d\'accéder à la caméra');
    }
  }

  async stop() {
    if (!this.isScanning || !this.scanner) return;

    try {
      await this.scanner.stop();
      this.isScanning = false;
    } catch (error) {
      console.error('Erreur arrêt scanner:', error);
    }
  }

  async handleScan(decodedText) {
    // Format attendu: APPEL:{appelId}:{timestamp}
    const parts = decodedText.split(':');
    
    if (parts.length !== 3 || parts[0] !== 'APPEL') {
      this.showError('QR code invalide');
      return;
    }

    const qrAppelId = parseInt(parts[1]);
    
    if (qrAppelId !== this.appelId) {
      this.showError('Ce QR code ne correspond pas à cet appel');
      return;
    }

    // Extraire l'ID étudiant du QR code (à adapter selon votre format)
    // Pour l'instant, on suppose que l'étudiant scanne son propre QR code
    // qui contient son ID
    
    try {
      // Ici, vous devez adapter selon votre logique
      // Par exemple, le QR code pourrait contenir l'ID de l'étudiant
      const etudiantId = this.extractEtudiantId(decodedText);
      
      await api.enregistrerPresence(this.appelId, etudiantId, 'présent', 'QR');
      
      this.showSuccess('Présence enregistrée');
      this.onSuccess();
      
      // Pause de 2 secondes avant le prochain scan
      await this.stop();
      setTimeout(() => this.start(), 2000);
      
    } catch (error) {
      this.showError('Erreur lors de l\'enregistrement');
    }
  }

  extractEtudiantId(qrCode) {
    // À implémenter selon votre format de QR code
    // Exemple: "ETUDIANT:123" ou extraire depuis une base de données
    return 1; // Placeholder
  }

  showSuccess(message) {
    // Afficher un toast de succès
    this.showToast(message, 'success');
  }

  showError(message) {
    // Afficher un toast d'erreur
    this.showToast(message, 'danger');
  }

  showToast(message, type) {
    const toast = document.createElement('div');
    toast.className = `alert alert-${type} position-fixed top-0 start-50 translate-middle-x mt-3`;
    toast.style.zIndex = '9999';
    toast.textContent = message;
    
    document.body.appendChild(toast);
    
    setTimeout(() => {
      toast.remove();
    }, 3000);
  }
}

export default QRScanner;
```

## 📝 Liste de Classe (classe-list.js)

```javascript
// classe-list.js
import api from './api.js';

class ClasseList {
  constructor(seanceId, appelId) {
    this.seanceId = seanceId;
    this.appelId = appelId;
    this.etudiants = [];
    this.init();
  }

  async init() {
    await this.loadEtudiants();
    this.render();
    this.attachEventListeners();
  }

  async loadEtudiants() {
    try {
      this.etudiants = await api.getEtudiantsSeance(this.seanceId, this.appelId);
    } catch (error) {
      console.error('Erreur chargement étudiants:', error);
    }
  }

  render() {
    const container = document.getElementById('classe-list');
    
    container.innerHTML = `
      <div class="mb-3">
        <input type="text" class="form-control" id="search-etudiant" 
               placeholder="Rechercher un étudiant...">
      </div>
      
      <div id="etudiants-container">
        ${this.etudiants.map(etudiant => this.renderEtudiant(etudiant)).join('')}
      </div>
    `;
  }

  renderEtudiant(etudiant) {
    return `
      <div class="card mb-2 etudiant-card" data-etudiant-id="${etudiant.id}">
        <div class="card-body py-2">
          <div class="d-flex justify-content-between align-items-center">
            <div>
              <strong>${etudiant.nom} ${etudiant.prenom}</strong><br>
              <small class="text-muted">${etudiant.matricule}</small>
            </div>
            
            <div class="btn-group" role="group">
              <button type="button" 
                      class="btn btn-sm ${etudiant.statut === 'présent' ? 'btn-success' : 'btn-outline-success'}"
                      data-statut="présent">
                Présent
              </button>
              <button type="button" 
                      class="btn btn-sm ${etudiant.statut === 'retard' ? 'btn-warning' : 'btn-outline-warning'}"
                      data-statut="retard">
                Retard
              </button>
              <button type="button" 
                      class="btn btn-sm ${etudiant.statut === 'absent' ? 'btn-danger' : 'btn-outline-danger'}"
                      data-statut="absent">
                Absent
              </button>
            </div>
          </div>
        </div>
      </div>
    `;
  }

  attachEventListeners() {
    // Recherche
    document.getElementById('search-etudiant')?.addEventListener('input', (e) => {
      this.filterEtudiants(e.target.value);
    });

    // Boutons de statut
    document.querySelectorAll('.etudiant-card').forEach(card => {
      card.querySelectorAll('button[data-statut]').forEach(btn => {
        btn.addEventListener('click', async (e) => {
          const etudiantId = parseInt(card.dataset.etudiantId);
          const statut = e.target.dataset.statut;
          await this.updateStatut(etudiantId, statut);
        });
      });
    });
  }

  async updateStatut(etudiantId, statut) {
    try {
      await api.enregistrerPresence(this.appelId, etudiantId, statut, 'manuel');
      
      // Mettre à jour l'affichage
      const etudiant = this.etudiants.find(e => e.id === etudiantId);
      if (etudiant) {
        etudiant.statut = statut;
        this.render();
        this.attachEventListeners();
      }
      
      // Émettre un événement pour mettre à jour les statistiques
      window.dispatchEvent(new CustomEvent('presence-updated'));
      
    } catch (error) {
      console.error('Erreur mise à jour statut:', error);
      alert('Erreur lors de la mise à jour');
    }
  }

  filterEtudiants(searchTerm) {
    const term = searchTerm.toLowerCase();
    
    document.querySelectorAll('.etudiant-card').forEach(card => {
      const text = card.textContent.toLowerCase();
      card.style.display = text.includes(term) ? 'block' : 'none';
    });
  }

  async refresh() {
    await this.loadEtudiants();
    this.render();
    this.attachEventListeners();
  }
}

export default ClasseList();
```

Ces exemples fournissent une base solide pour implémenter le frontend. Adaptez-les selon vos besoins spécifiques! 🚀
