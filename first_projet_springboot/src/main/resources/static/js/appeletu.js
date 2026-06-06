/**
 * ADSIS — appeletu.js
 * Page appel étudiant : cours du jour, QR scan, saisie PIN, résumé détaillé
 *
 * Refonte v2 :
 *  - Polling API toutes les 30s (GET /api/appels-etudiant/statut-seance/{id})
 *  - Blocage boutons PAR APPEL (pas par séance) — l'étudiant peut répondre au 2e appel
 *  - Affichage détaillé du statut après réponse (retard, heures absentes/présentes)
 *  - sessionStorage conservé uniquement comme indicateur informatif (pas de verrou)
 */
import { requireRole, getUser } from './auth.js';

if (!requireRole(['ETUDIANT'])) throw new Error('Accès refusé');

const user = getUser();
let qrScanner = null;
let currentSeanceId = null;
let pollingInterval = null;

// ── sessionStorage : heure de la dernière réponse par séance (informatif uniquement) ──
const TODAY = new Date().toISOString().split('T')[0];
function _markedKey() { return `adsis_marked_${TODAY}`; }
function getMarkedSeances() {
    try { return JSON.parse(sessionStorage.getItem(_markedKey()) || '{}'); } catch { return {}; }
}
function setDerniereReponse(seanceId, heureISO) {
    const m = getMarkedSeances();
    m[String(seanceId)] = heureISO;
    sessionStorage.setItem(_markedKey(), JSON.stringify(m));
}
function getDerniereReponse(seanceId) {
    return getMarkedSeances()[String(seanceId)] || null;
}

// ── Init topbar ───────────────────────────────────────────────────────────────
if (user) {
    const n = document.getElementById('user-name-top');
    const e = document.getElementById('user-email-top');
    const a = document.getElementById('user-avatar');
    if (n) n.textContent = `${user.prenom} ${user.nom}`;
    if (e) e.textContent = user.email || '';
    if (a) a.textContent = (user.prenom?.[0] || 'E').toUpperCase();
}

// ── Date du jour ──────────────────────────────────────────────────────────────
const jourEl = document.getElementById('date-jour');
if (jourEl) {
    jourEl.textContent = new Date().toLocaleDateString('fr-FR', {
        weekday: 'long', year: 'numeric', month: 'long', day: 'numeric'
    });
}

// ── Helpers ───────────────────────────────────────────────────────────────────
function getToken() {
    return localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
}
function authHeaders(extra = {}) {
    return { 'Authorization': `Bearer ${getToken()}`, 'Content-Type': 'application/json', ...extra };
}
function localDateString(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

const urlSeanceId = new URLSearchParams(window.location.search).get('seanceId');

function isSessionActive(heureDebut, heureFin) {
    if (!heureDebut || !heureFin) return false;
    const now = new Date();
    const nowMin = now.getHours() * 60 + now.getMinutes();
    const toMin = t => { const p = t.split(':').map(Number); return p[0] * 60 + (p[1] || 0); };
    return nowMin >= toMin(heureDebut) - 15 && nowMin <= toMin(heureFin) + 15;
}

function showMsg(msg, type = 'success') {
    const div = document.getElementById('appel-message');
    if (!div) return;
    div.className = `alert alert-${type}`;
    div.innerHTML = `<i class="bi bi-${type === 'success' ? 'check-circle' : 'x-circle'} me-2"></i>${msg}`;
    div.classList.remove('d-none');
    setTimeout(() => div.classList.add('d-none'), 4000);
}

// ── Polling ───────────────────────────────────────────────────────────────────
function startPolling(seancesIds) {
    stopPolling();
    if (!seancesIds || seancesIds.length === 0) return;
    pollingInterval = setInterval(async () => {
        // Rafraîchir uniquement si on est sur la page
        if (document.visibilityState === 'visible') {
            await chargerCoursDuJour();
        }
    }, 30000);
}
function stopPolling() {
    if (pollingInterval) { clearInterval(pollingInterval); pollingInterval = null; }
}

// ── API : statut de l'appel pour l'étudiant ───────────────────────────────────
async function verifierStatutAppel(seanceId) {
    try {
        const res = await fetch(`/api/appels-etudiant/statut-seance/${parseInt(seanceId)}`, {
            headers: authHeaders()
        });
        if (!res.ok) return null;
        return await res.json(); // StatutSeanceEtudiantDTO
    } catch { return null; }
}

// ── Charger les cours du jour ─────────────────────────────────────────────────
async function chargerCoursDuJour() {
    const container = document.getElementById('cours-container');
    if (!container) return;

    // Ne montrer le spinner que si le container est vide
    if (!container.querySelector('.cours-item')) {
        container.innerHTML = '<div class="text-center py-5 text-muted"><div class="spinner-border spinner-border-sm text-success me-2"></div>Chargement…</div>';
    }

    try {
        const res = await fetch('/api/etudiant/cours/a-venir', { headers: authHeaders() });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const tousLesSeances = await res.json();

        const todayStr = localDateString(new Date());
        const seancesAujourdhui = tousLesSeances.filter(s => s.date === todayStr && !s.evenement);

        if (seancesAujourdhui.length === 0) {
            stopPolling();
            container.innerHTML = `
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-calendar-x fs-2 d-block mb-2"></i>
                    Aucun cours programmé pour aujourd'hui
                </div>`;
            return;
        }

        container.innerHTML = '';
        const seancesIds = seancesAujourdhui.map(s => s.seanceId);

        // Charger les statuts en parallèle
        const statuts = await Promise.all(
            seancesAujourdhui.map(s => verifierStatutAppel(s.seanceId))
        );

        seancesAujourdhui.forEach((cours, idx) => {
            renderCours(cours, container, statuts[idx]);
        });

        bindButtons();
        startPolling(seancesIds);

        if (urlSeanceId) {
            setTimeout(() => {
                const target = container.querySelector(`[data-seance-id="${urlSeanceId}"]`);
                if (target) target.scrollIntoView({ behavior: 'smooth', block: 'center' });
            }, 300);
        }

    } catch (err) {
        console.error('[appeletu] Erreur:', err);
        container.innerHTML = `
            <div class="text-center py-5">
                <div class="alert alert-warning d-inline-block">
                    <i class="bi bi-wifi-off me-2"></i>Impossible de charger les cours — réessayez
                </div>
            </div>`;
    }
}

// ── Rendu d'un cours ──────────────────────────────────────────────────────────
function renderCours(cours, container, statut) {
    const windowActive = isSessionActive(cours.heureDebut, cours.heureFin);
    const isTarget = urlSeanceId && String(cours.seanceId) === String(urlSeanceId);

    // Déterminer l'état des boutons selon le statut API
    const appelOuvert  = statut?.appelOuvert ?? false;
    const dejaPresent  = statut?.dejaPresent ?? false;

    // Badge fenêtre horaire
    const statusBadge = appelOuvert
        ? `<span class="badge bg-success ms-2"><i class="bi bi-unlock me-1"></i>Appel ouvert</span>`
        : `<span class="badge bg-secondary ms-2"><i class="bi bi-lock me-1"></i>Aucun appel</span>`;

    // Heure de dernière réponse (sessionStorage informatif)
    const derniereReponseISO = getDerniereReponse(cours.seanceId);
    const derniereHeure = derniereReponseISO
        ? new Date(derniereReponseISO).toLocaleTimeString('fr-FR', { hour: '2-digit', minute: '2-digit' })
        : null;

    // ── Génération de la zone boutons/statut ──
    let actionZone = '';
    if (dejaPresent) {
        // A déjà répondu à un appel de cette séance (statut persistant après clôture)
        actionZone = genererResumePresence(statut);
    } else if (!windowActive && !appelOuvert) {
        // Hors fenêtre, aucun appel
        actionZone = `
            <div class="text-muted small">
                <i class="bi bi-clock me-1"></i>Hors de la fenêtre d'appel
            </div>`;
    } else if (appelOuvert && !dejaPresent) {
        // Appel ouvert, pas encore répondu → boutons actifs
        const infoReponse = derniereHeure
            ? `<small class="text-success d-block mt-1"><i class="bi bi-check-circle me-1"></i>Précédente réponse à ${derniereHeure}</small>`
            : '';
        actionZone = `
            <div>
                <div class="d-flex flex-wrap gap-2 align-items-center">
                    <button class="btn-present btn-scan-qr"
                            data-seance="${cours.seanceId}"
                            data-label="${cours.matiere || '—'} ${cours.heureDebut || ''}">
                        <i class="bi bi-qr-code-scan me-1"></i>Scanner QR
                    </button>
                    <button class="btn-absent btn-saisir-pin"
                            data-seance="${cours.seanceId}"
                            data-label="${cours.matiere || '—'}">
                        <i class="bi bi-lock me-1"></i>Code PIN
                    </button>
                </div>
                ${infoReponse}
            </div>`;
    } else {
        // Fenêtre active mais aucun appel lancé
        actionZone = `
            <span class="badge bg-warning text-dark">
                <i class="bi bi-hourglass-split me-1"></i>En attente d'un appel
            </span>`;
    }

    const el = document.createElement('div');
    el.id = `cours-item-${cours.seanceId}`;
    el.className = 'cours-item d-flex justify-content-between align-items-start flex-wrap gap-2 m-3'
        + (isTarget ? ' border border-primary border-2 rounded-3' : '')
        + (dejaPresent ? ' border border-success border-2 rounded-3' : '');
    el.dataset.seanceId = cours.seanceId;
    if (isTarget)    el.style.boxShadow = '0 0 0 3px rgba(13,110,253,.25)';
    if (dejaPresent) el.style.boxShadow = '0 0 0 3px rgba(25,135,84,.25)';

    el.innerHTML = `
        <div class="flex-grow-1">
            <div class="fw-bold">${cours.matiere || '—'} ${statusBadge}</div>
            <small class="text-muted">
                <i class="bi bi-clock me-1"></i>${cours.heureDebut || ''} – ${cours.heureFin || ''}
                &nbsp;|&nbsp;
                <i class="bi bi-person me-1"></i>${cours.enseignant || '—'}
            </small>
        </div>
        <div class="d-flex flex-wrap gap-2 align-items-center justify-content-end">
            ${actionZone}
        </div>`;

    container.appendChild(el);
}

// ── Génération du résumé détaillé après réponse ───────────────────────────────
function genererResumePresence(statut) {
    if (!statut) return '';

    const icones = {
        'présent':         '✅',
        'retard':          '⚠️',
        'présent_partiel': '⚡',
        'absent':          '❌'
    };
    const icone = icones[statut.statut] || '🔵';

    // Message principal
    const message = statut.messageStatut || 'Présence enregistrée';

    // Détails supplémentaires
    let details = '';
    if (statut.retard) {
        details += `<span class="badge bg-warning text-dark me-1"><i class="bi bi-clock-history me-1"></i>En retard</span>`;
    }
    if (statut.heuresAbsentes > 0) {
        details += `<span class="badge bg-danger me-1"><i class="bi bi-x-circle me-1"></i>${statut.heuresAbsentes}h absent(es)</span>`;
    }
    if (statut.heuresPresentes > 0) {
        details += `<span class="badge bg-success me-1"><i class="bi bi-check-circle me-1"></i>${statut.heuresPresentes}h présent(es)</span>`;
    }

    return `
        <div class="presence-resume rounded border border-success-subtle bg-success-subtle px-3 py-2" style="max-width:320px;">
            <div class="fw-semibold mb-1">${icone} ${message}</div>
            ${details ? `<div class="mt-1">${details}</div>` : ''}
            <small class="text-muted d-block mt-1">
                <i class="bi bi-lock-fill me-1"></i>Présence enregistrée pour cet appel
            </small>
        </div>`;
}

// ── Liaison des boutons ───────────────────────────────────────────────────────
function bindButtons() {
    document.querySelectorAll('.btn-scan-qr').forEach(btn => {
        btn.addEventListener('click', () => ouvrirQrScan(btn.dataset.seance, btn.dataset.label));
    });
    document.querySelectorAll('.btn-saisir-pin').forEach(btn => {
        btn.addEventListener('click', () => ouvrirPinSaisie(btn.dataset.seance));
    });
}

// ── QR Scan ───────────────────────────────────────────────────────────────────
function ouvrirQrScan(seanceId, label) {
    currentSeanceId = seanceId;
    const modal = new bootstrap.Modal(document.getElementById('qrScanModal'));
    modal.show();

    const resultDiv = document.getElementById('qr-modal-result');
    const errorDiv  = document.getElementById('qr-modal-error');
    if (resultDiv) resultDiv.classList.add('d-none');
    if (errorDiv)  errorDiv.classList.add('d-none');

    if (qrScanner) { try { qrScanner.stop(); } catch(e) {} qrScanner = null; }

    if (typeof Html5QrcodeScanner !== 'undefined') {
        qrScanner = new Html5QrcodeScanner('qr-reader-modal', { fps: 10, qrbox: 250 });
        qrScanner.render(async (decodedText) => {
            qrScanner.clear();
            await validerQrCode(decodedText, seanceId);
        });
    } else {
        if (errorDiv) {
            errorDiv.innerHTML = '<i class="bi bi-exclamation-triangle me-2"></i>Scanner QR non disponible';
            errorDiv.classList.remove('d-none');
        }
    }
}

async function validerQrCode(token, seanceId) {
    const resultDiv = document.getElementById('qr-modal-result');
    const errorDiv  = document.getElementById('qr-modal-error');
    try {
        const res = await fetch(`/api/appels/seance/${parseInt(seanceId)}/etudiant/valider-qr`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ qrToken: token })
        });
        if (res.ok) {
            const data = await res.json().catch(() => null);
            if (resultDiv) {
                const texte = document.getElementById('qr-modal-text');
                if (texte) texte.textContent = 'Présence enregistrée avec succès !';
                resultDiv.classList.remove('d-none');
            }
            if (errorDiv) errorDiv.classList.add('d-none');
            // Enregistrer l'heure de réponse (informatif)
            setDerniereReponse(seanceId, new Date().toISOString());
            showMsg('✔ Présence enregistrée !', 'success');
            // Rafraîchir immédiatement pour afficher le résumé
            setTimeout(() => chargerCoursDuJour(), 1000);
        } else {
            const data = await res.json().catch(() => null);
            const msg  = data?.error || 'QR invalide ou expiré';
            if (errorDiv) {
                const errTexte = document.getElementById('qr-modal-err-text');
                if (errTexte) errTexte.textContent = msg;
                errorDiv.classList.remove('d-none');
            }
            if (resultDiv) resultDiv.classList.add('d-none');
        }
    } catch (err) {
        console.error('[appeletu] QR error:', err);
        if (errorDiv) {
            const errTexte = document.getElementById('qr-modal-err-text');
            if (errTexte) errTexte.textContent = 'Erreur réseau';
            errorDiv.classList.remove('d-none');
        }
    }
}

// ── PIN ───────────────────────────────────────────────────────────────────────
function ouvrirPinSaisie(seanceId) {
    currentSeanceId = seanceId;
    const input = document.getElementById('pin-input');
    const msg   = document.getElementById('pin-message');
    if (input) input.value = '';
    if (msg)   msg.classList.add('d-none');
    const pinCard = document.getElementById('pin-card');
    if (pinCard) {
        pinCard.style.display = '';
        pinCard.scrollIntoView({ behavior: 'smooth', block: 'start' });
    }
}

document.getElementById('btn-valider-pin')?.addEventListener('click', async () => {
    const pin   = document.getElementById('pin-input')?.value?.trim();
    const msgEl = document.getElementById('pin-message');

    if (!pin || pin.length < 4) {
        if (msgEl) { msgEl.className = 'alert alert-warning'; msgEl.textContent = 'Code PIN trop court'; msgEl.classList.remove('d-none'); }
        return;
    }
    if (!currentSeanceId) {
        if (msgEl) { msgEl.className = 'alert alert-warning'; msgEl.textContent = 'Aucune séance sélectionnée'; msgEl.classList.remove('d-none'); }
        return;
    }

    const btn = document.getElementById('btn-valider-pin');
    if (btn) btn.disabled = true;
    if (msgEl) msgEl.classList.add('d-none');

    try {
        const res = await fetch(`/api/appels/seance/${parseInt(currentSeanceId)}/etudiant/valider-pin`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ codePIN: pin })
        });
        if (res.ok) {
            const data = await res.json().catch(() => null);
            if (msgEl) { msgEl.className = 'alert alert-success'; msgEl.textContent = '✔ Présence enregistrée !'; msgEl.classList.remove('d-none'); }
            // Enregistrer l'heure de réponse (informatif)
            setDerniereReponse(currentSeanceId, new Date().toISOString());
            showMsg('Présence enregistrée avec succès !', 'success');
            const pinCard = document.getElementById('pin-card');
            if (pinCard) pinCard.style.display = 'none';
            // Rafraîchir pour afficher le résumé détaillé
            setTimeout(() => chargerCoursDuJour(), 800);
        } else {
            const data = await res.json().catch(() => null);
            const msg  = data?.error || 'Code PIN incorrect ou expiré';
            if (msgEl) { msgEl.className = 'alert alert-danger'; msgEl.textContent = msg; msgEl.classList.remove('d-none'); }
        }
    } catch (err) {
        if (msgEl) { msgEl.className = 'alert alert-warning'; msgEl.textContent = 'Erreur réseau'; msgEl.classList.remove('d-none'); }
    } finally {
        if (btn) btn.disabled = false;
    }
});

// ── Justification ─────────────────────────────────────────────────────────────
function ouvrirJustification(presenceId, label) {
    document.getElementById('justif-presence-id').value = presenceId;
    document.getElementById('justif-cours-label').textContent = label || '—';
    document.getElementById('justif-motif').value = '';
    const modal = new bootstrap.Modal(document.getElementById('justificationModal'));
    modal.show();
}

document.getElementById('envoyer-justif')?.addEventListener('click', async () => {
    const presenceId = document.getElementById('justif-presence-id')?.value;
    const motif = document.getElementById('justif-motif')?.value?.trim();
    if (!motif) { alert('Veuillez saisir un motif.'); return; }

    const btn = document.getElementById('envoyer-justif');
    btn.disabled = true;
    try {
        const fd = new FormData();
        fd.append('presenceId', presenceId);
        fd.append('motif', motif);
        const res = await fetch(`/api/justificatifs/submit?etudiantId=${user?.id || ''}`, {
            method: 'POST',
            headers: { 'Authorization': `Bearer ${getToken()}` },
            body: fd
        });
        if (res.ok) {
            bootstrap.Modal.getInstance(document.getElementById('justificationModal'))?.hide();
            showMsg('Justification envoyée avec succès !', 'success');
        } else {
            const err = await res.text();
            alert('Erreur : ' + err);
        }
    } catch (err) {
        alert('Erreur réseau');
    } finally {
        btn.disabled = false;
    }
});

// ── Refresh manuel ────────────────────────────────────────────────────────────
document.getElementById('btn-refresh')?.addEventListener('click', () => {
    stopPolling();
    chargerCoursDuJour();
});

// ── Arrêter le polling quand la page est cachée ───────────────────────────────
document.addEventListener('visibilitychange', () => {
    if (document.visibilityState === 'hidden') {
        stopPolling();
    }
});

// ── Lancement ─────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', chargerCoursDuJour);