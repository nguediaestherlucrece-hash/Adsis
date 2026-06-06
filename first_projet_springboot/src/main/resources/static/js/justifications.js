/**
 * ADSIS — justifications.js
 * Page justifications étudiant — soumettre une justification + historique
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

// Pré-sélection via URL ?id=XX
const urlParams = new URLSearchParams(window.location.search);
const preselectedId = urlParams.get('id');

// ── Charger absences non justifiées dans le select ────────────────────────────
async function loadAbsencesNonJustifiees() {
    const select = document.getElementById('absence-select');
    if (!select) return;
    select.innerHTML = '<option value="">Chargement…</option>';
    try {
        const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
        const res = await fetch('/api/etudiant/absences/non-justifiees', {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const absences = await res.json();
        if (absences.length === 0) {
            select.innerHTML = '<option value="">Aucune absence à justifier</option>';
            select.disabled = true;
            document.getElementById('btn-submit').disabled = true;
            return;
        }
        select.innerHTML = '<option value="">— Sélectionnez une absence —</option>' +
            absences.map(a => {
                const date       = a.date ? new Date(a.date).toLocaleDateString('fr-FR') : '?';
                const matiere    = a.matiere || '—';
                const selected   = preselectedId && String(a.id) === String(preselectedId) ? 'selected' : '';

                // Détail des heures précises
                let heuresInfo = '';
                if (a.heuresAbsentes > 0) {
                    if (a.statut === 'présent_partiel') {
                        const plage = (a.heureDebutSeance && a.heureFinSeance)
                            ? ` (séance ${a.heureDebutSeance}–${a.heureFinSeance})`
                            : '';
                        heuresInfo = ` — ${a.heuresAbsentes}h abs${plage}`;
                    } else {
                        heuresInfo = ` — ${a.heuresAbsentes}h`;
                    }
                }

                const label = `${matiere} (${date})${heuresInfo}`;

                // Si un justificatif est EN_ATTENTE, l'option est disabled
                if (a.justificatifStatut === 'EN_ATTENTE') {
                    return `<option value="${a.id}" disabled ${selected}>⏳ En attente — ${label}</option>`;
                }
                if (a.justificatifStatut === 'REJETE') {
                    return `<option value="${a.id}" ${selected}>⚠️ Rejeté — résoumettre — ${label}</option>`;
                }
                return `<option value="${a.id}" ${selected}>${label}</option>`;
            }).join('');
    } catch (err) {
        console.warn('[justifications] Erreur chargement absences:', err);
        select.innerHTML = '<option value="">⚠ Hors ligne — réessayez plus tard</option>';
    }
}

// ── Charger l'historique des justificatifs ────────────────────────────────────
async function loadHistorique() {
    const list = document.getElementById('historique-list');
    if (!list || !user) return;
    try {
        const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
        const res = await fetch(`/api/justificatifs/my-justificatifs?etudiantId=${user.id}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const data = await res.json();
        const items = data.justificatifs || data.content || data || [];
        if (items.length === 0) {
            list.innerHTML = '<li class="list-group-item text-muted text-center py-3">Aucun justificatif soumis</li>';
            return;
        }
        const statutConfig = {
            'EN_ATTENTE': { badge: 'warning',   label: 'En attente', icon: 'hourglass-split' },
            'ACCEPTE':    { badge: 'success',   label: 'Accepté',    icon: 'check-circle'   },
            'REJETE':     { badge: 'danger',    label: 'Rejeté',     icon: 'x-circle'       },
        };
        list.innerHTML = items.map(j => {
            const cfg  = statutConfig[j.statutValidation] || { badge: 'secondary', label: j.statutValidation, icon: 'question' };
            const date = j.dateSoumission ? new Date(j.dateSoumission).toLocaleDateString('fr-FR') : '—';
            const matiere = j.coursNom || '—';

            // Informations sur la séance + heures absentes
            let seanceInfo = '';
            if (j.seanceDate) {
                const sd = new Date(j.seanceDate).toLocaleDateString('fr-FR');
                seanceInfo = `<div class="text-muted" style="font-size:.78rem;">Séance du ${sd}`;
                if (j.seanceHeureDebut) seanceInfo += ` ${j.seanceHeureDebut}–${j.seanceHeureFin || '?'}`;
                if (j.heuresAbsentes > 0) {
                    seanceInfo += ` — <span class="text-danger fw-semibold">${j.heuresAbsentes}h absente${j.heuresAbsentes > 1 ? 's' : ''}</span>`;
                    if (j.heuresPresentes > 0) {
                        seanceInfo += ` / ${j.heuresPresentes}h présente${j.heuresPresentes > 1 ? 's' : ''}`;
                    }
                }
                seanceInfo += '</div>';
            }

            return `
            <li class="list-group-item">
                <div class="d-flex justify-content-between align-items-start">
                    <div>
                        <div class="fw-semibold small">${matiere}</div>
                        ${seanceInfo}
                        <div class="text-muted" style="font-size:.78rem;">Soumis le ${date}</div>
                        <div class="text-muted" style="font-size:.78rem;">${(j.motif || '').substring(0,60)}${j.motif && j.motif.length > 60 ? '…' : ''}</div>
                    </div>
                    <span class="badge bg-${cfg.badge}"><i class="bi bi-${cfg.icon} me-1"></i>${cfg.label}</span>
                </div>
            </li>`;
        }).join('');
    } catch (err) {
        console.warn('[justifications] Erreur historique:', err);
        const list2 = document.getElementById('historique-list');
        if (list2) list2.innerHTML = '<li class="list-group-item text-warning text-center py-2"><i class="bi bi-wifi-off me-2"></i>Hors ligne</li>';
    }
}

// ── Soumission du formulaire ──────────────────────────────────────────────────
document.getElementById('justification-form')?.addEventListener('submit', async (e) => {
    e.preventDefault();
    const msgDiv    = document.getElementById('form-message');
    const absenceId = document.getElementById('absence-select').value;
    const motif     = document.getElementById('motif').value.trim();
    const fichier   = document.getElementById('piece-jointe')?.files?.[0];
    const btn       = document.getElementById('btn-submit');

    if (!absenceId) {
        msgDiv.innerHTML = '<div class="alert alert-warning">Veuillez sélectionner une absence.</div>';
        return;
    }
    if (!motif) {
        msgDiv.innerHTML = '<div class="alert alert-warning">Veuillez saisir un motif.</div>';
        return;
    }
    if (!user?.id) {
        msgDiv.innerHTML = '<div class="alert alert-danger">Session expirée. Reconnectez-vous.</div>';
        return;
    }

    btn.disabled = true;
    btn.innerHTML = '<span class="spinner-border spinner-border-sm me-2"></span>Envoi…';

    try {
        const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
        const formData = new FormData();
        formData.append('presenceId', absenceId);
        formData.append('motif', motif);
        // Le champ s'appelle 'document' dans CreateJustificatifRequest (@ModelAttribute)
        if (fichier) formData.append('document', fichier);

        const res = await fetch(`/api/justificatifs/submit?etudiantId=${user.id}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${token}` },
            body: formData,
        });

        if (res.ok) {
            msgDiv.innerHTML = '<div class="alert alert-success"><i class="bi bi-check-circle me-2"></i>Justification envoyée. Un responsable la traitera sous peu.</div>';
            document.getElementById('justification-form').reset();
            // Rafraîchir les deux listes
            await loadAbsencesNonJustifiees();
            await loadHistorique();
        } else {
            const err = await res.text();
            msgDiv.innerHTML = `<div class="alert alert-danger"><i class="bi bi-x-circle me-2"></i>Erreur : ${err}</div>`;
        }
    } catch (err) {
        console.error('[justifications] Erreur soumission:', err);
        msgDiv.innerHTML = '<div class="alert alert-warning"><i class="bi bi-wifi-off me-2"></i>Hors ligne. Réessayez une fois connecté.</div>';
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="bi bi-send me-2"></i>Envoyer la justification';
    }
});

// ── Lancement ─────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    loadAbsencesNonJustifiees();
    loadHistorique();
});