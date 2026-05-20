/**
 * ADSIS — dashboardetu.js
 * Dashboard étudiant — données dynamiques via API + guard rôle + offline
 */
import { requireRole, getUser } from './auth.js';

// ── Guard de sécurité ───────────────────────────────────────────────────────
if (!requireRole(['ETUDIANT'])) throw new Error('Accès refusé');

// ── Init utilisateur ─────────────────────────────────────────────────────────
const user = getUser();
if (user) {
    const nameEl   = document.getElementById('user-name-top');
    const emailEl  = document.getElementById('user-email-top');
    const avatarEl = document.getElementById('user-avatar');
    const dateEl   = document.getElementById('dashboardDate');
    if (nameEl)   nameEl.textContent   = `${user.prenom} ${user.nom}`;
    if (emailEl)  emailEl.textContent  = user.email || '';
    if (avatarEl) avatarEl.textContent = (user.prenom?.[0] || 'E').toUpperCase();
    if (dateEl) {
        const opts = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
        dateEl.textContent = new Date().toLocaleDateString('fr-FR', opts);
    }
}

const CACHE_KEY_COURS    = 'etu_prochains_cours';
const CACHE_KEY_ABSENCES = 'etu_absences_recentes';

// ── Helper JWT ───────────────────────────────────────────────────────────────
function authHeader() {
    const t = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
    return t ? { 'Authorization': `Bearer ${t}` } : {};
}

// ── Prochains cours ──────────────────────────────────────────────────────────
async function loadProchainsCours() {
    const container = document.getElementById('cours-list');
    if (!container) return;

    // Afficher le cache immédiatement si dispo (offline-first)
    const cached = sessionStorage.getItem(CACHE_KEY_COURS);
    if (cached) renderCours(JSON.parse(cached), container);

    try {
        const res = await fetch('/api/etudiant/cours/a-venir', { headers: authHeader() });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const cours = await res.json();
        sessionStorage.setItem(CACHE_KEY_COURS, JSON.stringify(cours));
        renderCours(cours, container);
        // Mettre à jour la stat topbar
        const statEl = document.getElementById('stat-prochain-cours');
        if (statEl && cours.length > 0) statEl.textContent = cours[0].matiere || '—';
    } catch (err) {
        console.warn('[dashboard] Cours hors-ligne ou erreur:', err);
        if (!cached) {
            container.innerHTML = '<li class="list-group-item text-warning"><i class="bi bi-wifi-off me-2"></i>Données indisponibles hors ligne</li>';
        }
    }
}

function renderCours(cours, container) {
    if (!cours || cours.length === 0) {
        container.innerHTML = '<li class="list-group-item text-muted">Aucun cours à venir cette semaine</li>';
        return;
    }
    container.innerHTML = cours.map(c => `
        <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
                <strong>${c.matiere || '—'}</strong><br>
                <small class="text-muted">
                    ${c.jour || ''} ${c.date ? new Date(c.date).toLocaleDateString('fr-FR', {day:'2-digit', month:'short'}) : ''}
                    — ${c.heureDebut || ''} à ${c.heureFin || ''}
                </small><br>
                <small>${c.enseignant ? '👤 ' + c.enseignant : ''}</small>
            </div>
            <span class="badge bg-primary rounded-pill">${c.salle || '—'}</span>
        </li>
    `).join('');
}

// ── Absences récentes ─────────────────────────────────────────────────────────
async function loadAbsencesRecentes() {
    const container = document.getElementById('absences-list');
    if (!container) return;

    const cached = sessionStorage.getItem(CACHE_KEY_ABSENCES);
    if (cached) renderAbsences(JSON.parse(cached), container);

    try {
        const res = await fetch('/api/etudiant/absences?limite=5', { headers: authHeader() });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const absences = await res.json();
        sessionStorage.setItem(CACHE_KEY_ABSENCES, JSON.stringify(absences));
        renderAbsences(absences, container);

        // Compteurs — EN_ATTENTE n'est pas "non justifiée" (dossier en cours)
        const total = document.getElementById('stat-total-absences');
        const inj   = document.getElementById('stat-injustifiees');
        if (total) total.textContent = absences.length;
        if (inj)   inj.textContent   = absences.filter(
            a => !a.justificatifStatut || a.justificatifStatut === 'REJETE'
        ).length;
    } catch (err) {
        console.warn('[dashboard] Absences hors-ligne ou erreur:', err);
        if (!cached) {
            container.innerHTML = '<li class="list-group-item text-warning"><i class="bi bi-wifi-off me-2"></i>Données indisponibles hors ligne</li>';
        }
    }
}

function renderAbsences(absences, container) {
    if (!absences || absences.length === 0) {
        container.innerHTML = '<li class="list-group-item text-muted"><i class="bi bi-check-circle text-success me-2"></i>Aucune absence enregistrée</li>';
        return;
    }
    container.innerHTML = absences.map(a => {
        let badgeClass, badgeLabel;
        if (a.justificatifStatut === 'ACCEPTE') {
            badgeClass = 'bg-success';        badgeLabel = 'Justifiée';
        } else if (a.justificatifStatut === 'EN_ATTENTE') {
            badgeClass = 'bg-info text-dark'; badgeLabel = '⏳ En attente';
        } else if (a.justificatifStatut === 'REJETE') {
            badgeClass = 'bg-warning text-dark'; badgeLabel = 'Rejetée';
        } else {
            badgeClass = 'bg-danger';         badgeLabel = 'Non justifiée';
        }
        return `
        <li class="list-group-item d-flex justify-content-between align-items-center">
            <div>
                <strong>${a.matiere || '—'}</strong><br>
                <small class="text-muted">${a.date ? new Date(a.date).toLocaleDateString('fr-FR') : '—'}</small>
            </div>
            <span class="badge ${badgeClass} rounded-pill">${badgeLabel}</span>
        </li>`;
    }).join('');
}

// ── Lancement ─────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadProchainsCours();
    loadAbsencesRecentes();
});