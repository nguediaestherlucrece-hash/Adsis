# Plan — Liaison sécurisée Frontend ↔ Backend (JWT + Rôles + Permissions + Profil Universel)

## Contexte et diagnostic

Le backend Spring Boot est **complet et fonctionnel** :
- `JwtService` : génération / validation tokens HS512
- `JwtAuthFilter` : intercepte chaque requête, valide le JWT
- `AuthController` : `/api/auth/login`, `/refresh`, `/logout`, `/validate`
- `Utilisateur` implémente `UserDetails` → `getAuthorities()` retourne les **permissions effectives** (rôle - permissions retirées via `UserPermissionOverride`)
- `AdministrateurController` : CRUD `/api/administrateurs` protégé par `hasRole('ADMIN')`

**Ce qui manque côté frontend :**
- `login.js` en mode DEMO
- `api.js` sans header `Authorization`
- Aucune vérification du rôle ni des permissions sur les pages
- `gestionutilisateur.html` ne gère pas les comptes Administrateurs
- `AuthResponse` ne renvoie ni rôles ni permissions
- Page Profil : coexistence de 2 pages séparées (`admin/profil.html` + `enseignant/profilens.html`) alors qu'une page unique et universelle est requise

---

## Partie 0 — Backend : enrichir AuthResponse (rôles + permissions)

### 0.1 — [MODIFY] `AuthResponse.java`

```java
private List<String> roles;        // ex: ["ENSEIGNANT"]
private List<String> permissions;  // ex: ["ATTENDANCE_MANAGE", "COURSE_VIEW"] = permissions effectives
```

### 0.2 — [MODIFY] `AuthController.java` — méthodes `login()` ET `refreshToken()`

Après récupération de l'utilisateur, collecter rôles et permissions effectives :

```java
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.security.core.GrantedAuthority;

// Rôles de l'utilisateur
List<String> roles = utilisateur.getRoles().stream()
    .map(Role::getName)
    .toList();

// Permissions EFFECTIVES (rôle - overrides retirées) déjà calculées par Utilisateur.getAuthorities()
List<String> permissions = utilisateur.getAuthorities().stream()
    .map(GrantedAuthority::getAuthority)
    .collect(Collectors.toList());

AuthResponse authResponse = AuthResponse.builder()
    .accessToken(accessToken)
    .refreshToken(refreshToken)
    .expiresIn(3600000L)
    .id(utilisateur.getId())
    .email(utilisateur.getEmail())
    .nom(utilisateur.getNom())
    .prenom(utilisateur.getPrenom())
    .roles(roles)           // <- AJOUT
    .permissions(permissions) // <- AJOUT
    .build();
```

> **Note :** `utilisateur.getAuthorities()` appelle déjà `getAuthorities()` qui soustrait les permissions retirées (`UserPermissionOverride`). Les permissions retournées sont donc les **permissions réelles et finales** de l'utilisateur, pas celles du rôle brut. C'est exactement ce qu'on veut.

---

## Partie 1 — [NEW] `script/auth.js` — Module central de sécurité frontend

Fichier **pilier** importé par tous les autres scripts.

### 1.1 — Constantes

```js
const API_BASE_URL = 'http://localhost:8081';
const KEYS = {
    ACCESS_TOKEN:  'adsis_token',
    REFRESH_TOKEN: 'adsis_refresh_token',
    USER:          'adsis_user',
};
```

### 1.2 — Gestion de session

```js
export function saveSession(data) {
    localStorage.setItem(KEYS.ACCESS_TOKEN, data.accessToken);
    localStorage.setItem(KEYS.REFRESH_TOKEN, data.refreshToken);
    localStorage.setItem(KEYS.USER, JSON.stringify({
        id: data.id,
        nom: data.nom,
        prenom: data.prenom,
        email: data.email,
        roles: data.roles,             // ['ENSEIGNANT']
        permissions: data.permissions, // ['ATTENDANCE_MANAGE', 'COURSE_VIEW', ...]
    }));
}

export function getToken()        { return localStorage.getItem(KEYS.ACCESS_TOKEN); }
export function getRefreshToken() { return localStorage.getItem(KEYS.REFRESH_TOKEN); }
export function getUser() {
    try { return JSON.parse(localStorage.getItem(KEYS.USER) || 'null'); }
    catch  { return null; }
}
export function clearSession() {
    Object.values(KEYS).forEach(k => localStorage.removeItem(k));
}
```

### 1.3 — Helpers permissions et rôles

```js
/**
 * Vérifie si l'utilisateur a UN rôle parmi la liste fournie.
 */
export function hasRole(allowedRoles = []) {
    const user = getUser();
    if (!user) return false;
    return allowedRoles.some(r => (user.roles || []).includes(r));
}

/**
 * Vérifie si l'utilisateur a UNE permission parmi la liste fournie.
 * C'est la vérification fine-grain : tient compte des retirées individuellement.
 */
export function hasPermission(requiredPermissions = []) {
    const user = getUser();
    if (!user) return false;
    // Les admins ont accès à tout (pas de restriction par permission)
    if ((user.roles || []).includes('ADMIN')) return true;
    return requiredPermissions.some(p => (user.permissions || []).includes(p));
}
```

### 1.4 — `authFetch()` : fetch authentifié avec refresh silencieux

```js
let refreshPromise = null;

export async function authFetch(url, options = {}) {
    const token = getToken();
    options.headers = {
        'Content-Type': 'application/json',
        ...(options.headers || {}),
        ...(token ? { 'Authorization': `Bearer ${token}` } : {}),
    };

    let response = await fetch(`${API_BASE_URL}${url}`, options);

    if (response.status === 401) {
        const refreshed = await tryRefreshToken();
        if (refreshed) {
            options.headers['Authorization'] = `Bearer ${getToken()}`;
            response = await fetch(`${API_BASE_URL}${url}`, options);
        } else {
            clearSession();
            window.location.href = getLoginPath();
            return;
        }
    }
    return response;
}

async function tryRefreshToken() {
    if (refreshPromise) return refreshPromise;
    refreshPromise = (async () => {
        const rt = getRefreshToken();
        if (!rt) return false;
        try {
            const res = await fetch(`${API_BASE_URL}/api/auth/refresh`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ refreshToken: rt }),
            });
            if (!res.ok) return false;
            saveSession(await res.json());
            return true;
        } catch { return false; }
        finally { refreshPromise = null; }
    })();
    return refreshPromise;
}
```

### 1.5 — Guard de rôle + Guard de permission + Logout

```js
/**
 * Guard de RÔLE : protège une page entière.
 * Si le user n'a pas le rôle → redirige vers son propre dashboard.
 */
export function requireRole(allowedRoles = []) {
    const user = getUser();
    if (!user) { clearSession(); window.location.href = getLoginPath(); return false; }
    if (!allowedRoles.some(r => (user.roles || []).includes(r))) {
        redirectToDashboard(user.roles || []);
        return false;
    }
    return true;
}

/**
 * Guard de PERMISSION : masque un élément UI ou bloque une action si permission absente.
 * Usage : if (!requirePermission('ATTENDANCE_MANAGE')) { return; }
 * ou dans le HTML : appeler hideIfNoPermission() sur des éléments.
 */
export function requirePermission(permission) {
    const user = getUser();
    if (!user) return false;
    if ((user.roles || []).includes('ADMIN')) return true; // admin passe tout
    return (user.permissions || []).includes(permission);
}

/**
 * Masque les éléments DOM portant data-requires-permission="PERMISSION_NAME"
 * si l'utilisateur ne possède pas la permission.
 * À appeler sur DOMContentLoaded dans chaque page.
 */
export function applyPermissionVisibility() {
    const user = getUser();
    if (!user) return;
    const isAdmin = (user.roles || []).includes('ADMIN');
    document.querySelectorAll('[data-requires-permission]').forEach(el => {
        const perm = el.dataset.requiresPermission;
        const hasIt = isAdmin || (user.permissions || []).includes(perm);
        if (!hasIt) {
            el.style.display = 'none';
            el.setAttribute('aria-hidden', 'true');
        }
    });
}

function redirectToDashboard(roles) {
    if (roles.includes('ADMIN'))
        window.location.href = '../admin/tableaudebord.html';
    else if (roles.includes('ENSEIGNANT'))
        window.location.href = '../enseignant/dashboardens.html';
    else if (roles.includes('ASSISTANT_PEDAGOGIQUE') || roles.includes('SURVEILLANT'))
        window.location.href = '../assistant/tableaudebordass.html';
    else
        window.location.href = getLoginPath();
}

function getLoginPath() {
    const p = window.location.pathname;
    return (p.includes('/admin/') || p.includes('/assistant/') || p.includes('/enseignant/'))
        ? '../enseignant/login.html'
        : './enseignant/login.html';
}

export function logout() {
    authFetch('/api/auth/logout', { method: 'POST' }).catch(() => {});
    clearSession();
    window.location.href = getLoginPath();
}
```

---

## Partie 2 — Comment utiliser les permissions dans chaque page

### 2.1 — Approche par attribut HTML `data-requires-permission`

C'est la méthode la plus propre et la plus maintenable. Dans chaque page HTML, les boutons/sections sensibles portent l'attribut `data-requires-permission` avec la permission requise. `applyPermissionVisibility()` les masque automatiquement si l'utilisateur ne l'a pas.

**Exemple dans `assistant/emploidutemps.html` :**

```html
<!-- Créer un emploi du temps : nécessite COURSE_MANAGE -->
<button id="btn-create-emploi" data-requires-permission="COURSE_MANAGE">
    + Créer une séance
</button>

<!-- Voir uniquement : nécessite juste COURSE_VIEW -->
<section id="section-calendrier" data-requires-permission="COURSE_VIEW">
    <!-- le tableau de l'emploi du temps -->
</section>

<!-- Gérer les étudiants : nécessite STUDENT_VIEW -->
<nav-item data-requires-permission="STUDENT_VIEW">
    <a href="gestionetudiant.html">Gestion Étudiants</a>
</nav-item>
```

Dans le script JS de la page :

```js
import { requireRole, applyPermissionVisibility } from '../script/auth.js';

requireRole(['ASSISTANT_PEDAGOGIQUE', 'SURVEILLANT']);

document.addEventListener('DOMContentLoaded', () => {
    applyPermissionVisibility(); // masque les éléments sans permission
    // ... reste du code
});
```

### 2.2 — Approche par vérification programmatique

Pour les actions (pas juste la visibilité), utiliser `requirePermission()` directement dans le handler :

```js
import { requirePermission } from '../script/auth.js';

async function handleSaveSeance() {
    if (!requirePermission('COURSE_MANAGE')) {
        showToast("Vous n'avez pas la permission de modifier l'emploi du temps.", 'danger');
        return;
    }
    // ... code de sauvegarde
}
```

### 2.3 — Tableau de correspondance permissions / pages / actions

| Page | Permission requise | Action protégée |
|---|---|---|
| `assistant/emploidutemps.html` | `COURSE_VIEW` | Voir le planning |
| `assistant/emploidutemps.html` | `COURSE_MANAGE` | Créer / modifier / supprimer une séance |
| `assistant/matiere.html` | `COURSE_VIEW` | Voir les matières |
| `assistant/matiere.html` | `COURSE_MANAGE` | Ajouter / modifier une matière |
| `assistant/gestionetudiant.html` | `STUDENT_VIEW` | Voir les étudiants |
| `enseignant/appel.html` | `ATTENDANCE_MANAGE` | Saisir les présences |
| `enseignant/appel.html` | `ATTENDANCE_VIEW` | Consulter les présences |
| `admin/gestionacces.html` | Rôle `ADMIN` | Modifier les permissions des rôles |
| `admin/gestionutilisateur.html` | `USER_MANAGE` | CRUD utilisateurs |

> La navigation latérale (sidebar) doit aussi masquer les liens vers des pages auxquelles l'utilisateur n'a pas accès, via `data-requires-permission`.

---

## Partie 3 — [MODIFY] `script/login.js` — Connexion réelle

```js
import { saveSession, getUser } from './auth.js';

// Guard session existante
(function checkSession() {
    const user = getUser();
    if (user?.roles?.length) routeByRole(user.roles);
})();

function routeByRole(roles = []) {
    if (roles.includes('ADMIN'))
        window.location.href = '../admin/tableaudebord.html';
    else if (roles.includes('ENSEIGNANT'))
        window.location.href = 'dashboardens.html';
    else if (roles.includes('ASSISTANT_PEDAGOGIQUE') || roles.includes('SURVEILLANT'))
        window.location.href = '../assistant/tableaudebordass.html';
    else
        window.location.href = 'dashboardens.html';
}

async function doLogin() {
    clearAlert();
    if (!validate()) { showAlert('Veuillez remplir correctement tous les champs.'); return; }
    setLoading(true);
    try {
        const res = await fetch('http://localhost:8081/api/auth/login', {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                email: emailInput.value.trim(),
                password: passInput.value,
            }),
        });
        if (!res.ok) {
            const err = await res.json().catch(() => ({}));
            throw new Error(err.message || 'Email ou mot de passe incorrect.');
        }
        const data = await res.json();
        saveSession(data); // stocke token + roles + permissions
        showAlert('Connexion réussie !', 'success');
        setTimeout(() => routeByRole(data.roles), 600);
    } catch (err) {
        showAlert(err.message || 'Erreur de connexion. Réessayez.');
    } finally {
        setLoading(false);
    }
}
```

**HTML :** `<script type="module" src="../script/login.js">` dans `enseignant/login.html`.

---

## Partie 4 — [MODIFY] `script/api.js` — fetch → authFetch

```js
import { authFetch } from './auth.js';

// Remplacer :
// fetch(`${API_BASE_URL}/api/enseignants`)
// par :
// authFetch('/api/enseignants')
// (authFetch préfixe déjà avec http://localhost:8081)
```

Toutes les méthodes de l'objet `Api` (getUsers, getStaff, createUser, updateUser, deleteUser, getRoles, etc.) sont migrées.

---

## Partie 5 — [MODIFY] `script/shared.js` — authGuard + logout

```js
import { getUser, logout, applyPermissionVisibility } from './auth.js';

(function authGuard() {
    const user = getUser();
    if (!user) { window.location.href = '../enseignant/login.html'; return; }

    const fullName = `${user.prenom} ${user.nom}`;
    document.querySelectorAll('[data-user-name]').forEach(el => el.textContent = fullName);
    document.querySelectorAll('[data-user-email]').forEach(el => el.textContent = user.email);
    const initials = [user.prenom, user.nom].map(s => s?.[0] || '').join('').toUpperCase();
    document.querySelectorAll('.avatar, [data-user-initials]').forEach(el => el.textContent = initials);

    // Appliquer les restrictions de permissions sur la sidebar et les éléments UI
    applyPermissionVisibility();
})();

document.getElementById('btn-logout')?.addEventListener('click', e => {
    e.preventDefault();
    logout();
});
```

---

## Partie 6 — Guards dans les scripts de chaque page

### Module Admin
```js
import { requireRole, applyPermissionVisibility } from './auth.js';
requireRole(['ADMIN']);
document.addEventListener('DOMContentLoaded', applyPermissionVisibility);
```

### Module Enseignant
```js
import { requireRole, applyPermissionVisibility } from './auth.js';
requireRole(['ENSEIGNANT']);
document.addEventListener('DOMContentLoaded', applyPermissionVisibility);
```

### Module Assistant/Surveillant
```js
import { requireRole, applyPermissionVisibility } from './auth.js';
requireRole(['ASSISTANT_PEDAGOGIQUE', 'SURVEILLANT']);
document.addEventListener('DOMContentLoaded', applyPermissionVisibility);
```

---

## Partie 7 — [NEW] Page Profil universelle unique

### Contexte actuel

Il existe actuellement **2 pages de profil séparées** :
- `admin/profil.html` + `script/profile.js` (Bootstrap, très sommaire)
- `enseignant/profilens.html` + `script/profilens.js` (design premium, adapté enseignant)

### Décision architecturale

**Une seule page** : `profil/profil.html` (nouveau dossier) **ou** réutiliser `enseignant/profilens.html` comme page universelle (design premium déjà en place) accessible depuis n'importe quel module.

**→ Solution choisie : réutiliser `enseignant/profilens.html` comme base**, renommée en une page universelle accessible via un chemin partagé, par exemple `profil.html` à la racine du projet ou dans un dossier `shared/`.

> **Alternative plus simple (recommandée pour éviter les problèmes de chemins) :** garder la page dans `enseignant/profilens.html` mais y accéder depuis tous les modules via un lien relatif `../enseignant/profilens.html`. Les pages `admin/profil.html` et les profils assistant deviennent de simples **redirections** vers cette page unique.

### 7.1 — [MODIFY] `enseignant/profilens.html` — Adaptation universelle

La page devient la **page de profil universelle**. Elle doit :

#### a) Ajouter le bouton Déconnexion (manquant actuellement)

Dans `profilens.html`, dans la section footer de la sidebar, ajouter :

```html
<div class="sidebar-footer">
    <!-- Bouton déconnexion bien visible -->
    <button id="btn-logout" class="nav-item nav-logout" style="width:100%;">
        <svg viewBox="0 0 24 24" stroke-width="2">
            <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"/>
            <polyline points="16 17 21 12 16 7"/>
            <line x1="21" y1="12" x2="9" y2="12"/>
        </svg>
        Déconnexion
    </button>
</div>
```

#### b) Ajouter une section "Rôle & Permissions" dynamique

```html
<!-- Section qui s'affiche selon le rôle -->
<section class="profile-section" id="section-role-info">
    <div class="section-heading">
        <svg><!-- icône shield --></svg>
        Rôle & accès
    </div>
    <div id="role-badge-container">
        <!-- Peuplé dynamiquement par profilens.js -->
    </div>
    <!-- Champs spécifiques ENSEIGNANT -->
    <div id="section-enseignant" style="display:none;">
        <p>Spécialité : <span id="info-specialite">—</span></p>
        <p>Grade : <span id="info-grade">—</span></p>
    </div>
    <!-- Champs spécifiques ASSISTANT/SURVEILLANT -->
    <div id="section-assistant" style="display:none;">
        <p>Département : <span id="info-departement">—</span></p>
    </div>
    <!-- Champs spécifiques ADMIN -->
    <div id="section-admin" style="display:none;">
        <p>Compte administrateur système</p>
    </div>
</section>
```

#### c) Navigation de la sidebar adaptée selon le rôle

La sidebar doit avoir des liens différents selon le type d'utilisateur. Utiliser `data-role-link` pour masquer les liens non pertinents :

```html
<nav class="sidebar-nav">
    <!-- Liens ENSEIGNANT -->
    <a class="nav-item" href="dashboardens.html" data-role-link="ENSEIGNANT">
        Tableau de bord
    </a>
    <a class="nav-item" href="appel.html" data-role-link="ENSEIGNANT">
        Appel
    </a>
    <!-- Liens ADMIN -->
    <a class="nav-item" href="../admin/tableaudebord.html" data-role-link="ADMIN">
        Tableau de bord
    </a>
    <!-- Liens ASSISTANT -->
    <a class="nav-item" href="../assistant/tableaudebordass.html" data-role-link="ASSISTANT_PEDAGOGIQUE SURVEILLANT">
        Tableau de bord
    </a>
    <!-- Lien Profil commun à tous -->
    <a class="nav-item active" href="#">Mon profil</a>
</nav>
```

### 7.2 — [MODIFY] `script/profilens.js` — Adaptation universelle

```js
import { getUser, logout, authFetch, requirePermission } from './auth.js';

const user = getUser();
if (!user) { window.location.href = '../enseignant/login.html'; }

// ── Remplir les champs communs ────────────────────────────────────────────
document.getElementById('f-prenom').value        = user.prenom || '';
document.getElementById('f-nom').value           = user.nom    || '';
document.getElementById('f-email').value         = user.email  || '';
document.getElementById('prof-name').textContent = `${user.prenom} ${user.nom}`;

const initials = [user.prenom, user.nom].map(s => s?.[0] || '').join('').toUpperCase();
document.getElementById('prof-avatar').textContent = initials;

// ── Adapter la navigation sidebar selon le rôle ───────────────────────────
document.querySelectorAll('[data-role-link]').forEach(el => {
    const allowedRoles = el.dataset.roleLink.split(' ');
    const hasRole = allowedRoles.some(r => (user.roles || []).includes(r));
    if (!hasRole) el.style.display = 'none';
});

// ── Afficher la section spécifique au rôle ────────────────────────────────
const roleBadges = {
    ADMIN: { label: 'Administrateur', color: '#6366f1', sectionId: 'section-admin' },
    ENSEIGNANT: { label: 'Enseignant', color: '#3B55E6', sectionId: 'section-enseignant' },
    ASSISTANT_PEDAGOGIQUE: { label: 'Assistant Pédagogique', color: '#10b981', sectionId: 'section-assistant' },
    SURVEILLANT: { label: 'Surveillant', color: '#f59e0b', sectionId: 'section-assistant' },
};

const userRole = (user.roles || [])[0] || '';
const badgeCfg = roleBadges[userRole];
if (badgeCfg) {
    document.getElementById('role-badge-container').innerHTML =
        `<span class="prof-role-badge" style="background:${badgeCfg.color}20;color:${badgeCfg.color}">
            ${badgeCfg.label}
        </span>`;
    const section = document.getElementById(badgeCfg.sectionId);
    if (section) section.style.display = 'block';
}

// ── Charger les données spécifiques au rôle depuis l'API ──────────────────
async function loadRoleSpecificData() {
    if (!user.id) return;
    try {
        let endpoint = '';
        if (userRole === 'ENSEIGNANT') endpoint = `/api/enseignants/${user.id}`;
        else if (userRole === 'ASSISTANT_PEDAGOGIQUE') endpoint = `/api/assistants-pedagogiques/${user.id}`;
        else if (userRole === 'SURVEILLANT') endpoint = `/api/surveillants/${user.id}`;
        else if (userRole === 'ADMIN') endpoint = `/api/administrateurs/${user.id}`;
        
        if (!endpoint) return;
        const res = await authFetch(endpoint);
        if (!res.ok) return;
        const data = await res.json();

        // Pré-remplir les champs spécifiques
        if (userRole === 'ENSEIGNANT') {
            document.getElementById('info-specialite').textContent = data.specialite || '—';
            document.getElementById('info-grade').textContent = data.grade || '—';
        } else if (userRole === 'ASSISTANT_PEDAGOGIQUE' || userRole === 'SURVEILLANT') {
            document.getElementById('info-departement').textContent = data.departement || data.affectation || '—';
        }
    } catch (e) { console.warn('Données spécifiques non chargées:', e.message); }
}

loadRoleSpecificData();

// ── Sauvegarde du profil (PUT vers le bon endpoint) ───────────────────────
document.getElementById('btn-save').addEventListener('click', async () => {
    const prenom = document.getElementById('f-prenom').value.trim();
    const nom    = document.getElementById('f-nom').value.trim();
    const email  = document.getElementById('f-email').value.trim();
    const feedback = document.getElementById('save-feedback');
    if (!prenom || !nom || !email) {
        feedback.textContent = 'Veuillez remplir les champs obligatoires.';
        feedback.className = 'save-feedback error';
        return;
    }

    // Construire le payload
    const payload = { nom, prenom, email };

    // Déterminer l'endpoint selon le rôle
    let endpoint = '';
    if (userRole === 'ENSEIGNANT') endpoint = `/api/enseignants/${user.id}`;
    else if (userRole === 'ASSISTANT_PEDAGOGIQUE') endpoint = `/api/assistants-pedagogiques/${user.id}`;
    else if (userRole === 'SURVEILLANT') endpoint = `/api/surveillants/${user.id}`;
    else if (userRole === 'ADMIN') endpoint = `/api/administrateurs/${user.id}`;

    try {
        const res = await authFetch(endpoint, { method: 'PUT', body: JSON.stringify(payload) });
        if (!res.ok) throw new Error(await res.text());

        // Mettre à jour le localStorage
        const updated = { ...user, nom, prenom, email };
        localStorage.setItem('adsis_user', JSON.stringify(updated));
        document.getElementById('prof-name').textContent = `${prenom} ${nom}`;

        feedback.textContent = 'Modifications enregistrées !';
        feedback.className = 'save-feedback success';
        showToast('Profil mis à jour avec succès !', 'success');
    } catch (err) {
        feedback.textContent = 'Erreur : ' + err.message;
        feedback.className = 'save-feedback error';
    }
});

// ── Changement de mot de passe ────────────────────────────────────────────
document.getElementById('btn-change-pw').addEventListener('click', async () => {
    const cur  = document.getElementById('f-pw-cur').value;
    const nw   = document.getElementById('f-pw-new').value;
    const conf = document.getElementById('f-pw-conf').value;
    if (!cur || !nw || !conf) { showToast('Remplissez tous les champs.', 'error'); return; }
    if (nw.length < 6)         { showToast('6 caractères minimum.', 'error'); return; }
    if (nw !== conf)            { showToast('Mots de passe non identiques.', 'error'); return; }
    try {
        const res = await authFetch('/api/utilisateurs/password', {
            method: 'PUT',
            body: JSON.stringify({ currentPassword: cur, newPassword: nw }),
        });
        if (!res.ok) throw new Error(await res.text());
        showToast('Mot de passe modifié !', 'success');
        ['f-pw-cur','f-pw-new','f-pw-conf'].forEach(id => document.getElementById(id).value = '');
    } catch (err) {
        showToast('Erreur : ' + err.message, 'error');
    }
});

// ── Bouton Déconnexion ────────────────────────────────────────────────────
document.getElementById('btn-logout')?.addEventListener('click', e => {
    e.preventDefault();
    logout();
});
```

### 7.3 — [MODIFY] `admin/profil.html` — Redirection vers la page universelle

Remplacer tout le contenu par une simple redirection :

```html
<!doctype html>
<html lang="fr">
<head>
    <meta charset="UTF-8">
    <title>Redirection...</title>
    <script>window.location.replace('../enseignant/profilens.html');</script>
</head>
<body></body>
</html>
```

De même pour tout futur lien "Mon profil" dans le module `assistant/` qui pointera vers `../enseignant/profilens.html`.

### 7.4 — Liens "Mon profil" dans chaque module

| Module | Lien actuel | Nouveau lien |
|---|---|---|
| `admin/*.html` | `profil.html` | `../enseignant/profilens.html` |
| `assistant/*.html` | (inexistant) | `../enseignant/profilens.html` |
| `enseignant/*.html` | `profilens.html` | (inchangé) |

---

## Partie 8 — CRUD Administrateurs dans `gestionutilisateur.html` & `admin.js`

### 8.1 — HTML

```html
<!-- Onglet Administrateurs -->
<span class="filter-tab">Administrateurs</span>

<!-- Option dans la modal de création/édition -->
<option value="Administrateur">Administrateur</option>
```

### 8.2 — `admin.js` — Fonctions à ajouter

```js
// Charger les admins
async function loadAdmins() {
    try {
        const res = await authFetch('/api/administrateurs');
        if (!res.ok) throw new Error(await res.text());
        return (await res.json()).map(normalizeAdminUser);
    } catch (e) { console.warn(e.message); return []; }
}

// Normaliser un admin pour l'affichage
function normalizeAdminUser(u) {
    return {
        id: u.id, name: `${u.nom} ${u.prenom}`, email: u.email,
        role: 'Administrateur',
        status: u.statut === 'ACTIF' ? 'Actif' : 'Bloqué',
        details: '<span class="text-muted fst-italic small">— Compte administrateur</span>',
        filieres: [], matieres: [], raw: u, color: '#6366f1',
    };
}

// Dans loadUsers() : fusionner staff + admins
const [staff, admins] = await Promise.all([Api.getStaff(), loadAdmins()]);
currentUserList = [...staff.map(normalizeUser), ...admins];

// Dans handleSaveUser() : cas Administrateur
if (roleStr === 'Administrateur') {
    const endpoint = id ? `/api/administrateurs/${id}` : '/api/administrateurs';
    const res = await authFetch(endpoint, {
        method: id ? 'PUT' : 'POST',
        body: JSON.stringify({ ...baseData, roleNames: ['ADMIN'] }),
    });
    if (!res.ok) throw new Error(await res.text());
}

// Dans deleteUser() : cas Administrateur
if (user.role === 'Administrateur') {
    const res = await authFetch(`/api/administrateurs/${id}`, { method: 'DELETE' });
    if (!res.ok) throw new Error(await res.text());
} else { await Api.deleteUser(id, roleName); }

// Dans toggleBlock() : cas Administrateur
if (user.role === 'Administrateur') {
    const newStatut = user.status === 'Actif' ? 'INACTIF' : 'ACTIF';
    await authFetch(`/api/administrateurs/${id}`, {
        method: 'PUT',
        body: JSON.stringify({ ...user.raw, statut: newStatut }),
    });
}

// Dans updateCounts() : ajouter Administrateurs dans singularMap
const singularMap = {
    'Enseignants': 'Enseignant', 'Assistants': 'Assistant pédagogique',
    'Surveillants': 'Surveillant', 'Administrateurs': 'Administrateur',
};
```

---

## Partie 9 — CORS Backend & Routes publiques

```java
@Bean
public CorsConfigurationSource corsConfigurationSource() {
    CorsConfiguration config = new CorsConfiguration();
    config.setAllowedOriginPatterns(List.of("*"));
    config.setAllowedMethods(List.of("GET","POST","PUT","PATCH","DELETE","OPTIONS"));
    config.setAllowedHeaders(List.of("*"));
    config.setAllowCredentials(false);
    UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
    source.registerCorsConfiguration("/api/**", config);
    return source;
}
```

Vérifier que `/api/auth/login` et `/api/auth/refresh` sont en **route publique** (pas de JWT pour s'authentifier).

---

## Tableau récapitulatif — Tous les fichiers

### Backend
| Fichier | Action | Détail |
|---|---|---|
| `AuthResponse.java` | MODIFY | + `List<String> roles` + `List<String> permissions` |
| `AuthController.java` | MODIFY | Peupler `roles` et `permissions` dans login + refresh |
| `SecurityConfig.java` | VERIFY | CORS OK + `/api/auth/**` public |

### Frontend — Scripts (nouveaux ou modifiés)
| Fichier | Action | Détail |
|---|---|---|
| `script/auth.js` | **NEW** | saveSession, authFetch, requireRole, hasPermission, requirePermission, applyPermissionVisibility, logout |
| `script/login.js` | MODIFY | Appel réel, saveSession, routing par rôle |
| `script/api.js` | MODIFY | fetch → authFetch |
| `script/shared.js` | MODIFY | authGuard via auth.js, applyPermissionVisibility, #btn-logout |
| `script/profilens.js` | MODIFY | Page universelle : chargement conditionnel par rôle, authFetch, btn-logout |
| `script/profile.js` | DELETE | Remplacé par profilens.js universel |
| `script/admin.js` | MODIFY | requireRole ADMIN, authFetch, CRUD admins |
| `script/dashboard.js` | MODIFY | requireRole ADMIN, applyPermissionVisibility |
| `script/access.js` | MODIFY | requireRole ADMIN |
| `script/cycles.js` | MODIFY | requireRole ADMIN, authFetch |
| `script/appel.js` | MODIFY | requireRole ENSEIGNANT, requirePermission ATTENDANCE_MANAGE |
| `script/emploiens.js` | MODIFY | requireRole ENSEIGNANT |
| `script/dashboardens.js` | MODIFY | requireRole ENSEIGNANT |
| `script/emploi.js` | MODIFY | requireRole ASSISTANT/SURVEILLANT, permissions COURSE_MANAGE |
| `script/matiere.js` | MODIFY | requireRole ASSISTANT/SURVEILLANT, permissions COURSE_VIEW/MANAGE |
| `script/gestionetudiant.js` | MODIFY | requireRole ASSISTANT/SURVEILLANT, permission STUDENT_VIEW |
| `script/tableaudebordass.js` | MODIFY | requireRole ASSISTANT/SURVEILLANT |

### Frontend — HTML
| Fichier | Action | Détail |
|---|---|---|
| `enseignant/login.html` | MODIFY | script type="module" |
| `enseignant/profilens.html` | MODIFY | Page universelle : sidebar adaptative, section rôle, **#btn-logout ajouté**, liens assistants/admin |
| `admin/profil.html` | MODIFY | Redirection vers `../enseignant/profilens.html` |
| `admin/gestionutilisateur.html` | MODIFY | Onglet Administrateurs, option select, data-user-*, #btn-logout |
| `admin/*.html` | MODIFY | data-user-*, #btn-logout, lien profil → `../enseignant/profilens.html` |
| `assistant/*.html` | MODIFY | data-user-*, #btn-logout, lien profil → `../enseignant/profilens.html`, data-requires-permission |

---

## Ordre d'exécution

```
1. Backend : AuthResponse (+ roles + permissions)
2. Backend : AuthController (peupler roles + permissions dans login + refresh)
3. Backend : Vérifier CORS + routes publiques
4. Frontend : Créer script/auth.js (le pilier)
5. Frontend : Modifier script/login.js (appel réel + routing)
6. Frontend : Modifier script/api.js (authFetch partout)
7. Frontend : Modifier script/shared.js (authGuard + applyPermissionVisibility + logout)
8. Frontend : Guards requireRole() dans tous les scripts
9. Frontend : data-requires-permission sur les éléments sensibles (boutons, nav)
10. Frontend : Corriger les fetch() directs dans admin.js, appel.js, cycles.js, etc.
11. Frontend : CRUD Admins dans gestionutilisateur.html + admin.js
12. Frontend : Page Profil universelle (profilens.js + profilens.html)
13. Frontend : admin/profil.html → redirection
14. Frontend : HTML — data-user-*, #btn-logout, inclure shared.js, liens profil unifiés
15. Tests end-to-end
```

---

## Plan de tests

| Test | Attendu |
|---|---|
| Login ENSEIGNANT | → `enseignant/dashboardens.html`, permissions stockées |
| Login ADMIN | → `admin/tableaudebord.html` |
| Login ASSISTANT_PEDAGOGIQUE avec que COURSE_VIEW | Bouton "Créer séance" masqué, calendrier visible |
| Login ASSISTANT avec COURSE_MANAGE | Tous les boutons visibles |
| Accès `/admin/` sans session | → login |
| Accès `/admin/` avec session ENSEIGNANT | → son dashboard |
| Token expiré | refresh silencieux + retry |
| Mon profil depuis admin | → profilens.html avec sidebar admin |
| Mon profil depuis enseignant | profilens.html avec sidebar enseignant et champs grade/spécialité |
| Mon profil depuis assistant | profilens.html avec champ département/filières |
| Bouton Déconnexion sur profil | vide localStorage + redirect login |
| CRUD Admin | créer / modifier / bloquer / supprimer un compte admin |
| Topbar dynamique | Nom / prénom / initiales du compte connecté |
| Changement de mot de passe | Appel PUT `/api/utilisateurs/password` avec JWT |
