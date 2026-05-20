/**
 * ADSIS — appeletu.js
 * Page appel étudiant : cours du jour, QR scan, saisie PIN, justification
 *
 * Corrections par rapport à la version collègue :
 *  - Import depuis auth.js (getUser + guard ETUDIANT)
 *  - URLs API corrigées (/api/etudiant/cours/a-venir filtré sur aujourd'hui,
 *    puis /api/appels-etudiant/* pour marquer présent)
 *  - Intégration html5-qrcode pour le scan caméra
 *  - Pas de `authFetch` (non exporté) → on utilise fetch standard avec Authorization header
 */
import { requireRole, getUser } from './auth.js';

if (!requireRole(['ETUDIANT'])) throw new Error('Accès refusé');

const user = getUser();
let qrScanner = null;
let currentSeanceId = null;

// ── Gestion "déjà présent" — persistance via sessionStorage ──────────────────
const TODAY = new Date().toISOString().split('T')[0];
function _markedKey() { return `adsis_marked_${TODAY}`; }
function getMarkedSeances() {
    try { return JSON.parse(sessionStorage.getItem(_markedKey()) || '{}'); } catch { return {}; }
}
function markSeanceAsPresent(seanceId) {
    const m = getMarkedSeances();
    m[String(seanceId)] = new Date().toISOString();
    sessionStorage.setItem(_markedKey(), JSON.stringify(m));
}
function isSeanceAlreadyMarked(seanceId) {
    return !!getMarkedSeances()[String(seanceId)];
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

// ── Helper: récupérer le token JWT ──────────────────────────────────────────────
function getToken() {
    return localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
}

// ── Helper: seanceId depuis l'URL (?seanceId=X) ─────────────────────────────
const urlSeanceId = new URLSearchParams(window.location.search).get('seanceId');

// ── Helper: fenêtre de 15 min autour d'une séance ───────────────────────────
/**
 * Returns true if the current time is within [-15min, +15min] of [heureDebut, heureFin].
 * Expects "HH:MM" or "HH:MM:SS" strings.
 */
function isSessionActive(heureDebut, heureFin) {
    if (!heureDebut || !heureFin) return false;
    const now = new Date();
    const nowMin = now.getHours() * 60 + now.getMinutes();
    const toMin = t => { const p = t.split(':').map(Number); return p[0] * 60 + (p[1] || 0); };
    return nowMin >= toMin(heureDebut) - 15 && nowMin <= toMin(heureFin) + 15;
}

function authHeaders(extra = {}) {
    return { 'Authorization': `Bearer ${getToken()}`, 'Content-Type': 'application/json', ...extra };
}

// ── Afficher message ──────────────────────────────────────────────────────────
function showMsg(msg, type = 'success') {
    const div = document.getElementById('appel-message');
    if (!div) return;
    div.className = `alert alert-${type}`;
    div.innerHTML = `<i class="bi bi-${type === 'success' ? 'check-circle' : 'x-circle'} me-2"></i>${msg}`;
    div.classList.remove('d-none');
    setTimeout(() => div.classList.add('d-none'), 4000);
}

// ── Charger les cours du jour ─────────────────────────────────────────────────
async function chargerCoursDuJour() {
    const container = document.getElementById('cours-container');
    if (!container) return;

    container.innerHTML = '<div class="text-center py-5 text-muted"><div class="spinner-border spinner-border-sm text-success me-2"></div>Chargement…</div>';

    try {
        // On récupère les "prochains cours" et on filtre sur aujourd'hui
        const res = await fetch('/api/etudiant/cours/a-venir', {
            headers: authHeaders()
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const tousLesSeances = await res.json();

        const todayStr = localDateString(new Date());
        const seancesAujourdhui = tousLesSeances.filter(s => s.date === todayStr);

        if (seancesAujourdhui.length === 0) {
            container.innerHTML = `
                <div class="text-center py-5 text-muted">
                    <i class="bi bi-calendar-x fs-2 d-block mb-2"></i>
                    Aucun cours programmé pour aujourd'hui
                </div>`;
            return;
        }

        container.innerHTML = '';
        seancesAujourdhui.forEach(cours => {
            renderCours(cours, container);
        });

        bindButtons();

        // Si un seanceId est passé en URL, scroll vers la séance cible
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

function renderCours(cours, container) {
    const active         = isSessionActive(cours.heureDebut, cours.heureFin);
    const isTarget       = urlSeanceId && String(cours.seanceId) === String(urlSeanceId);
    const alreadyPresent = isSeanceAlreadyMarked(cours.seanceId);

    const el = document.createElement('div');
    el.id = `cours-item-${cours.seanceId}`;
    el.className = `cours-item d-flex justify-content-between align-items-start flex-wrap gap-2 m-3`
        + (isTarget       ? ' border border-primary border-2 rounded-3' : '')
        + (alreadyPresent ? ' border border-success border-2 rounded-3' : '');
    el.dataset.seanceId = cours.seanceId;
    if (isTarget)       el.style.boxShadow = '0 0 0 3px rgba(13,110,253,.25)';
    if (alreadyPresent) el.style.boxShadow = '0 0 0 3px rgba(25,135,84,.25)';

    // Badge statut appel
    const statusBadge = active
        ? `<span class="badge bg-success ms-2"><i class="bi bi-unlock me-1"></i>Appel ouvert</span>`
        : `<span class="badge bg-secondary ms-2"><i class="bi bi-lock me-1"></i>Hors fenêtre</span>`;

    // Badge "déjà présent" (persisté en sessionStorage)
    const presentBadge = alreadyPresent
        ? `<span class="badge bg-success ms-2"><i class="bi bi-check-circle-fill me-1"></i>Présence enregistrée</span>`
        : '';

    const lockMsg = active ? '' : `
        <div class="alert alert-warning py-1 px-2 mt-2 mb-0" style="font-size:.8rem;">
            <i class="bi bi-clock me-1"></i>
            L'appel n'est disponible que 15 min avant ou après le début du cours.
        </div>`;

    el.innerHTML = `
        <div class="flex-grow-1">
            <div class="fw-bold">${cours.matiere || '—'} ${statusBadge}${presentBadge}</div>
            <small class="text-muted">
                <i class="bi bi-clock me-1"></i>${cours.heureDebut || ''} – ${cours.heureFin || ''}
                &nbsp;|&nbsp;
                <i class="bi bi-person me-1"></i>${cours.enseignant || '—'}
            </small>
            ${lockMsg}
        </div>
        <div class="d-flex flex-wrap gap-2 align-items-center">
            ${alreadyPresent
                ? `<span class="badge bg-success fs-6 px-3 py-2"><i class="bi bi-check2-all me-1"></i>Vous êtes présent ✓</span>`
                : `<button class="btn-present btn-scan-qr"
                        data-seance="${cours.seanceId}"
                        data-label="${cours.matiere || '—'} ${cours.heureDebut || ''}"
                        ${active ? '' : 'disabled title="Hors fenêtre d\'appel"'}>
                    <i class="bi bi-qr-code-scan me-1"></i>Scanner QR
                   </button>
                   <button class="btn-absent btn-saisir-pin"
                        data-seance="${cours.seanceId}"
                        data-label="${cours.matiere || '—'}"
                        ${active ? '' : 'disabled title="Hors fenêtre d\'appel"'}>
                    <i class="bi bi-lock me-1"></i>Code PIN
                   </button>`
            }
        </div>`;
    container.appendChild(el);
}

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

    const resultDiv  = document.getElementById('qr-modal-result');
    const errorDiv   = document.getElementById('qr-modal-error');
    if (resultDiv) resultDiv.classList.add('d-none');
    if (errorDiv)  errorDiv.classList.add('d-none');

    // Stopper un éventuel scan précédent
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
        // Utilise AppelController — vérifie le QR de l'enseignant dans le pinCache
        const res = await fetch(`/api/appels/seance/${parseInt(seanceId)}/etudiant/valider-qr`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ qrToken: token })
        });
        if (res.ok) {
            if (resultDiv) {
                resultDiv.querySelector('#qr-modal-text').textContent = 'Présence enregistrée avec succès !';
                resultDiv.classList.remove('d-none');
            }
            if (errorDiv) errorDiv.classList.add('d-none');
            // Enregistrer dans sessionStorage
            markSeanceAsPresent(seanceId);
            showMsg('✔ Présence enregistrée !', 'success');
            setTimeout(() => chargerCoursDuJour(), 1500);
        } else {
            const data = await res.json().catch(() => null);
            const msg  = data?.error || 'QR invalide ou expiré';
            if (errorDiv) {
                document.getElementById('qr-modal-err-text').textContent = msg;
                errorDiv.classList.remove('d-none');
            }
            if (resultDiv) resultDiv.classList.add('d-none');
        }
    } catch (err) {
        console.error('[appeletu] QR error:', err);
        if (errorDiv) {
            document.getElementById('qr-modal-err-text').textContent = 'Erreur réseau';
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
        // Utilise AppelController — vérifie le PIN de l'enseignant dans le pinCache
        const res = await fetch(`/api/appels/seance/${parseInt(currentSeanceId)}/etudiant/valider-pin`, {
            method: 'POST',
            headers: authHeaders(),
            body: JSON.stringify({ codePIN: pin })
        });
        if (res.ok) {
            if (msgEl) { msgEl.className = 'alert alert-success'; msgEl.textContent = '✔ Présence enregistrée !'; msgEl.classList.remove('d-none'); }
            // Enregistrer dans sessionStorage
            markSeanceAsPresent(currentSeanceId);
            showMsg('Présence enregistrée avec succès !', 'success');
            const pinCard = document.getElementById('pin-card');
            if (pinCard) pinCard.style.display = 'none';
            setTimeout(() => chargerCoursDuJour(), 1000);
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

// ── Refresh ───────────────────────────────────────────────────────────────────
document.getElementById('btn-refresh')?.addEventListener('click', chargerCoursDuJour);

// ── Helper : date locale sans problème UTC ────────────────────────────────────
function localDateString(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

// ── Lancement ─────────────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', chargerCoursDuJour);