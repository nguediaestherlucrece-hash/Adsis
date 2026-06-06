/**
 * ADSIS — absences.js
 * Page absences étudiant — toutes les absences avec statut et actions
 */
import { requireRole, getUser } from './auth.js';

if (!requireRole(['ETUDIANT'])) throw new Error('Accès refusé');

const user = getUser();
if (user) {
    const n = document.getElementById('user-name-top');
    const e = document.getElementById('user-email-top');
    const a = document.getElementById('user-avatar');
    if (n) n.textContent = `${user.prenom} ${user.nom}`;
    if (e) e.textContent = user.email || '';
    if (a) a.textContent = (user.prenom?.[0] || 'E').toUpperCase();
}

const CACHE_KEY = 'etu_absences_toutes';

async function loadAbsences() {
    const tbody = document.getElementById('absences-body');
    if (!tbody) return;
    const cached = localStorage.getItem(CACHE_KEY);
    if (cached) renderAbsences(JSON.parse(cached), tbody);
    try {
        const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
        const res = await fetch('/api/etudiant/absences/toutes', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        localStorage.setItem(CACHE_KEY, JSON.stringify(data));
        renderAbsences(data, tbody);
        updateCompteurs(data);
    } catch (err) {
        console.warn('[absences] Erreur:', err);
        if (!cached) {
            tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-warning"><i class="bi bi-wifi-off me-2"></i>Données hors ligne indisponibles</td></tr>';
        }
    }
}

function renderAbsences(absences, tbody) {
    if (!absences || absences.length === 0) {
        tbody.innerHTML = '<tr><td colspan="6" class="text-center py-5 text-muted"><i class="bi bi-check-circle-fill text-success fs-3 d-block mb-2"></i>Aucune absence enregistrée</td></tr>';
        return;
    }
    tbody.innerHTML = absences.map(a => {
        const date    = a.date ? new Date(a.date).toLocaleDateString('fr-FR', {day:'2-digit',month:'short',year:'numeric'}) : '—';
        const horaire = a.heureDebut ? `${a.heureDebut}–${a.heureFin || '?'}` : '—';

        // Précision sur les heures : si des créneaux existent, on les affiche
        let heuresPrecises = '';
        if (a.heuresAbsentes > 0 || a.heuresPresentes > 0) {
            if (a.statut === 'présent_partiel') {
                heuresPrecises = `<br><span class="text-danger small fw-semibold">${a.heuresAbsentes}h absent${a.heuresAbsentes > 1 ? 'es' : ''}</span>`
                               + ` <span class="text-muted small">/ ${a.heuresPresentes}h présent${a.heuresPresentes > 1 ? 'es' : ''}</span>`;
            } else {
                heuresPrecises = `<br><span class="text-danger small fw-semibold">${a.heuresAbsentes}h absent${a.heuresAbsentes > 1 ? 'es' : ''}</span>`;
            }
        }

        // Badge selon statut réel
        let statutBadge;
        if (a.statut === 'retard') {
            statutBadge = '<span class="badge bg-warning text-dark">Retard</span>';
        } else if (a.statut === 'présent_partiel') {
            statutBadge = '<span class="badge bg-orange text-white" style="background:#e67e22">Partiel</span>';
        } else {
            statutBadge = '<span class="badge bg-danger">Absent</span>';
        }

        // Badge justificatif basé sur justificatifStatut (plus précis que justifiee)
        let justBadge, action;
        if (a.justificatifStatut === 'ACCEPTE') {
            justBadge = '<span class="badge bg-success">Justifiée</span>';
            action    = '<span class="text-success small"><i class="bi bi-check-circle me-1"></i>Traitée</span>';
        } else if (a.justificatifStatut === 'EN_ATTENTE') {
            justBadge = '<span class="badge bg-info text-dark">⏳ En attente</span>';
            action    = '<span class="text-info small"><i class="bi bi-hourglass-split me-1"></i>Justificatif en cours</span>';
        } else if (a.justificatifStatut === 'REJETE') {
            justBadge = '<span class="badge bg-danger">Rejetée</span>';
            action    = `<a href="/etudiant/justifications?id=${a.id}" class="btn btn-sm btn-outline-warning"><i class="bi bi-arrow-repeat"></i> Re-soumettre</a>`;
        } else {
            justBadge = '<span class="badge bg-secondary">Non justifiée</span>';
            action    = `<a href="/etudiant/justifications?id=${a.id}" class="btn btn-sm btn-outline-primary"><i class="bi bi-file-earmark-plus"></i> Justifier</a>`;
        }
        return `<tr><td>${date}</td><td><strong>${a.matiere || '—'}</strong></td><td><small>${horaire}${heuresPrecises}</small></td><td>${statutBadge}</td><td>${justBadge}</td><td>${action}</td></tr>`;
    }).join('');
}

function updateCompteurs(abs) {
    const total = abs.length;
    const inj   = abs.filter(a => !a.justifiee).length;
    const just  = abs.filter(a => a.justifiee).length;
    const t = document.getElementById('cnt-total');
    const i = document.getElementById('cnt-injustifiees');
    const j = document.getElementById('cnt-justifiees');
    if (t) t.textContent = total;
    if (i) i.textContent = inj;
    if (j) j.textContent = just;
}

document.addEventListener('DOMContentLoaded', loadAbsences);