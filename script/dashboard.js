import { Api } from './api.js';

const API_BASE = 'http://localhost:8081/api';

// Interroge l'API et retourne les données en JSON
function apiFetch(path) {
    return fetch(`${API_BASE}${path}`, { headers: { 'Content-Type': 'application/json' } })
        .then(r => r.ok ? r.json() : [])
        .catch(() => []);
}

// Anime le défilement d'un nombre (0 jusqu'à la valeur finale)
function animateCount(el, target, duration = 700) {
    if (!el) return;
    const start = performance.now();
    const update = (now) => {
        const t = Math.min((now - start) / duration, 1);
        el.textContent = Math.round(t * target);
        if (t < 1) requestAnimationFrame(update);
    };
    requestAnimationFrame(update);
}

const AVATAR_COLORS = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#a855f7', '#14b8a6', '#f97316'];

// Assigne une couleur aléatoire constante basée sur l'ID de l'utilisateur
function avatarColor(id) { return AVATAR_COLORS[id % AVATAR_COLORS.length]; }

// Formate et affiche la date actuelle au chargement
document.addEventListener('DOMContentLoaded', () => {
    const dateEl = document.getElementById('dashboardDate');
    if (dateEl) {
        const now = new Date();
        dateEl.textContent = now.toLocaleDateString('fr-FR', {
            weekday: 'long', day: 'numeric', month: 'long', year: 'numeric'
        });
    }
    loadDashboard();
});

// Lance la récupération parallèle de tous les éléments du tableau de bord
async function loadDashboard() {
    const [staff, cycles, filieres, classes] = await Promise.all([
        Api.getStaff().catch(() => []),
        apiFetch('/cycles'),
        apiFetch('/filieres'),
        apiFetch('/classes'),
    ]);

    renderKPIs(staff, cycles, filieres, classes);
    renderStaffHealth(staff);
    renderCyclesList(cycles, filieres, classes);
    renderStaffList(staff);
    renderClassesList(classes, filieres);
}

// Met à jour les compteurs chiffrés du haut de page
function renderKPIs(staff, cycles, filieres, classes) {
    const surveillants = staff.filter(u => u.roleNames?.includes('SURVEILLANT'));
    const aps = staff.filter(u => u.roleNames?.includes('ASSISTANT_PEDAGOGIQUE'));

    animateCount(document.getElementById('kpi-staff'), staff.length);
    animateCount(document.getElementById('kpi-surveil'), surveillants.length);
    animateCount(document.getElementById('kpi-ap'), aps.length);
    animateCount(document.getElementById('kpi-cycles'), cycles.length);
    animateCount(document.getElementById('kpi-filieres'), filieres.length);
    animateCount(document.getElementById('kpi-classes'), classes.length);
}

// Construit les barres de proportion d'utilisateurs actifs/bloqués
function renderStaffHealth(staff) {
    const total = staff.length || 1;
    const actifs = staff.filter(u => u.statut === 'ACTIF').length;
    const bloques = staff.filter(u => u.statut !== 'ACTIF').length;
    const surveil = staff.filter(u => u.roleNames?.includes('SURVEILLANT')).length;
    const ap = staff.filter(u => u.roleNames?.includes('ASSISTANT_PEDAGOGIQUE')).length;

    document.getElementById('label-actif').textContent = actifs;
    document.getElementById('label-bloque').textContent = bloques;
    document.getElementById('big-surveil').textContent = surveil;
    document.getElementById('big-ap').textContent = ap;

    setTimeout(() => {
        const barActif = document.getElementById('bar-actif');
        const barBloque = document.getElementById('bar-bloque');
        if (barActif) barActif.style.width = `${(actifs / total * 100).toFixed(1)}%`;
        if (barBloque) barBloque.style.width = `${(bloques / total * 100).toFixed(1)}%`;
    }, 100);
}

// Génère la section listant les cycles avec leurs filières enfants
function renderCyclesList(cycles, filieres, classes) {
    const container = document.getElementById('cyclesList');
    if (!container) return;

    if (cycles.length === 0) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-diagram-3"></i>Aucun cycle enregistré</div>`;
        return;
    }

    container.innerHTML = cycles.map(c => {
        const fils = filieres.filter(f => f.cycleId == c.id);
        const nbCls = classes.filter(cl => fils.some(f => f.id == cl.filiereId)).length;
        const annee = (c.anneeDebut && c.anneeFin) ? `${c.anneeDebut}–${c.anneeFin}` : '';

        return `
        <div class="filiere-row">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-3 d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:38px;height:38px;background:linear-gradient(135deg,#6366f1,#8b5cf6);font-size:.8rem;flex-shrink:0;">
                    ${(c.nomCycle || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${c.nomCycle || '—'}
                        ${annee ? `<span class="badge bg-light text-muted border ms-1" style="font-size:.65rem;">${annee}</span>` : ''}
                    </div>
                    <div style="font-size:.75rem;color:#94a3b8;">
                        ${fils.length} filière${fils.length !== 1 ? 's' : ''} · ${nbCls} classe${nbCls !== 1 ? 's' : ''}
                    </div>
                </div>
            </div>
            <div class="d-flex flex-wrap gap-1 justify-content-end" style="max-width:55%;">
                ${fils.slice(0, 3).map(f =>
            `<span class="badge rounded-pill" style="background:#eff6ff;color:#3b82f6;font-size:.7rem;">${f.nomFiliere}</span>`
        ).join('')}
                ${fils.length > 3 ? `<span class="badge rounded-pill bg-light text-muted" style="font-size:.7rem;">+${fils.length - 3}</span>` : ''}
            </div>
        </div>`;
    }).join('');
}

// Affiche les 6 membres du personnel les plus récents
function renderStaffList(staff) {
    const container = document.getElementById('staffList');
    if (!container) return;

    if (staff.length === 0) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-people"></i>Aucun membre du staff</div>`;
        return;
    }

    const display = staff.slice(0, 6);
    container.innerHTML = display.map(u => {
        const role = u.roleNames?.includes('SURVEILLANT') ? 'Surveillant' : 'Assistant Péd.';
        const isActif = u.statut === 'ACTIF';
        const bgColor = avatarColor(u.id || 0);
        const initials = ((u.nom || '?').charAt(0) + (u.prenom || '').charAt(0)).toUpperCase();

        return `
        <div class="entity-card">
            <div class="entity-avatar" style="background:${bgColor};">${initials}</div>
            <div class="flex-grow-1">
                <div class="entity-name">${u.nom || ''} ${u.prenom || ''}</div>
                <div class="entity-sub">${u.email || ''}</div>
            </div>
            <div class="d-flex flex-column align-items-end gap-1">
                <span class="badge rounded-pill" style="background:#f8fafc;color:#64748b;border:1px solid #e2e8f0;font-size:.68rem;">${role}</span>
                <span class="badge rounded-pill ${isActif ? 'bg-success' : 'bg-danger'}" style="font-size:.65rem;">${isActif ? 'Actif' : 'Bloqué'}</span>
            </div>
        </div>`;
    }).join('');

    if (staff.length > 6) {
        container.innerHTML += `
        <a href="gestionutilisateur.html" class="d-block text-center text-primary small mt-2 text-decoration-none">
            Voir les ${staff.length - 6} autres membres →
        </a>`;
    }
}

// Construit visuellement un aperçu des 6 premières classes
function renderClassesList(classes, filieres) {
    const container = document.getElementById('classesList');
    if (!container) return;

    if (classes.length === 0) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-mortarboard"></i>Aucune classe enregistrée</div>`;
        return;
    }

    const display = classes.slice(0, 6);
    const levelColors = ['', '#3b82f6', '#22c55e', '#f97316', '#a855f7', '#ef4444'];

    container.innerHTML = display.map(cl => {
        const filiere = filieres.find(f => f.id == cl.filiereId);
        const niv = parseInt(cl.niveau) || 0;
        const nivColor = levelColors[niv] || '#64748b';

        return `
        <div class="entity-card">
            <div class="entity-avatar" style="background:${nivColor};">
                ${cl.niveau ? `N${cl.niveau}` : '—'}
            </div>
            <div class="flex-grow-1">
                <div class="entity-name">${cl.nomClasse || '—'}</div>
                <div class="entity-sub">
                    ${filiere ? filiere.nomFiliere : '<span class="fst-italic">Sans filière</span>'}
                    ${cl.anneeAcademique ? ' · ' + cl.anneeAcademique : ''}
                </div>
            </div>
            <span class="badge bg-light text-muted border" style="font-size:.7rem;">${cl.idClasse || ''}</span>
        </div>`;
    }).join('');

    if (classes.length > 6) {
        container.innerHTML += `
        <a href="cycles.html" class="d-block text-center text-primary small mt-2 text-decoration-none">
            Voir les ${classes.length - 6} autres classes →
        </a>`;
    }
}
