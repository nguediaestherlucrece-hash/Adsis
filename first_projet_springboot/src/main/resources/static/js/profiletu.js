/**
 * ADSIS — profiletu.js
 * Profil partagé étudiant : charge les infos depuis JWT + API et les bilan d'absences.
 * Ce script fonctionne pour la page shared/profil.html quand visitée par un étudiant.
 */
import { getUser } from './auth.js';

const user = getUser();

// ── Init topbar / avatar ──────────────────────────────────────────────────────
function initTopbar() {
    if (!user) return;
    const nameEl  = document.getElementById('user-name-top');
    const emailEl = document.getElementById('user-email-top');
    const avatarEl = document.getElementById('user-avatar');
    const dateEl  = document.getElementById('dashboardDate');
    const heroName  = document.getElementById('prof-name');
    const heroAvatar = document.getElementById('prof-avatar');

    if (nameEl)  nameEl.textContent  = `${user.prenom || ''} ${user.nom || ''}`.trim() || 'Utilisateur';
    if (emailEl) emailEl.textContent = user.email || '';
    if (avatarEl) {
        const init = (user.prenom?.[0] || user.nom?.[0] || '?').toUpperCase();
        avatarEl.textContent = init;
        // Couleur selon le rôle
        const roles = user.roles || [];
        const color = roles.includes('ETUDIANT') ? '#10b981'
                    : roles.includes('ENSEIGNANT') ? '#4e73df'
                    : '#6366f1';
        avatarEl.style.background = color;
    }
    if (dateEl) {
        dateEl.textContent = new Date().toLocaleDateString('fr-FR', {
            weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
        });
    }
    if (heroName) heroName.textContent = `${user.prenom || ''} ${user.nom || ''}`.trim();
    if (heroAvatar) heroAvatar.textContent = (user.prenom?.[0] || '?').toUpperCase();
}

// ── Remplir les champs du formulaire ─────────────────────────────────────────
function fillForm() {
    if (!user) return;
    const set = (id, val) => {
        const el = document.getElementById(id);
        if (el) el.value = val || '';
    };
    set('f-prenom',  user.prenom);
    set('f-nom',     user.nom);
    set('f-email',   user.email);
    set('f-matricule', user.matricule);
}

// ── Stats absences pour l'étudiant ────────────────────────────────────────────
async function loadStats() {
    if (!user) return;
    const roles = user.roles || [];
    if (!roles.includes('ETUDIANT')) return; // Pas d'appel stats pour non-étudiants

    const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
    try {
        const res = await fetch('/api/etudiant/absences/toutes', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) return;
        const absences = await res.json();

        const total = absences.length;
        const injust = absences.filter(a => !a.justifiee).length;

        const elTotal = document.getElementById('stat-absences');
        const elInj   = document.getElementById('stat-injust');
        if (elTotal) elTotal.textContent = total;
        if (elInj)   elInj.textContent   = injust;

        // Tentative de chargement des présences (via les stats dashboard)
        const res2 = await fetch('/api/etudiant/cours/a-venir', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        // On affiche juste un indicateur symbolique
    } catch (err) {
        console.warn('[profiletu] Stats non disponibles:', err);
    }
}

// ── Déconnexion ───────────────────────────────────────────────────────────────
document.getElementById('btn-logout')?.addEventListener('click', () => {
    localStorage.removeItem('jwt_token');
    sessionStorage.removeItem('jwt_token');
    localStorage.removeItem('adsis_user');
    window.location.href = '/login';
});

// ── Rôle badge dans le hero ───────────────────────────────────────────────────
function renderRoleBadge() {
    if (!user) return;
    const roles = user.roles || [];
    const heroRole = document.getElementById('prof-role-label');
    const heroClasse = document.getElementById('prof-classe');
    if (heroRole) {
        const label = roles.includes('ETUDIANT')            ? 'Étudiant'
                    : roles.includes('ENSEIGNANT')           ? 'Enseignant'
                    : roles.includes('ASSISTANT_PEDAGOGIQUE')? 'Assistant pédagogique'
                    : roles.includes('ADMIN')                ? 'Administrateur'
                    : 'Utilisateur';
        heroRole.textContent = label;
    }
    // Badge rôle container
    const badgeC = document.getElementById('role-badge-container');
    if (badgeC && roles.length > 0) {
        badgeC.innerHTML = roles.map(r =>
            `<span class="badge me-1" style="background:rgba(255,255,255,.2);color:#fff;font-size:.75rem;">${r.replace(/_/g,' ')}</span>`
        ).join('');
    }
    if (heroClasse) {
        heroClasse.textContent = user.classe || '';
    }
}

// ── Lancement ─────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    initTopbar();
    fillForm();
    renderRoleBadge();
    loadStats();
});
