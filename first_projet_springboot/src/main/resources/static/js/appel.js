/* ===== ADSIS – appel.js — Séance d'appel dynamique ===== */
import { requireRole, requirePermission, getLoginPath, authFetch, getUser } from './auth.js';

// Guard : enseignants uniquement avec permission ATTENDANCE_MANAGE
if (!requireRole(['ENSEIGNANT'])) { throw new Error('Unauthorized'); }
if (!requirePermission('ATTENDANCE_MANAGE')) {
    window.location.href = getLoginPath();
    throw new Error('Permission refusée');
}

// ── État global ──────────────────────────────────────────────────────────────
let seanceId = null;
let appelId = null;
let studentsData = []; // EtudiantSeanceDTO[]
let creneauxHeaders = []; // [{heureDebut, heureFin}]
let seanceInfo = null; // SeanceEnseignantDTO

const user = getUser();

// ── Récupérer seanceId depuis l'URL ──────────────────────────────────────────
const urlParams = new URLSearchParams(window.location.search);
seanceId = parseInt(urlParams.get('seanceId')) || null;

// ── Éléments DOM ─────────────────────────────────────────────────────────────
const sessionSelectEl = document.getElementById('session-select');

// ── Toast ─────────────────────────────────────────────────────────────────────
let toastTimer = null;
function showToast(msg, type = 'info', duration = 3000) {
    const toast = document.getElementById('toast');
    toast.textContent = msg;
    toast.className = `toast ${type}-toast show`;
    clearTimeout(toastTimer);
    toastTimer = setTimeout(() => { toast.className = 'toast'; }, duration);
}

// ═══════════════════════════════════════════════════════════════════════════════
// INITIALISATION
// ═══════════════════════════════════════════════════════════════════════════════

document.addEventListener('DOMContentLoaded', async () => {
    // Date dans la topbar
    const dateEl = document.getElementById('page-date');
    if (dateEl) {
        dateEl.textContent = new Date().toLocaleDateString('fr-FR', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
        });
    }

    await loadSeances();
});

// Charger les séances pour le sélecteur
async function loadSeances() {
    if (!user || !user.id) return;

    try {
        // Charger les séances sur une large période (2 semaines passées + 4 semaines futures)
        const now = new Date();
        const dateDebut = new Date(now);
        dateDebut.setDate(now.getDate() - 14);
        const dateFin = new Date(now);
        dateFin.setDate(now.getDate() + 28);

        const fmt = d => d.toISOString().split('T')[0];
        const res = await authFetch(`/api/seances/enseignant/${user.id}/periode?dateDebut=${fmt(dateDebut)}&dateFin=${fmt(dateFin)}`);
        if (!res || !res.ok) return;

        const seances = await res.json();

        // Trier par date puis heure
        seances.sort((a, b) => {
            const d = (a.date || '').localeCompare(b.date || '');
            return d !== 0 ? d : (a.heureDebut || '').localeCompare(b.heureDebut || '');
        });

        // Peupler le sélecteur
        sessionSelectEl.innerHTML = '';
        const DAY_MAP = { 'MONDAY':'Lun', 'TUESDAY':'Mar', 'WEDNESDAY':'Mer', 'THURSDAY':'Jeu', 'FRIDAY':'Ven', 'SATURDAY':'Sam', 'SUNDAY':'Dim' };
        
        seances.forEach(s => {
            const opt = document.createElement('option');
            opt.value = s.id;
            let dayLabel = '';
            if (s.date) {
                const d = new Date(s.date + 'T00:00:00');
                const dayKey = d.toLocaleDateString('en-US', { weekday: 'long' }).toUpperCase();
                dayLabel = DAY_MAP[dayKey] || '';
            }
            const hd = s.heureDebut ? s.heureDebut.substring(0, 5) : '';
            opt.textContent = `${dayLabel} · ${hd} – ${s.coursIntitule || 'Cours'} (${s.classeNom || ''})`;
            sessionSelectEl.appendChild(opt);
        });

        // Si pas de seanceId dans l'URL, prendre la première
        if (!seanceId && seances.length > 0) {
            seanceId = seances[0].id;
        }

        if (seanceId) {
            sessionSelectEl.value = seanceId;
            seanceInfo = seances.find(s => s.id === seanceId) || seances[0] || null;
            await initSession();
        }

    } catch (err) {
        console.error('Erreur chargement séances:', err);
    }
}

// Changement de séance
sessionSelectEl.addEventListener('change', async () => {
    seanceId = parseInt(sessionSelectEl.value);
    appelId = null;
    await initSession();
    showToast('Séance changée', 'info', 2000);
});

// Vérifie si la séance est dans la fenêtre temporelle autorisée (±15 min)
function isSeanceInTimeWindow(info) {
    if (!info || !info.heureDebut || !info.heureFin || !info.date) return true; // permissif si données manquantes

    const now = new Date();
    const todayStr = now.toISOString().split('T')[0];
    const seanceDateStr = info.date;

    // Seule la séance du jour est vérifiable
    if (seanceDateStr !== todayStr) {
        // Séance passée ou future → bloquer
        return false;
    }

    const [sh, sm] = info.heureDebut.split(':').map(Number);
    const [eh, em] = info.heureFin.split(':').map(Number);
    const startMin = sh * 60 + sm - 15;  // 15 min avant le début
    const endMin = eh * 60 + em + 15;     // 15 min après la fin
    const nowMin = now.getHours() * 60 + now.getMinutes();

    return nowMin >= startMin && nowMin <= endMin;
}

// Affiche un message de blocage quand la séance n'est pas dans la fenêtre autorisée
function showTimeBlockedMessage(info) {
    const contentEl = document.querySelector('.content');
    if (!contentEl) return;

    const hd = info?.heureDebut ? info.heureDebut.substring(0, 5) : '??:??';
    const hf = info?.heureFin ? info.heureFin.substring(0, 5) : '??:??';
    const dateLabel = info?.date || '';

    const now = new Date();
    const todayStr = now.toISOString().split('T')[0];
    const isPast = info?.date && info.date < todayStr;
    const isFuture = info?.date && info.date > todayStr;

    let reasonText = '';
    if (isPast) {
        reasonText = 'Cette séance est déjà passée.';
    } else if (isFuture) {
        reasonText = 'Cette séance n\'a pas encore eu lieu.';
    } else {
        reasonText = `L'appel est autorisé uniquement entre <strong>${hd}</strong> (−15 min) et <strong>${hf}</strong> (+15 min).`;
    }

    contentEl.innerHTML = `
        <div style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:60vh;text-align:center;padding:2rem;">
            <div style="width:80px;height:80px;border-radius:50%;background:#FEF3C7;display:flex;align-items:center;justify-content:center;margin-bottom:1.5rem;">
                <svg viewBox="0 0 24 24" fill="none" stroke="#D97706" stroke-width="2" width="40" height="40">
                    <circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/>
                    <line x1="12" y1="16" x2="12.01" y2="16"/>
                </svg>
            </div>
            <h3 style="font-size:1.3rem;font-weight:800;color:#1C1C1E;margin-bottom:0.5rem;">Appel non disponible</h3>
            <p style="color:#8E8E93;font-size:0.9rem;max-width:380px;line-height:1.5;margin-bottom:0.5rem;">
                ${reasonText}
            </p>
            <p style="color:#8E8E93;font-size:0.8rem;margin-bottom:1.5rem;">
                Séance : <strong>${info?.coursIntitule || 'Cours'}</strong> — ${dateLabel} de ${hd} à ${hf}
            </p>
            <button onclick="history.back()" style="background:#007AFF;color:white;border:none;padding:12px 32px;border-radius:12px;font-weight:700;font-size:0.9rem;cursor:pointer;transition:transform 0.15s;">
                ← Retour à l'emploi du temps
            </button>
        </div>`;
}

// Initialiser la session sélectionnée
async function initSession() {
    if (!seanceId) return;

    // Mettre à jour la bannière
    updateBanner();

    // Vérifier la fenêtre temporelle ±15 min
    if (seanceInfo && !isSeanceInTimeWindow(seanceInfo)) {
        showTimeBlockedMessage(seanceInfo);
        return; // Bloquer toute opération d'appel
    }

    // Vérifier s'il y a un appel ouvert
    await checkAppelOuvert();

    // Charger les étudiants
    await loadEtudiants();
}

function updateBanner() {
    const courseEl = document.getElementById('session-course');
    const metaEl = document.getElementById('session-meta');

    if (seanceInfo) {
        courseEl.textContent = seanceInfo.coursIntitule || 'Cours';
        const hd = seanceInfo.heureDebut ? seanceInfo.heureDebut.substring(0, 5) : '';
        const hf = seanceInfo.heureFin ? seanceInfo.heureFin.substring(0, 5) : '';
        metaEl.textContent = [seanceInfo.classeNom, `${hd} – ${hf}`].filter(Boolean).join(' • ');
    } else {
        courseEl.textContent = 'Séance d\'appel';
        metaEl.textContent = '';
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// APPEL — GESTION
// ═══════════════════════════════════════════════════════════════════════════════

async function checkAppelOuvert() {
    try {
        const res = await authFetch(`/api/appels/seance/${seanceId}/ouvert`);
        if (res && res.ok && res.status !== 204) {
            const data = await res.json();
            appelId = data.id;
            updateSessionStatus(true);
        } else {
            appelId = null;
            updateSessionStatus(false);
        }
    } catch {
        appelId = null;
    }
}

function updateSessionStatus(isOpen) {
    const statusEl = document.getElementById('session-status');
    if (isOpen) {
        statusEl.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><path d="M5 12.55a11 11 0 0 1 14.08 0"/><path d="M1.42 9a16 16 0 0 1 21.16 0"/><path d="M8.53 16.11a6 6 0 0 1 6.95 0"/><line x1="12" y1="20" x2="12.01" y2="20"/></svg>Appel ouvert`;
        statusEl.style.background = '#f0fdf4';
        statusEl.style.color = 'var(--success)';
        statusEl.style.borderColor = '#bbf7d0';
    } else {
        statusEl.innerHTML = `<svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="16" height="16"><circle cx="12" cy="12" r="10"/><line x1="12" y1="8" x2="12" y2="12"/><line x1="12" y1="16" x2="12.01" y2="16"/></svg>Pas d'appel`;
        statusEl.style.background = '#fef3c7';
        statusEl.style.color = 'var(--warning)';
        statusEl.style.borderColor = '#fde68a';
    }
}

async function demarrerAppel(mode = 'manuel') {
    if (!seanceId) {
        showToast('Veuillez d\'abord sélectionner une séance.', 'error');
        return null;
    }
    try {
        const res = await authFetch('/api/appels', {
            method: 'POST',
            body: JSON.stringify({ seanceId, modeAppel: mode })
        });
        if (!res || !res.ok) {
            const err = await res?.text();
            showToast('Erreur: ' + (err || 'Impossible de démarrer l\'appel'), 'error');
            return null;
        }
        const data = await res.json();
        appelId = data.id;
        updateSessionStatus(true);
        showToast('Appel démarré !', 'success');
        return data;
    } catch (err) {
        showToast('Erreur de connexion', 'error');
        return null;
    }
}

// ═══════════════════════════════════════════════════════════════════════════════
// ÉTUDIANTS et GRILLE HORAIRE
// ═══════════════════════════════════════════════════════════════════════════════

async function loadEtudiants() {
    if (!seanceId) return;

    try {
        let url = `/api/appels/seance/${seanceId}/etudiants`;
        if (appelId) url += `?appelId=${appelId}`;

        const res = await authFetch(url);
        if (!res || !res.ok) {
            console.error('Erreur chargement étudiants');
            return;
        }

        studentsData = await res.json();

        // Extraire les créneaux horaires depuis le premier étudiant
        if (studentsData.length > 0 && studentsData[0].creneaux) {
            creneauxHeaders = studentsData[0].creneaux.map(c => ({
                heureDebut: c.heureDebut,
                heureFin: c.heureFin
            }));
        }

        renderHourlyGrid();
        updateCounters();

    } catch (err) {
        console.error('Erreur étudiants:', err);
    }
}

function renderHourlyGrid() {
    const headerRow = document.getElementById('hourly-header');
    const body = document.getElementById('hourly-body');

    // Reconstruire le header
    headerRow.innerHTML = '<th>Étudiant</th>';
    creneauxHeaders.forEach(c => {
        const hd = c.heureDebut ? c.heureDebut.substring(0, 5) : '';
        const hf = c.heureFin ? c.heureFin.substring(0, 5) : '';
        headerRow.innerHTML += `<th>${hd}-${hf}</th>`;
    });
    headerRow.innerHTML += '<th>Retard</th>';

    // Construire les lignes
    body.innerHTML = '';
    studentsData.forEach(student => {
        const row = document.createElement('tr');
        row.id = `row-${student.id}`;
        row.dataset.name = `${student.nom} ${student.prenom}`.toLowerCase();

        // Déterminer la couleur du statut
        const creneaux = student.creneaux || [];
        const allPresent = creneaux.length > 0 && creneaux.every(c => c.present);
        const allAbsent = creneaux.length > 0 && creneaux.every(c => !c.present);
        let statusClass = 'none';
        if (allPresent) statusClass = 'present';
        else if (allAbsent) statusClass = 'absent';
        else if (creneaux.some(c => c.present)) statusClass = 'partial';

        // Colonne étudiant
        const initials = ((student.nom || '?')[0] + (student.prenom || '')[0]).toUpperCase();
        row.innerHTML = `
            <td>
                <div class="hourly-student-info">
                    <div class="status-indicator ${statusClass}"></div>
                    <div class="student-avatar" style="width:32px;height:32px;font-size:0.75rem;">${initials}</div>
                    <div>
                        <div class="hourly-student-name">${student.nom || ''} ${student.prenom || ''}</div>
                        <div class="hourly-student-mat">${student.matricule || ''}</div>
                    </div>
                </div>
            </td>`;

        // Colonnes créneaux
        creneaux.forEach((creneau, idx) => {
            const td = document.createElement('td');
            const cell = document.createElement('button');
            cell.className = `hourly-cell ${creneau.present ? 'present' : 'absent'}`;
            cell.textContent = creneau.present ? '✅' : '❌';
            cell.dataset.studentId = student.id;
            cell.dataset.creneauIdx = idx;
            cell.addEventListener('click', () => toggleCreneau(student.id, idx));
            td.appendChild(cell);
            row.appendChild(td);
        });

        // Colonne retard
        const tdRetard = document.createElement('td');
        const retardCb = document.createElement('input');
        retardCb.type = 'checkbox';
        retardCb.className = 'retard-checkbox';
        retardCb.checked = student.retard || false;
        retardCb.dataset.studentId = student.id;
        retardCb.addEventListener('change', () => {
            const s = studentsData.find(s => s.id === student.id);
            if (s) s.retard = retardCb.checked;
            savePresence(student.id);
        });
        tdRetard.appendChild(retardCb);
        row.appendChild(tdRetard);

        body.appendChild(row);
    });
}

function toggleCreneau(studentId, creneauIdx) {
    const student = studentsData.find(s => s.id === studentId);
    if (!student || !student.creneaux || !student.creneaux[creneauIdx]) return;

    // Toggle
    student.creneaux[creneauIdx].present = !student.creneaux[creneauIdx].present;

    // Mettre à jour l'UI
    const row = document.getElementById(`row-${studentId}`);
    if (row) {
        const cells = row.querySelectorAll('.hourly-cell');
        const cell = cells[creneauIdx];
        if (cell) {
            const isPresent = student.creneaux[creneauIdx].present;
            cell.className = `hourly-cell ${isPresent ? 'present' : 'absent'}`;
            cell.textContent = isPresent ? '✅' : '❌';
        }

        // Mettre à jour l'indicateur de statut
        updateStatusIndicator(row, student);
    }

    updateCounters();
    savePresence(studentId);
}

function updateStatusIndicator(row, student) {
    const indicator = row.querySelector('.status-indicator');
    if (!indicator) return;

    const creneaux = student.creneaux || [];
    const allPresent = creneaux.length > 0 && creneaux.every(c => c.present);
    const allAbsent = creneaux.length > 0 && creneaux.every(c => !c.present);

    indicator.className = 'status-indicator';
    if (allPresent) indicator.classList.add('present');
    else if (allAbsent) indicator.classList.add('absent');
    else if (creneaux.some(c => c.present)) indicator.classList.add('partial');
    else indicator.classList.add('none');
}

// Sauvegarder la présence d'un étudiant
async function savePresence(studentId) {
    if (!appelId) {
        // Démarrer un appel si nécessaire
        const appel = await demarrerAppel('manuel');
        if (!appel) return;
    }

    const student = studentsData.find(s => s.id === studentId);
    if (!student) return;

    // Déterminer le statut
    const creneaux = student.creneaux || [];
    const allPresent = creneaux.every(c => c.present);
    const allAbsent = creneaux.every(c => !c.present);
    let statut = 'absent';
    if (allPresent) statut = 'présent';
    else if (!allAbsent) statut = 'présent_partiel';

    try {
        await authFetch(`/api/appels/${appelId}/presences`, {
            method: 'POST',
            body: JSON.stringify({
                etudiantId: studentId,
                statut: statut,
                modeEnregistrement: 'manuel',
                retard: student.retard || false,
                creneaux: creneaux.map(c => ({
                    heureDebut: c.heureDebut,
                    heureFin: c.heureFin,
                    present: c.present
                }))
            })
        });
    } catch (err) {
        console.error('Erreur sauvegarde présence:', err);
    }
}

// Compteurs
function updateCounters() {
    let presents = 0, retards = 0, absents = 0;

    studentsData.forEach(s => {
        const creneaux = s.creneaux || [];
        const allPresent = creneaux.every(c => c.present);
        const allAbsent = creneaux.every(c => !c.present);

        if (allPresent) presents++;
        else if (allAbsent) absents++;
        else presents++; // partiel = compté comme présent

        if (s.retard) retards++;
    });

    document.getElementById('cnt-present').textContent = presents;
    document.getElementById('cnt-retard').textContent = retards;
    document.getElementById('cnt-absent').textContent = absents;
}

// Recherche
document.getElementById('student-search').addEventListener('input', function () {
    const q = this.value.toLowerCase();
    document.querySelectorAll('#hourly-body tr').forEach(row => {
        row.style.display = (row.dataset.name || '').includes(q) ? '' : 'none';
    });
});

// Marquer tous présents
document.getElementById('btn-mark-all-present').addEventListener('click', async () => {
    if (!appelId) {
        const appel = await demarrerAppel('manuel');
        if (!appel) return;
    }

    studentsData.forEach(student => {
        if (student.creneaux) {
            student.creneaux.forEach(c => c.present = true);
        }
    });

    renderHourlyGrid();
    updateCounters();

    // Sauvegarder en batch
    for (const student of studentsData) {
        await savePresence(student.id);
    }

    showToast('Tous les étudiants marqués présents.', 'success');
});

// ═══════════════════════════════════════════════════════════════════════════════
// TABS
// ═══════════════════════════════════════════════════════════════════════════════

const tabScan = document.getElementById('tab-scan');
const tabQrgen = document.getElementById('tab-qrgen');
const tabList = document.getElementById('tab-list');
const tabPin = document.getElementById('tab-pin');
const panelScan = document.getElementById('panel-scan');
const panelQrgen = document.getElementById('panel-qrgen');
const panelList = document.getElementById('panel-list');
const panelPin = document.getElementById('panel-pin');

function activateTab(tab) {
    [tabScan, tabQrgen, tabList, tabPin].forEach(t => {
        t.classList.remove('active');
        t.setAttribute('aria-selected', 'false');
    });
    [panelScan, panelQrgen, panelList, panelPin].forEach(p => p.classList.add('hidden'));

    if (tab === 'scan') {
        tabScan.classList.add('active'); tabScan.setAttribute('aria-selected', 'true');
        panelScan.classList.remove('hidden');
    } else if (tab === 'qrgen') {
        tabQrgen.classList.add('active'); tabQrgen.setAttribute('aria-selected', 'true');
        panelQrgen.classList.remove('hidden');
        stopScan();
        initQRGen();
    } else if (tab === 'pin') {
        tabPin.classList.add('active'); tabPin.setAttribute('aria-selected', 'true');
        panelPin.classList.remove('hidden');
        stopScan();
    } else {
        tabList.classList.add('active'); tabList.setAttribute('aria-selected', 'true');
        panelList.classList.remove('hidden');
        stopScan();
    }
}

tabScan.addEventListener('click', () => activateTab('scan'));
tabQrgen.addEventListener('click', () => activateTab('qrgen'));
tabList.addEventListener('click', () => activateTab('list'));
tabPin.addEventListener('click', () => activateTab('pin'));

// ═══════════════════════════════════════════════════════════════════════════════
// QR SCAN
// ═══════════════════════════════════════════════════════════════════════════════

let html5QrCode = null;
let scanning = false;

const btnStart = document.getElementById('btn-start-scan');
const btnStop = document.getElementById('btn-stop-scan');
const camError = document.getElementById('camera-error');
const scanResult = document.getElementById('scan-result');
const scanResultText = document.getElementById('scan-result-text');
let resultTimer = null;

function showScanResult(name) {
    scanResultText.textContent = `${name} marqué présent !`;
    scanResult.style.display = 'flex';
    clearTimeout(resultTimer);
    resultTimer = setTimeout(() => { scanResult.style.display = 'none'; }, 3000);
}

async function startScan() {
    if (scanning) return;

    try {
        await navigator.mediaDevices.getUserMedia({ video: true });
    } catch (e) {
        camError.style.display = 'flex';
        btnStart.style.display = 'none';
        return;
    }

    camError.style.display = 'none';

    if (!html5QrCode) {
        html5QrCode = new Html5Qrcode('reader');
    }

    const config = { fps: 10, qrbox: { width: 200, height: 200 }, aspectRatio: 1.0 };

    try {
        await html5QrCode.start({ facingMode: 'environment' }, config, onQrSuccess, () => {});
        scanning = true;
        btnStart.style.display = 'none';
        btnStop.style.display = 'inline-flex';
    } catch {
        try {
            await html5QrCode.start({ facingMode: 'user' }, config, onQrSuccess, () => {});
            scanning = true;
            btnStart.style.display = 'none';
            btnStop.style.display = 'inline-flex';
        } catch {
            camError.style.display = 'flex';
        }
    }
}

async function stopScan() {
    if (!scanning || !html5QrCode) return;
    try { await html5QrCode.stop(); } catch {}
    scanning = false;
    btnStart.style.display = 'inline-flex';
    btnStop.style.display = 'none';
}

async function onQrSuccess(decodedText) {
    // Format attendu: APPEL:{appelId}:{timestamp}
    // Vérifier et valider via le backend
    if (!appelId) {
        const appel = await demarrerAppel('QR');
        if (!appel) return;
    }

    try {
        const res = await authFetch(`/api/appels/${appelId}/valider-qr`, {
            method: 'POST',
            body: JSON.stringify({ qrToken: decodedText })
        });

        if (res && res.ok) {
            showToast('QR code validé !', 'success');
            showScanResult('Étudiant');
        } else {
            showToast('QR code invalide.', 'error');
        }
    } catch {
        showToast('Erreur de validation.', 'error');
    }
}

btnStart.addEventListener('click', startScan);
btnStop.addEventListener('click', stopScan);

// ═══════════════════════════════════════════════════════════════════════════════
// QR PRÉSENCE — GÉNÉRATION (backend)
// ═══════════════════════════════════════════════════════════════════════════════

let qrGenInited = false;
let qrTimerInterval = null;
let qrTimeLeft = 120;

async function initQRGen() {
    if (qrGenInited) return;
    qrGenInited = true;

    // Labels
    document.getElementById('qr-label-course').textContent = seanceInfo?.coursIntitule || 'Cours';
    document.getElementById('qr-label-meta').textContent = seanceInfo?.classeNom || '';
    document.getElementById('qr-fs-course').textContent = seanceInfo?.coursIntitule || '';
    document.getElementById('qr-fs-meta').textContent = seanceInfo?.classeNom || '';

    await generateSessionQR();
}

async function generateSessionQR() {
    if (!appelId) {
        const appel = await demarrerAppel('QR');
        if (!appel) return;
    }

    try {
        const res = await authFetch(`/api/appels/${appelId}/regenerer-qr`, { method: 'POST' });
        if (!res || !res.ok) {
            showToast('Erreur génération QR', 'error');
            return;
        }

        const data = await res.json();
        const qrBase64 = data.qrCodeBase64;

        // Afficher le QR code comme image
        const container = document.getElementById('qr-canvas-session');
        const containerFS = document.getElementById('qr-fullscreen-canvas');

        container.innerHTML = `<img src="data:image/png;base64,${qrBase64}" alt="QR Code" style="border-radius:8px;max-width:220px;">`;
        containerFS.innerHTML = `<img src="data:image/png;base64,${qrBase64}" alt="QR Code" style="border-radius:12px;max-width:320px;box-shadow:0 0 60px rgba(59,85,230,0.4);">`;

        // Hide expired overlay
        const expiredOverlay = document.getElementById('qr-expired-overlay');
        if (expiredOverlay) expiredOverlay.style.display = 'none';

        startQRTimer();

    } catch (err) {
        console.error('Erreur QR:', err);
    }
}

window.renewQR = generateSessionQR;

function startQRTimer() {
    clearInterval(qrTimerInterval);
    qrTimeLeft = 120;
    updateTimerDisplay();

    qrTimerInterval = setInterval(() => {
        qrTimeLeft--;
        updateTimerDisplay();

        if (qrTimeLeft <= 0) {
            clearInterval(qrTimerInterval);
            const expiredOverlay = document.getElementById('qr-expired-overlay');
            if (expiredOverlay) expiredOverlay.style.display = 'flex';
            const img = document.querySelector('#qr-canvas-session img');
            if (img) img.style.filter = 'blur(6px)';
        }
    }, 1000);
}

function updateTimerDisplay() {
    const mins = String(Math.floor(qrTimeLeft / 60)).padStart(2, '0');
    const secs = String(qrTimeLeft % 60).padStart(2, '0');
    const str = `${mins}:${secs}`;
    const expiring = qrTimeLeft <= 30;

    ['qr-timer', 'qr-fs-timer'].forEach(id => {
        const el = document.getElementById(id);
        if (!el) return;
        el.textContent = str;
        el.classList.toggle('expiring', expiring);
    });
}

// Renew button
document.getElementById('btn-refresh-qr')?.addEventListener('click', generateSessionQR);

// Fullscreen
const fsModal = document.getElementById('qr-fullscreen-modal');
const fsCloseBtn = document.getElementById('qr-fullscreen-close');

document.getElementById('btn-fullscreen-qr')?.addEventListener('click', () => {
    fsModal.style.display = 'flex';
    document.body.style.overflow = 'hidden';
});

fsCloseBtn?.addEventListener('click', closeFullscreen);
fsModal?.addEventListener('click', e => { if (e.target === fsModal) closeFullscreen(); });
document.addEventListener('keydown', e => { if (e.key === 'Escape') closeFullscreen(); });

function closeFullscreen() {
    fsModal.style.display = 'none';
    document.body.style.overflow = '';
}

// Download QR
document.getElementById('btn-download-qr')?.addEventListener('click', () => {
    const img = document.querySelector('#qr-canvas-session img');
    if (!img) return;
    const a = document.createElement('a');
    a.download = `QR_${seanceInfo?.coursIntitule || 'Seance'}.png`;
    a.href = img.src;
    a.click();
});

// ═══════════════════════════════════════════════════════════════════════════════
// QR PAR ÉTUDIANT (backend)
// ═══════════════════════════════════════════════════════════════════════════════

const modeSessionBtn = document.getElementById('qrgen-mode-session');
const modeStudentBtn = document.getElementById('qrgen-mode-student');
const sessionPanel = document.getElementById('qrgen-session-panel');
const studentPanel = document.getElementById('qrgen-student-panel');

modeSessionBtn?.addEventListener('click', () => {
    modeSessionBtn.classList.add('active');
    modeStudentBtn.classList.remove('active');
    sessionPanel.style.display = '';
    studentPanel.style.display = 'none';
});

modeStudentBtn?.addEventListener('click', () => {
    modeStudentBtn.classList.add('active');
    modeSessionBtn.classList.remove('active');
    studentPanel.style.display = '';
    sessionPanel.style.display = 'none';
    renderStudentPicker();
});

function renderStudentPicker() {
    const listEl = document.getElementById('qrgen-student-list');
    if (!listEl) return;
    listEl.innerHTML = '';

    studentsData.forEach(s => {
        const item = document.createElement('div');
        item.className = 'qrgen-stu-item';
        item.dataset.id = s.id;
        item.dataset.name = `${s.nom} ${s.prenom}`.toLowerCase();

        const initials = ((s.nom || '?')[0] + (s.prenom || '')[0]).toUpperCase();
        item.innerHTML = `
            <div class="qrgen-stu-avatar">${initials}</div>
            <div class="qrgen-stu-info">
                <div class="qrgen-stu-name">${s.nom || ''} ${s.prenom || ''}</div>
                <div class="qrgen-stu-mat">${s.matricule || ''}</div>
            </div>`;

        item.addEventListener('click', () => {
            listEl.querySelectorAll('.qrgen-stu-item').forEach(i => i.classList.remove('selected'));
            item.classList.add('selected');
            showStudentQRBackend(s);
        });

        listEl.appendChild(item);
    });
}

// Student search
document.getElementById('qrgen-student-search')?.addEventListener('input', function () {
    const q = this.value.toLowerCase();
    document.querySelectorAll('.qrgen-stu-item').forEach(item => {
        item.style.display = (item.dataset.name || '').includes(q) ? '' : 'none';
    });
});

function showStudentQRBackend(student) {
    const container = document.getElementById('qrgen-student-qr');
    container.innerHTML = '';

    const initials = ((student.nom || '?')[0] + (student.prenom || '')[0]).toUpperCase();
    const box = document.createElement('div');
    box.className = 'qrgen-stu-qr-box';
    box.innerHTML = `
        <div class="qrgen-stu-qr-name">${student.nom || ''} ${student.prenom || ''}</div>
        <div class="qrgen-stu-qr-mat">${student.matricule || ''}</div>
        <div class="qrgen-stu-btn-row">
            <button class="btn-qrgen-action btn-stu-mark" id="btn-mark-stu-${student.id}">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" width="14" height="14">
                    <polyline points="20 6 9 17 4 12"/>
                </svg>
                Marquer présent
            </button>
        </div>`;

    container.appendChild(box);

    // Marquer présent
    document.getElementById(`btn-mark-stu-${student.id}`)?.addEventListener('click', async () => {
        const s = studentsData.find(sd => sd.id === student.id);
        if (s && s.creneaux) {
            s.creneaux.forEach(c => c.present = true);
        }
        await savePresence(student.id);
        renderHourlyGrid();
        updateCounters();
        showToast(`✓ ${student.nom} ${student.prenom} marqué présent`, 'success');
    });
}

// ═══════════════════════════════════════════════════════════════════════════════
// CODE PIN
// ═══════════════════════════════════════════════════════════════════════════════

let pinTimerInterval = null;
let pinTimeLeft = 0;

document.getElementById('btn-generate-pin').addEventListener('click', generatePIN);
document.getElementById('btn-regenerate-pin').addEventListener('click', generatePIN);

async function generatePIN() {
    if (!appelId) {
        const appel = await demarrerAppel('PIN');
        if (!appel) return;
    }

    try {
        const res = await authFetch(`/api/appels/${appelId}/generer-pin`, {
            method: 'POST',
            body: JSON.stringify({ dureeMinutes: 5 })
        });

        if (!res || !res.ok) {
            showToast('Erreur génération PIN', 'error');
            return;
        }

        const data = await res.json();
        const pin = data.codePIN;
        const expiration = data.expirationPIN;

        displayPIN(pin);

        // Calculer le temps restant
        const expDate = new Date(expiration);
        pinTimeLeft = Math.max(0, Math.round((expDate - new Date()) / 1000));
        startPINTimer();

        // Afficher le bouton régénérer
        document.getElementById('btn-generate-pin').style.display = 'none';
        document.getElementById('btn-regenerate-pin').style.display = 'inline-flex';
        document.getElementById('pin-timer-wrap').style.display = 'flex';

        showToast('Code PIN généré !', 'success');

    } catch (err) {
        console.error('Erreur PIN:', err);
        showToast('Erreur de connexion', 'error');
    }
}

function displayPIN(pin) {
    const area = document.getElementById('pin-display-area');
    const digits = pin.toString().split('');

    area.innerHTML = '<div class="pin-display">' +
        digits.map((d, i) =>
            (i === 3 ? '<span class="pin-separator">·</span>' : '') +
            `<div class="pin-digit">${d}</div>`
        ).join('') +
        '</div>';
}

function startPINTimer() {
    clearInterval(pinTimerInterval);
    updatePINTimerDisplay();

    pinTimerInterval = setInterval(() => {
        pinTimeLeft--;
        updatePINTimerDisplay();

        if (pinTimeLeft <= 0) {
            clearInterval(pinTimerInterval);
            const area = document.getElementById('pin-display-area');
            area.innerHTML = '<p style="color:var(--danger);font-weight:700;font-size:0.95rem;">Code PIN expiré</p>';
            document.getElementById('btn-generate-pin').style.display = 'inline-flex';
            document.getElementById('btn-regenerate-pin').style.display = 'none';
        }
    }, 1000);
}

function updatePINTimerDisplay() {
    const el = document.getElementById('pin-timer-value');
    if (!el) return;
    const mins = String(Math.floor(pinTimeLeft / 60)).padStart(2, '0');
    const secs = String(pinTimeLeft % 60).padStart(2, '0');
    el.textContent = `${mins}:${secs}`;
    el.classList.toggle('expiring', pinTimeLeft <= 60);
}

// ═══════════════════════════════════════════════════════════════════════════════
// CLÔTURE
// ═══════════════════════════════════════════════════════════════════════════════

const modalOverlay = document.getElementById('modal-overlay');
const modalText = document.getElementById('modal-text');
const modalCancel = document.getElementById('modal-cancel');
const modalConfirm = document.getElementById('modal-confirm');

document.getElementById('btn-cloture').addEventListener('click', () => {
    let presents = 0, retards = 0, absents = 0;
    studentsData.forEach(s => {
        const creneaux = s.creneaux || [];
        const allPresent = creneaux.every(c => c.present);
        const allAbsent = creneaux.every(c => !c.present);
        if (allPresent) presents++;
        else if (allAbsent) absents++;
        else presents++;
        if (s.retard) retards++;
    });

    modalText.textContent =
        `Résumé : ${presents} présent(s), ${retards} en retard, ${absents} absent(s). Confirmer la clôture ?`;
    modalOverlay.style.display = 'flex';
});

modalCancel.addEventListener('click', () => { modalOverlay.style.display = 'none'; });
modalOverlay.addEventListener('click', e => { if (e.target === modalOverlay) modalOverlay.style.display = 'none'; });

modalConfirm.addEventListener('click', async () => {
    modalOverlay.style.display = 'none';
    await clotureAppel();
});

async function clotureAppel() {
    stopScan();
    clearInterval(qrTimerInterval);
    clearInterval(pinTimerInterval);

    if (!appelId) {
        showToast('Aucun appel à clôturer.', 'error');
        return;
    }

    try {
        const res = await authFetch(`/api/appels/${appelId}/cloturer`, { method: 'PUT' });
        if (res && res.ok) {
            showToast('Appel clôturé avec succès !', 'success', 4000);
            updateSessionStatus(false);
            setTimeout(() => { window.location.href = '/enseignant/dashboard'; }, 2500);
        } else {
            const err = await res?.text();
            showToast('Erreur: ' + (err || 'Clôture échouée'), 'error');
        }
    } catch (err) {
        showToast('Erreur de connexion', 'error');
    }
}
