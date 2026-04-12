import { requireRole, authFetch, getUser } from './auth.js';
import { Api } from './api.js';

// Guard : seuls les ADMINs accèdent au tableau de bord admin
if (!requireRole(['ADMIN'])) { /* redirection gérée par requireRole */ throw new Error('Unauthorized'); }

// authFetch préfixe déjà avec http://localhost:8081
function apiFetch(path) {
    return authFetch(path).then(r => r && r.ok ? r.json() : []).catch(() => []);
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
    
    // Topbar dynamique
    const user = getUser();
    if (user) {
        const topbarName = document.querySelector('.topbar .fw-bold.small');
        if (topbarName) topbarName.textContent = `${user.prenom || ''} ${user.nom || ''}`.trim() || 'Administrateur';
        const topbarEmail = document.querySelector('.topbar .text-muted');
        if (topbarEmail && topbarEmail.id !== 'dashboardDate') topbarEmail.textContent = user.email || '';
        const avatar = document.querySelector('.topbar .rounded-circle');
        if (avatar) avatar.textContent = ((user.prenom?.charAt(0) || '') + (user.nom?.charAt(0) || '')).toUpperCase() || 'A';
    }

    loadDashboard();
});

// Lance la récupération parallèle de tous les éléments du tableau de bord
async function loadDashboard() {
    const [staff, cycles, niveaux, filieres, specialites, classes] = await Promise.all([
        Api.getStaff().catch(() => []),
        apiFetch('/api/cycles'),
        apiFetch('/api/niveaux'),
        apiFetch('/api/filieres'),
        apiFetch('/api/specialites'),
        apiFetch('/api/classes'),
    ]);

    renderKPIs(staff, cycles, filieres, classes);
    renderStaffHealth(staff);
    renderCyclesList(cycles, niveaux, filieres, classes);
    renderStaffList(staff);
    renderClassesList(classes, specialites, filieres);
}

// Met à jour les compteurs chiffrés du haut de page
function renderKPIs(staff, cycles, filieres, classes) {
    const enseignants = staff.filter(u => u.roleNames?.includes('ENSEIGNANT'));
    const surveillants = staff.filter(u => u.roleNames?.includes('SURVEILLANT'));
    const aps = staff.filter(u => u.roleNames?.includes('ASSISTANT_PEDAGOGIQUE'));

    animateCount(document.getElementById('kpi-staff'), staff.length);
    animateCount(document.getElementById('kpi-enseign'), enseignants.length);
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
    const enseignants = staff.filter(u => u.roleNames?.includes('ENSEIGNANT')).length;
    const surveil = staff.filter(u => u.roleNames?.includes('SURVEILLANT')).length;
    const ap = staff.filter(u => u.roleNames?.includes('ASSISTANT_PEDAGOGIQUE')).length;

    document.getElementById('label-actif').textContent = actifs;
    document.getElementById('label-bloque').textContent = bloques;
    const bigEns = document.getElementById('big-enseign');
    if (bigEns) bigEns.textContent = enseignants;
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
function renderCyclesList(cycles, niveaux, filieres, classes) {
    const container = document.getElementById('cyclesList');
    if (!container) return;

    if (cycles.length === 0) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-diagram-3"></i>Aucun cycle enregistré</div>`;
        return;
    }

    container.innerHTML = cycles.map(c => {
        // Cycle -> Niveau -> Filière
        const nivIds = niveaux.filter(n => n.cycleId == c.id).map(n => n.id);
        const fils = filieres.filter(f => nivIds.includes(f.niveauId));
        // Trouver le nombre total de classes sous ce cycle ne peut pas se faire directement, ça se fait via Specialite. 
        // Comme on n'a pas inclus `specialites` dans la boucle simple ici, on se contente du compte ou on le zappe...
        // Pour être sûr, on passe juste le nombre de filières. Mais c'est plus joli avec les classes. 
        // L'utilisateur n'a pas montré quoi garder, mais laissons 0 classes si pas possible, ou retirons l'affichage.
        const nbCls = 0; // Temporairement car on ne passe pas spécialités ici (sauf si on ajoute)
        const annee = c.anneeAcademique || ((c.anneeDebut && c.anneeFin) ? `${c.anneeDebut}–${c.anneeFin}` : '');

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
                        ${fils.length} filière${fils.length !== 1 ? 's' : ''}
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
        let role = 'Utilisateur';
        if (u.roleNames?.includes('ENSEIGNANT')) role = 'Enseignant';
        else if (u.roleNames?.includes('SURVEILLANT')) role = 'Surveillant';
        else if (u.roleNames?.includes('ASSISTANT_PEDAGOGIQUE')) role = 'Assistant Péd.';

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
function renderClassesList(classes, specialites, filieres) {
    const container = document.getElementById('classesList');
    if (!container) return;

    if (classes.length === 0) {
        container.innerHTML = `<div class="empty-state"><i class="bi bi-mortarboard"></i>Aucune classe enregistrée</div>`;
        return;
    }

    const display = classes.slice(0, 6);

    container.innerHTML = display.map(cl => {
        const spId = cl.specialiteId || cl.speciliteId;
        const spec = specialites.find(s => s.id == spId);
        const filiere = spec ? filieres.find(f => f.id == spec.filiereId) : null;

        // Trouver la couleur en fonction du nom pour avoir un code propre
        const nivLabel = cl.nomClasse ? cl.nomClasse.substring(0, 2).toUpperCase() : 'Cl';

        return `
        <div class="entity-card">
            <div class="entity-avatar" style="background:#3b82f6;">
                ${nivLabel}
            </div>
            <div class="flex-grow-1">
                <div class="entity-name">${cl.nomClasse || '—'}</div>
                <div class="entity-sub">
                    ${spec ? (spec.nom || spec.nomSpecialite) : '<span class="fst-italic text-muted">Spécialité inconnue</span>'}
                    ${filiere ? ' (' + filiere.nomFiliere + ')' : ''}
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
