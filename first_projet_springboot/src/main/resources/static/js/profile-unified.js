/* =====================================================================
   ADSIS – profile-unified.js
   Page profil universelle — Admin / Enseignant / Assistant Pédagogique
   Endpoints Spring Boot :
     GET/PUT /api/administrateurs/{id}          (ADMIN)
     GET/PUT /api/enseignants/{id}              (ADMIN | ENSEIGNANT)
     GET/PUT /api/assistants-pedagogiques/{id}  (ADMIN | ASSISTANT_PEDAGOGIQUE)
     POST /api/auth/change-password             via authFetch
   ===================================================================== */

import { getUser, logout, authFetch, getLoginPath } from './auth.js';

/* ── 0. Auth guard ──────────────────────────────────────────────────── */
const user = getUser();
if (!user) { window.location.href = getLoginPath(); }

const userRole = (user?.roles || [])[0] || '';
const userId   = user?.id;

/* ── 1. Sidebar navigation dynamique selon le rôle ─────────────────── */
const NAV_CONFIG = {
  ADMIN: [
    {
      label: 'Tableau de bord',
      href: '/admin/tableaudebord',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
               <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
             </svg>`,
    },
    {
      label: 'Gestion du Staff',
      href: '/admin/gestionutilisateur',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
               <circle cx="9" cy="7" r="4"/>
               <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
             </svg>`,
    },
    {
      label: 'Gestion des accès',
      href: '/admin/gestionacces',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <rect x="3" y="11" width="18" height="11" rx="2"/>
               <path d="M7 11V7a5 5 0 0 1 10 0v4"/>
             </svg>`,
    },
    {
      label: 'Cycles & Filières',
      href: '/admin/cycles',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <circle cx="12" cy="5" r="3"/><circle cx="5" cy="19" r="3"/><circle cx="19" cy="19" r="3"/>
               <line x1="12" y1="8" x2="5" y2="16"/><line x1="12" y1="8" x2="19" y2="16"/>
             </svg>`,
    },
    {
      label: 'Mon profil',
      href: '#',
      active: true,
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
               <circle cx="12" cy="7" r="4"/>
             </svg>`,
    },
  ],
  ENSEIGNANT: [
    {
      label: 'Tableau de bord',
      href: '/enseignant/dashboard',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
               <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
             </svg>`,
    },
    {
      label: 'Emploi de temps',
      href: '/enseignant/emploi',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <rect x="3" y="4" width="18" height="18" rx="2"/>
               <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
               <line x1="3" y1="10" x2="21" y2="10"/>
             </svg>`,
    },
    {
      label: 'Appel',
      href: '/enseignant/appel',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M9 11l3 3L22 4"/>
               <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
             </svg>`,
    },
    {
      label: 'Mon profil',
      href: '#',
      active: true,
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
               <circle cx="12" cy="7" r="4"/>
             </svg>`,
    },
  ],
  ASSISTANT_PEDAGOGIQUE: [
    {
      label: 'Tableau de bord',
      href: '/assistant/tableaudebord',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <rect x="3" y="3" width="7" height="7"/><rect x="14" y="3" width="7" height="7"/>
               <rect x="3" y="14" width="7" height="7"/><rect x="14" y="14" width="7" height="7"/>
             </svg>`,
    },
    {
      label: 'Matières',
      href: '/assistant/matiere',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M4 19.5A2.5 2.5 0 0 1 6.5 17H20"/>
               <path d="M6.5 2H20v20H6.5A2.5 2.5 0 0 1 4 19.5v-15A2.5 2.5 0 0 1 6.5 2z"/>
             </svg>`,
    },
    {
      label: 'Étudiants',
      href: '/assistant/gestionetudiant',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"/>
               <circle cx="9" cy="7" r="4"/>
               <path d="M23 21v-2a4 4 0 0 0-3-3.87M16 3.13a4 4 0 0 1 0 7.75"/>
             </svg>`,
    },
    {
      label: 'Emploi du temps',
      href: '/assistant/emploidutemps',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <rect x="3" y="4" width="18" height="18" rx="2"/>
               <line x1="16" y1="2" x2="16" y2="6"/><line x1="8" y1="2" x2="8" y2="6"/>
               <line x1="3" y1="10" x2="21" y2="10"/>
             </svg>`,
    },
    {
      label: 'Mon profil',
      href: '#',
      active: true,
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
               <circle cx="12" cy="7" r="4"/>
             </svg>`,
    },
  ],
};

NAV_CONFIG.SURVEILLANT = [
    ...NAV_CONFIG.ASSISTANT_PEDAGOGIQUE.filter(item => item.label !== 'Mon profil'),
    {
      label: 'Appel',
      href: '/assistant/appel',
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M9 11l3 3L22 4"/>
               <path d="M21 12v7a2 2 0 0 1-2 2H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11"/>
             </svg>`,
    },
    {
      label: 'Mon profil',
      href: '#',
      active: true,
      icon: `<svg width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
               <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2"/>
               <circle cx="12" cy="7" r="4"/>
             </svg>`,
    },
];

// ETUDIANT — redirige vers son propre espace
NAV_CONFIG.ETUDIANT = [
    { label: 'Tableau de bord',  href: '/etudiant/dashboard',     icon: `<i class="bi bi-speedometer2"></i>` },
    { label: 'Emploi du temps', href: '/etudiant/emploi',         icon: `<i class="bi bi-calendar-week"></i>` },
    { label: 'Répondre à l\'appel', href: '/etudiant/appel',       icon: `<i class="bi bi-qr-code-scan"></i>` },
    { label: 'Mes absences',    href: '/etudiant/absences',       icon: `<i class="bi bi-exclamation-triangle"></i>` },
    { label: 'Justificatifs',   href: '/etudiant/justifications', icon: `<i class="bi bi-file-earmark-text"></i>` },
    { label: 'Mon profil',      href: '#', active: true,          icon: `<i class="bi bi-person-circle"></i>` },
];

/* ── 2. Injecter la navigation ──────────────────────────────────────── */
function buildSidebar() {
  const navEl = document.getElementById('sidebar-nav');
  if (!navEl) return;
  const items = NAV_CONFIG[userRole] || [];
  navEl.innerHTML = items.map(item => `
    <a href="${item.href}" class="nav-item${item.active ? ' active' : ''}">
      ${item.icon}
      <span>${item.label}</span>
    </a>
  `).join('');
}

buildSidebar();

/* ── 3. Remplir topbar & hero ───────────────────────────────────────── */
const fullName = `${user.prenom || ''} ${user.nom || ''}`.trim() || 'Utilisateur';
const initials = [user.prenom, user.nom].map(s => s?.[0] || '').join('').toUpperCase() || 'U';

const elNameTop   = document.getElementById('user-name-top');
const elEmailTop  = document.getElementById('user-email-top');
const elAvatar    = document.getElementById('user-avatar');
const elProfName  = document.getElementById('prof-name');
const elProfAvatar = document.getElementById('prof-avatar');
const elRoleLabel = document.getElementById('prof-role-label');

if (elNameTop)   elNameTop.textContent  = fullName;
if (elEmailTop)  elEmailTop.textContent = user.email || '';
if (elAvatar)    elAvatar.textContent   = initials;
if (elProfName)  elProfName.textContent = fullName;
if (elProfAvatar) elProfAvatar.textContent = initials;

/* ── 4. Badge de rôle ───────────────────────────────────────────────── */
const ROLE_CONFIG = {
  ADMIN:                 { label: 'Administrateur',        color: '#6366f1', roleLabel: 'ADMINISTRATEUR' },
  ENSEIGNANT:            { label: 'Enseignant',            color: '#3B55E6', roleLabel: 'ENSEIGNANT'     },
  ASSISTANT_PEDAGOGIQUE: { label: 'Assistant Pédagogique', color: '#10b981', roleLabel: 'ASSISTANT PÉDAGOGIQUE' },
  SURVEILLANT:           { label: 'Surveillant',           color: '#f59e0b', roleLabel: 'SURVEILLANT'    },
  ETUDIANT:              { label: 'Étudiant',              color: '#10b981', roleLabel: 'ÉTUDIANT'       },
};

const roleCfg = ROLE_CONFIG[userRole];
if (roleCfg) {
  if (elRoleLabel) elRoleLabel.textContent = roleCfg.roleLabel;
  const badgeContainer = document.getElementById('role-badge-container');
  if (badgeContainer) {
    badgeContainer.innerHTML = `
      <span class="prof-role-badge"
            style="background:${roleCfg.color}20;color:${roleCfg.color};">
        ${roleCfg.label}
      </span>`;
  }
}

/* ── 5. Pré-remplir les champs depuis localStorage ──────────────────── */
const fPrenom  = document.getElementById('f-prenom');
const fNom     = document.getElementById('f-nom');
const fEmail   = document.getElementById('f-email');

if (fPrenom) fPrenom.value = user.prenom || '';
if (fNom)    fNom.value    = user.nom    || '';
if (fEmail)  fEmail.value  = user.email  || '';

/* ── 6. Charger les données spécifiques depuis l'API ────────────────── */
const API_ENDPOINTS = {
  ADMIN:                 `/api/administrateurs/${userId}`,
  ENSEIGNANT:            `/api/enseignants/${userId}`,
  ASSISTANT_PEDAGOGIQUE: `/api/assistants-pedagogiques/${userId}`,
  SURVEILLANT:           `/api/assistants-pedagogiques/${userId}`,
  ETUDIANT:              `/api/etudiants/${userId}`,
};

async function loadProfileData() {
  if (!userId) return;
  const ep = API_ENDPOINTS[userRole];
  if (!ep) return;
  try {
    const res = await authFetch(ep);
    if (!res || !res.ok) return;
    const data = await res.json();

    // Champs communs présents dans les DTOs (prenom, nom, email)
    if (fPrenom && data.prenom) fPrenom.value = data.prenom;
    if (fNom    && data.nom)    fNom.value    = data.nom;
    if (fEmail  && data.email)  fEmail.value  = data.email;

    // Champs tel / adresse si présents
    const fTel    = document.getElementById('f-tel');
    const fAdresse = document.getElementById('f-adresse');
    if (fTel    && data.telephone) fTel.value    = data.telephone;
    if (fAdresse && data.adresse)  fAdresse.value = data.adresse;

    // Rafraîchir le nom affiché
    const nom = `${data.prenom || ''} ${data.nom || ''}`.trim();
    if (elProfName) elProfName.textContent = nom;
    if (elNameTop)  elNameTop.textContent  = nom;

    const ini = [data.prenom, data.nom].map(s => s?.[0] || '').join('').toUpperCase() || 'U';
    if (elAvatar)     elAvatar.textContent     = ini;
    if (elProfAvatar) elProfAvatar.textContent = ini;

  } catch (e) {
    console.warn('[Profil] Données API non chargées :', e.message);
  }
}

loadProfileData();

/* ── 7. Toast helper ────────────────────────────────────────────────── */
function showToast(msg, type = 'success') {
  const toast = document.getElementById('toast');
  if (!toast) return;
  toast.textContent = msg;
  toast.className   = `toast ${type === 'success' ? 'success-toast' : 'error-toast'} show`;
  clearTimeout(toast._timer);
  toast._timer = setTimeout(() => {
    toast.className = 'toast';
  }, 3500);
}

/* ── 8. Sauvegarde du profil ─────────────────────────────────────────── */
document.getElementById('btn-save')?.addEventListener('click', async () => {
  const prenom   = fPrenom?.value.trim();
  const nom      = fNom?.value.trim();
  const email    = fEmail?.value.trim();
  const tel      = document.getElementById('f-tel')?.value.trim();
  const adresse  = document.getElementById('f-adresse')?.value.trim();
  const feedback = document.getElementById('save-feedback');
  const btn      = document.getElementById('btn-save');

  if (!prenom || !nom || !email) {
    showToast('Prénom, nom et email sont obligatoires.', 'error');
    if (feedback) { feedback.textContent = 'Champs obligatoires manquants.'; feedback.className = 'save-feedback error'; }
    return;
  }

  const ep = API_ENDPOINTS[userRole];
  if (!ep) { showToast('Rôle non reconnu.', 'error'); return; }

  // Construction du payload selon le rôle
  // Les DTOs UpdateDTO acceptent au minimum nom, prenom, email
  const payload = { nom, prenom, email };
  if (tel)     payload.telephone = tel;
  if (adresse) payload.adresse   = adresse;

  btn.disabled   = true;
  btn.textContent = 'Enregistrement…';

  try {
    const res = await authFetch(ep, {
      method: 'PUT',
      body: JSON.stringify(payload),
    });

    if (!res || !res.ok) {
      const errText = await res?.text();
      throw new Error(errText || 'Erreur serveur');
    }

    // Mettre à jour le localStorage
    const updated = { ...user, nom, prenom, email };
    localStorage.setItem('adsis_user', JSON.stringify(updated));

    // Rafraîchir l'affichage
    const newName = `${prenom} ${nom}`;
    if (elProfName) elProfName.textContent = newName;
    if (elNameTop)  elNameTop.textContent  = newName;
    const ini = [prenom, nom].map(s => s?.[0] || '').join('').toUpperCase();
    if (elAvatar)     elAvatar.textContent     = ini;
    if (elProfAvatar) elProfAvatar.textContent = ini;

    if (feedback) { feedback.textContent = 'Modifications enregistrées !'; feedback.className = 'save-feedback success'; }
    showToast('Profil mis à jour avec succès !', 'success');

  } catch (err) {
    if (feedback) { feedback.textContent = 'Erreur : ' + err.message; feedback.className = 'save-feedback error'; }
    showToast('Erreur lors de la sauvegarde', 'error');
  } finally {
    btn.disabled = false;
    btn.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="15" height="15">
      <path d="M19 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h11l5 5v11a2 2 0 0 1-2 2z"/>
      <polyline points="17 21 17 13 7 13 7 21"/>
      <polyline points="7 3 7 8 15 8"/>
    </svg> Enregistrer les modifications`;
  }
});

/* ── 9. Changement de mot de passe ──────────────────────────────────── */
document.getElementById('btn-change-pw')?.addEventListener('click', async () => {
  const cur    = document.getElementById('f-pw-cur')?.value;
  const nw     = document.getElementById('f-pw-new')?.value;
  const conf   = document.getElementById('f-pw-conf')?.value;
  const pwFb   = document.getElementById('pw-feedback');
  const btn    = document.getElementById('btn-change-pw');

  const setFb = (msg, type) => {
    if (pwFb) { pwFb.textContent = msg; pwFb.className = `save-feedback ${type}`; }
  };

  if (!cur || !nw || !conf) { showToast('Remplissez tous les champs.', 'error'); setFb('Tous les champs sont requis.', 'error'); return; }
  if (nw.length < 6)         { showToast('6 caractères minimum.', 'error'); setFb('Le mot de passe doit contenir au moins 6 caractères.', 'error'); return; }
  if (nw !== conf)            { showToast('Les mots de passe ne correspondent pas.', 'error'); setFb('Les mots de passe ne correspondent pas.', 'error'); return; }

  btn.disabled = true;

  try {
    // Endpoint change-password (via authFetch — intercepteur global)
    const res = await authFetch('/api/auth/change-password', {
      method: 'POST',
      body: JSON.stringify({ ancienMotDePasse: cur, nouveauMotDePasse: nw }),
    });

    if (!res || !res.ok) {
      const errText = await res?.text();
      throw new Error(errText || 'Erreur serveur');
    }

    showToast('Mot de passe modifié avec succès !', 'success');
    setFb('Mot de passe mis à jour.', 'success');
    ['f-pw-cur', 'f-pw-new', 'f-pw-conf'].forEach(id => {
      const el = document.getElementById(id);
      if (el) el.value = '';
    });

  } catch (err) {
    showToast('Erreur : ' + err.message, 'error');
    setFb('Erreur : ' + err.message, 'error');
  } finally {
    btn.disabled = false;
  }
});

/* ── 10. Déconnexion avec modal de confirmation ──────────────────────── */
const logoutModal  = document.getElementById('logout-modal');
const logoutBtn    = document.getElementById('btn-logout');
const logoutCancel = document.getElementById('logout-cancel');
const logoutConfirm = document.getElementById('logout-confirm');

logoutBtn?.addEventListener('click', () => {
  if (logoutModal) logoutModal.classList.add('open');
});

logoutCancel?.addEventListener('click', () => {
  if (logoutModal) logoutModal.classList.remove('open');
});

logoutConfirm?.addEventListener('click', () => {
  logout();
});

// Fermer la modal si clic sur l'overlay
logoutModal?.addEventListener('click', (e) => {
  if (e.target === logoutModal) logoutModal.classList.remove('open');
});
