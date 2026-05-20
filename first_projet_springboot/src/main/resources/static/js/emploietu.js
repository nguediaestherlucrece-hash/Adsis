/**
 * ADSIS — emploietu.js
 * Emploi du temps étudiant — navigation par semaine + offline-first
 */
import { requireRole, getUser } from './auth.js';

if (!requireRole(['ETUDIANT'])) throw new Error('Accès refusé');

// ── Init utilisateur topbar ───────────────────────────────────────────────────
const user = getUser();
if (user) {
    const nameEl   = document.getElementById('user-name-top');
    const emailEl  = document.getElementById('user-email-top');
    const avatarEl = document.getElementById('user-avatar');
    if (nameEl)   nameEl.textContent   = `${user.prenom} ${user.nom}`;
    if (emailEl)  emailEl.textContent  = user.email || '';
    if (avatarEl) avatarEl.textContent = (user.prenom?.[0] || 'E').toUpperCase();
}

// ── State semaine ─────────────────────────────────────────────────────────────
let currentMonday = getMonday(new Date());

function getMonday(date) {
    const d = new Date(date);
    const day = d.getDay();
    const diff = (day === 0) ? -6 : 1 - day;
    d.setDate(d.getDate() + diff);
    d.setHours(0, 0, 0, 0);
    return d;
}

/**
 * ✅ FIX BUG UTC : on construit la chaîne YYYY-MM-DD à partir des composantes LOCALES.
 */
function toDateString(date) {
    const y = date.getFullYear();
    const m = String(date.getMonth() + 1).padStart(2, '0');
    const d = String(date.getDate()).padStart(2, '0');
    return `${y}-${m}-${d}`;
}

function formatWeekLabel(monday) {
    const saturday = new Date(monday);
    saturday.setDate(monday.getDate() + 5);
    const opts = { day: '2-digit', month: 'short' };
    return `Semaine du ${monday.toLocaleDateString('fr-FR', opts)} au ${saturday.toLocaleDateString('fr-FR', opts)}`;
}

// ── Constantes de grille ───────────────────────────────────────────────────────
/** Jours normalisés (sans accent) pour la correspondance avec l'API */
const JOURS = ['lundi', 'mardi', 'mercredi', 'jeudi', 'vendredi', 'samedi'];

/** Grille fixe 08h00 → 18h00 (10 créneaux d'1h), comme la vue enseignant */
const GRID_START = 8;
const GRID_END   = 18;

// ── Calcul de durée par minutes (évite les off-by-one sur les heures) ────────
function parseDurationH(heureDebut, heureFin) {
    if (!heureDebut || !heureFin) return 1;
    const toMin = t => { const p = t.split(':').map(Number); return p[0] * 60 + (p[1] || 0); };
    const diff = toMin(heureFin) - toMin(heureDebut);
    return Math.max(1, Math.ceil(diff / 60));
}

/**
 * Returns true if the current time is within [-15min, +15min] of the course window.
 * heureDebut / heureFin are "HH:MM" or "HH:MM:SS" strings.
 */
function isSessionActive(heureDebut, heureFin) {
    if (!heureDebut || !heureFin) return false;
    const now = new Date();
    const nowMin = now.getHours() * 60 + now.getMinutes();
    const toMin = t => { const p = t.split(':').map(Number); return p[0] * 60 + (p[1] || 0); };
    const start = toMin(heureDebut) - 15;  // 15 min before start
    const end   = toMin(heureFin)   + 15;  // 15 min after end
    return nowMin >= start && nowMin <= end;
}

// ── Chargement EDT ────────────────────────────────────────────────────────────
async function loadEmploiDuTemps() {
    const tbody       = document.getElementById('edt-body');
    const weekLabel   = document.getElementById('week-label');
    const offlineNote = document.getElementById('offline-edt-notice');
    if (!tbody) return;

    if (weekLabel) weekLabel.textContent = formatWeekLabel(currentMonday);
    tbody.innerHTML = '<tr><td colspan="7" class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary me-2"></div>Chargement…</td></tr>';

    const cacheKey = `etu_edt_${toDateString(currentMonday)}`;
    const cached   = localStorage.getItem(cacheKey);

    try {
        const token = localStorage.getItem('jwt_token') || sessionStorage.getItem('jwt_token') || '';
        const res = await fetch(`/api/etudiant/emploi-du-temps/semaine?date=${toDateString(currentMonday)}`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!res.ok) throw new Error(`HTTP ${res.status}`);
        const seances = await res.json();
        localStorage.setItem(cacheKey, JSON.stringify(seances));
        if (offlineNote) offlineNote.classList.add('d-none');
        renderEDT(seances, tbody);
    } catch (err) {
        console.warn('[emploi] Hors-ligne ou erreur:', err);
        if (cached) {
            if (offlineNote) offlineNote.classList.remove('d-none');
            renderEDT(JSON.parse(cached), tbody);
        } else {
            tbody.innerHTML = '<tr><td colspan="7" class="text-center text-warning py-4"><i class="bi bi-wifi-off me-2"></i>Données hors ligne indisponibles pour cette semaine</td></tr>';
        }
    }
}

// ── Rendu de l'emploi du temps ────────────────────────────────────────────────
function renderEDT(seances, tbody) {
    // 1. Grouper par jour normalisé
    const matrix = {};
    JOURS.forEach(j => { matrix[j] = []; });
    seances.forEach(s => {
        const jourNorm = (s.jour || '').toLowerCase().normalize('NFD').replace(/[\u0300-\u036f]/g, '');
        if (matrix[jourNorm] !== undefined) matrix[jourNorm].push(s);
    });

    // 2. Créneaux fixes : 08:00, 09:00 … 17:00
    const timeSlots = [];
    for (let h = GRID_START; h < GRID_END; h++) {
        timeSlots.push(String(h).padStart(2, '0') + ':00');
    }

    // 3. Pré-calculer occupied[dayIdx][slotIdx]
    //    { seance, rowspan } → cellule principale
    //    { covered: true }  → absorbée par le rowspan
    const occupied = {};
    JOURS.forEach((_, i) => { occupied[i] = {}; });

    JOURS.forEach((jour, dayIdx) => {
        (matrix[jour] || []).forEach(s => {
            if (!s.heureDebut) return;
            const sh = parseInt(s.heureDebut.split(':')[0], 10);
            if (sh < GRID_START || sh >= GRID_END) return;
            const slotIdx = sh - GRID_START;
            // Calcul par minutes — pas d'off-by-one
            const rowspan = Math.min(
                parseDurationH(s.heureDebut, s.heureFin),
                GRID_END - sh
            );
            if (!occupied[dayIdx][slotIdx]) {
                occupied[dayIdx][slotIdx] = { seance: s, rowspan };
            }
            for (let r = 1; r < rowspan; r++) {
                if (!occupied[dayIdx][slotIdx + r]) {
                    occupied[dayIdx][slotIdx + r] = { covered: true };
                }
            }
        });
    });

    // 4. Construire le tbody
    tbody.innerHTML = '';

    // Cas semaine vide : afficher quand même la grille avec un message
    if (seances.length === 0) {
        timeSlots.forEach((slot, si) => {
            const tr = document.createElement('tr');
            tr.appendChild(buildTimeCell(slot));
            if (si === 0) {
                const td = document.createElement('td');
                td.colSpan = 6;
                td.rowSpan = timeSlots.length;
                td.className = 'text-center align-middle text-muted';
                td.innerHTML = '<i class="bi bi-calendar-x fs-3 d-block mb-2"></i>Aucun cours cette semaine';
                tr.appendChild(td);
            }
            tbody.appendChild(tr);
        });
        return;
    }

    timeSlots.forEach((slot, slotIdx) => {
        const tr = document.createElement('tr');
        tr.appendChild(buildTimeCell(slot));

        JOURS.forEach((_, dayIdx) => {
            const occ = occupied[dayIdx][slotIdx];
            // Absorbé par un rowspan → on ne crée pas de <td>
            if (occ && occ.covered) return;

            const td = document.createElement('td');
            td.className = 'p-1 align-top';

            if (occ && occ.seance) {
                const s       = occ.seance;
                const rowspan = occ.rowspan;
                if (rowspan > 1) td.rowSpan = rowspan;
                const hDeb = (s.heureDebut || slot).substring(0, 5);
                const hFin = (s.heureFin  || '').substring(0, 5);
                const minH = rowspan * 46; // ~46px par heure

                // Check if today and within the 15-min window
                const todayStr = toDateString(new Date());
                const isToday  = s.date === todayStr;
                const active   = isToday && isSessionActive(hDeb, hFin);

                const activeBadge = active
                    ? `<div style="font-size:.65rem;background:#16a34a;color:#fff;border-radius:4px;padding:1px 5px;display:inline-block;margin-top:3px;">🟢 Appel ouvert</div>`
                    : (isToday ? `<div style="font-size:.65rem;background:#64748b;color:#fff;border-radius:4px;padding:1px 5px;display:inline-block;margin-top:3px;">⏳ Hors fenêtre</div>` : '');

                td.innerHTML = `
                    <div class="cours-card rounded-2 p-2" style="
                        background: linear-gradient(145deg,${active ? '#dcfce7,#bbf7d0' : '#eff6ff,#dbeafe'});
                        border-left: 4px solid ${active ? '#16a34a' : '#3b82f6'};
                        min-height: ${minH}px;
                        height: 100%;
                        display: flex; flex-direction: column; justify-content: center;
                        cursor: ${s.seanceId ? 'pointer' : 'default'};
                    " data-seance-id="${s.seanceId || ''}">
                        <div class="fw-semibold" style="font-size:.83rem;color:${active ? '#15803d' : '#1d4ed8'};">${s.matiere || '—'}</div>
                        <div style="font-size:.72rem;color:#64748b;margin-top:3px;">🕐 ${hDeb} – ${hFin}</div>
                        ${s.salle      ? `<div style="font-size:.7rem;color:#64748b;">📍 ${s.salle}</div>`      : ''}
                        ${s.enseignant ? `<div style="font-size:.7rem;color:#64748b;">👤 ${s.enseignant}</div>` : ''}
                        ${activeBadge}
                    </div>`;

                // Click: redirect to attendance page for this session
                if (s.seanceId) {
                    td.style.cursor = 'pointer';
                    td.querySelector('.cours-card').addEventListener('click', () => {
                        window.location.href = `/etudiant/appel?seanceId=${s.seanceId}`;
                    });
                }
            } else {
                td.style.cssText = 'background:#fafbfc;min-width:80px;';
            }

            tr.appendChild(td);
        });

        tbody.appendChild(tr);
    });
}

function buildTimeCell(slot) {
    const td = document.createElement('td');
    td.className = 'fw-semibold text-center align-top small';
    td.style.cssText = 'width:60px;white-space:nowrap;color:#64748b;background:#f8fafc;padding:6px 4px;';
    td.textContent = slot;
    return td;
}

// ── Navigation semaine ────────────────────────────────────────────────────────
document.addEventListener('DOMContentLoaded', () => {
    document.getElementById('btn-prev-week')?.addEventListener('click', () => {
        currentMonday.setDate(currentMonday.getDate() - 7);
        loadEmploiDuTemps();
    });
    document.getElementById('btn-next-week')?.addEventListener('click', () => {
        currentMonday.setDate(currentMonday.getDate() + 7);
        loadEmploiDuTemps();
    });
    loadEmploiDuTemps();
});