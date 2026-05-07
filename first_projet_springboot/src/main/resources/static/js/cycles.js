import { requireRole, authFetch } from './auth.js';

// Guard : réservé aux administrateurs
if (!requireRole(['ADMIN'])) { throw new Error('Unauthorized'); }

const API_BASE = 'http://localhost:8081/api';

// État global de l'application
let cycles = [];
let niveaux = [];
let filieres = [];
let specialites = [];
let classes = [];
let anneesAcademiques = [];
let semestres = [];

let activeTab = 'cycles';

// Affiche une notification en haut de l'écran
function showToast(message, type = 'info') {
    const cfg = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger: { icon: 'bi-x-circle-fill', label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info: { icon: 'bi-info-circle-fill', label: 'Info', bg: '#1a6fa8', delay: 4000 },
    };
    const c = cfg[type] || cfg.info;
    const container = document.getElementById('toastContainer');
    const el = document.createElement('div');
    el.className = 'toast border-0 rounded-3 shadow-lg overflow-hidden';
    el.setAttribute('role', 'alert');
    el.setAttribute('data-bs-delay', c.delay);
    el.style.minWidth = '280px';
    el.innerHTML = `
        <div class="toast-header border-0 text-white py-2 px-3" style="background:${c.bg}">
            <i class="bi ${c.icon} me-2"></i>
            <strong class="me-auto">${c.label}</strong>
            <button type="button" class="btn-close btn-close-white ms-2" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body text-dark bg-white small py-2 px-3">${message}</div>`;
    container.appendChild(el);
    const toast = new bootstrap.Toast(el);
    toast.show();
    el.addEventListener('hidden.bs.toast', () => el.remove());
}

// Affiche une fenêtre de confirmation et renvoie la réponse utilisateur
function showConfirm(title, message) {
    return new Promise((resolve) => {
        document.getElementById('confirmModalTitle').innerText = title;
        document.getElementById('confirmModalMessage').innerText = message;
        const modalEl = document.getElementById('confirmModal');
        const modal = new bootstrap.Modal(modalEl);
        let resolved = false;
        const done = (val) => {
            if (resolved) return;
            resolved = true;
            document.getElementById('confirmOkBtn').replaceWith(document.getElementById('confirmOkBtn').cloneNode(true));
            document.getElementById('confirmCancelBtn').replaceWith(document.getElementById('confirmCancelBtn').cloneNode(true));
            modal.hide();
            resolve(val);
        };
        document.getElementById('confirmOkBtn').addEventListener('click', () => done(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => done(false), { once: true });
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}

// Effectue un appel réseau avec timeout et retourne la réponse JSON
async function apiFetch(path, options = {}) {
    const controller = new AbortController();
    const timer = setTimeout(() => controller.abort(), 8000);
    try {
        const res = await fetch(`${API_BASE}${path}`, {
            headers: { 'Content-Type': 'application/json', ...options.headers },
            signal: controller.signal,
            ...options
        });
        clearTimeout(timer);
        if (res.status === 204) return null;
        if (!res.ok) {
            const text = await res.text();
            throw new Error(text || `Erreur HTTP ${res.status}`);
        }
        return res.json();
    } catch (e) {
        clearTimeout(timer);
        throw e;
    }
}

// Extrait un message d'erreur lisible depuis une exception
function parseError(e) {
    const raw = e?.message || String(e);
    try {
        const match = raw.match(/\{.*\}/s);
        if (match) {
            const parsed = JSON.parse(match[0]);
            return parsed.message || parsed.error || raw;
        }
    } catch (_) { }
    return raw;
}

// Génère la liste des années scolaires (5 ans passés à 8 ans futurs)
function generateAnneesScolaires() {
    const now = new Date().getFullYear();
    const years = [];
    for (let y = now - 5; y <= now + 8; y++) years.push(`${y}-${y + 1}`);
    return years;
}

// Extrait l'année académique lisible depuis un objet cycle
function getAnneeAcademiqueFromCycle(cycle) {
    if (!cycle) return '';
    if (cycle.anneeDebut && cycle.anneeFin) return `${cycle.anneeDebut}-${cycle.anneeFin}`;
    if (cycle.anneeDebut) return `${cycle.anneeDebut}-${cycle.anneeDebut + 1}`;
    return '';
}

// Remplit la liste déroulante des années scolaires dans la modale cycle
function populateAnneeScolaireSelect(selectedValue = '') {
    const sel = document.getElementById('cycleAnneeScolaire');
    const years = generateAnneesScolaires();
    sel.innerHTML = `<option value="">-- Sélectionnez une année scolaire --</option>`;
    years.forEach(y => {
        const opt = document.createElement('option');
        opt.value = y;
        opt.textContent = y;
        if (y === selectedValue) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Remplit la liste déroulante des années scolaires dans la modale classe
function populateAnneeScolaireClasseSelect(selectedValue = '') {
    const sel = document.getElementById('classeAnneeScolaire');
    if (!sel) return;
    const years = generateAnneesScolaires();
    sel.innerHTML = `<option value="">-- Sélectionnez --</option>`;
    years.forEach(y => {
        const opt = document.createElement('option');
        opt.value = y;
        opt.textContent = y;
        if (y === selectedValue) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Charge toutes les données en parallèle avec fallback individuel par endpoint
async function loadAll() {
    const safeLoad = (path) => apiFetch(path).catch(e => {
        console.warn(`Endpoint ${path} indisponible :`, e.message);
        return [];
    });
    try {
        [cycles, niveaux, filieres, specialites, classes, anneesAcademiques, semestres] = await Promise.all([
            safeLoad('/cycles'),
            safeLoad('/niveaux'),
            safeLoad('/filieres'),
            safeLoad('/specialites'),
            safeLoad('/classes'),
            safeLoad('/annees-academiques'),
            safeLoad('/semestres'),
        ]);
    } catch (e) {
        showToast('Erreur critique de chargement : ' + parseError(e), 'danger');
        cycles = []; niveaux = []; filieres = []; specialites = []; classes = []; anneesAcademiques = []; semestres = [];
    }
    updateCounts();
    renderCurrentTab();
}

// Met à jour les compteurs affichés dans chaque onglet
function updateCounts() {
    const counts = { cycles: cycles.length, niveaux: niveaux.length, filieres: filieres.length, specialites: specialites.length, classes: classes.length, annees: anneesAcademiques.length, semestres: semestres.length };
    const labels = { cycles: 'Cycles', niveaux: 'Niveaux', filieres: 'Filières', specialites: 'Spécialités', classes: 'Classes', annees: 'Années Scolaires', semestres: 'Semestres' };
    document.querySelectorAll('.filter-tab').forEach(t => {
        const name = t.dataset.tab;
        if (labels[name] !== undefined) t.textContent = `${labels[name]} (${counts[name] ?? 0})`;
    });
}

// Rafraîchit le contenu du tableau actif
function renderCurrentTab() {
    if (activeTab === 'cycles') renderCycles();
    if (activeTab === 'filieres') renderFilieres();
    if (activeTab === 'niveaux') renderNiveaux();
    if (activeTab === 'specialites') renderSpecialites();
    if (activeTab === 'classes') renderClasses();
    if (activeTab === 'annees') renderAnnees();
    if (activeTab === 'semestres') renderSemestres();
}

// ─── CYCLES ─────────────────────────────────────────────────────────────────

// Affiche la liste des cycles dans le tableau
// Hierarchy: Cycle → Filière (direct children via f.cycleId)
function renderCycles() {
    const tbody = document.getElementById('cyclesTableBody');
    if (!tbody) return;
    if (cycles.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucun cycle enregistré</td></tr>`;
        return;
    }
    tbody.innerHTML = cycles.map(c => {
        const filieresDuCycle = filieres.filter(f => f.cycleId == c.id);
        const anneeLabel = getAnneeAcademiqueFromCycle(c);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${c.nomCycle || '—'}</td>
            <td><span class="badge bg-primary">${c.idCycle || '—'}</span></td>
            <td class="text-muted small">${anneeLabel || '—'}</td>
            <td class="small">
                ${filieresDuCycle.length > 0
                ? filieresDuCycle.map(f => `<span class="badge bg-secondary me-1 mb-1">${f.nomFiliere}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune filière</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-primary border-0 me-1" title="Modifier" onclick="openEditCycle(${c.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger border-0" title="Supprimer" onclick="deleteCycle(${c.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

function openCreateCycle() {
    document.getElementById('cycleModalTitle').textContent = 'Nouveau cycle';
    document.getElementById('cycleId').value = '';
    document.getElementById('cycleIdCycle').value = '';
    document.getElementById('cycleNom').value = '';
    document.getElementById('cycleAnneeCreation').value = '';
    const now = new Date().getFullYear();
    populateAnneeScolaireSelect(`${now}-${now + 1}`);
    new bootstrap.Modal(document.getElementById('cycleModal')).show();
}

function openEditCycle(id) {
    const c = cycles.find(x => x.id === id);
    if (!c) return;
    document.getElementById('cycleModalTitle').textContent = 'Modifier le cycle';
    document.getElementById('cycleId').value = c.id;
    document.getElementById('cycleIdCycle').value = c.idCycle || '';
    document.getElementById('cycleNom').value = c.nomCycle || '';
    document.getElementById('cycleAnneeCreation').value = c.anneeDeCreation ?? '';
    populateAnneeScolaireSelect(getAnneeAcademiqueFromCycle(c));
    new bootstrap.Modal(document.getElementById('cycleModal')).show();
}

async function saveCycle() {
    const id = document.getElementById('cycleId').value;
    const anneeScolaire = document.getElementById('cycleAnneeScolaire').value;
    if (!anneeScolaire) { showToast('Veuillez sélectionner une année scolaire.', 'warning'); return; }
    const parts = anneeScolaire.split('-');
    const payload = {
        idCycle: document.getElementById('cycleIdCycle').value.trim(),
        nomCycle: document.getElementById('cycleNom').value.trim(),
        anneeDeCreation: parseInt(document.getElementById('cycleAnneeCreation').value) || null,
        anneeDebut: parseInt(parts[0]),
        anneeFin: parseInt(parts[1]),
    };
    if (!payload.nomCycle) { showToast('Le nom du cycle est requis.', 'warning'); return; }
    try {
        if (id) {
            const updated = await apiFetch(`/cycles/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            cycles = cycles.map(c => c.id == id ? updated : c);
            showToast('Cycle modifié avec succès.', 'success');
        } else {
            const created = await apiFetch('/cycles', { method: 'POST', body: JSON.stringify(payload) });
            cycles.push(created);
            showToast('Cycle créé avec succès.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('cycleModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

async function deleteCycle(id) {
    const c = cycles.find(x => x.id === id);
    if (!await showConfirm(`Supprimer "${c?.nomCycle}" ?`, 'Cette action est irréversible.')) return;
    try {
        await apiFetch(`/cycles/${id}`, { method: 'DELETE' });
        cycles = cycles.filter(x => x.id !== id);
        updateCounts(); renderCurrentTab();
        showToast('Cycle supprimé.', 'success');
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Remplit la liste déroulante des cycles
function populateCycleSelect(selectId, selectedId = null) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = `<option value="">-- Sélectionnez un cycle --</option>`;
    cycles.forEach(c => {
        const annee = getAnneeAcademiqueFromCycle(c);
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = annee ? `${c.nomCycle} (${annee})` : c.nomCycle;
        if (selectedId != null && c.id == selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
}


function renderFilieres() {
    const tbody = document.getElementById('filieresTableBody');
    if (!tbody) return;
    if (filieres.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Aucune filière enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = filieres.map(f => {
        const cycle = cycles.find(c => c.id == f.cycleId);
        // Find niveaux linked to this filière (Niveau has filiereId)
        const niveauxF = niveaux.filter(n => n.filiereId == f.id);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${f.nomFiliere || '—'}</td>
            <td><span class="badge bg-info text-dark">${f.idFiliere || '—'}</span></td>
            <td>${cycle
                ? `<span class="badge bg-primary">${cycle.nomCycle}</span>`
                : '<span class="text-muted fst-italic">—</span>'}
            </td>
            <td class="text-muted small" style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${f.description || ''}">
                ${f.description || '<span class="fst-italic">—</span>'}
            </td>
            <td class="small">
                ${niveauxF.length > 0
                ? niveauxF.map(n => `<span class="badge me-1 mb-1" style="background:#7c3aed;color:white;">${n.nom || n.nomNiveau}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucun niveau</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-warning border-0 me-1" title="Attribuer des niveaux" onclick="openAssignNiveaux(${f.id})">
                    <i class="bi bi-plus-circle"></i>
                </button>
                <button class="btn btn-sm btn-outline-primary border-0 me-1" title="Modifier" onclick="openEditFiliere(${f.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger border-0" title="Supprimer" onclick="deleteFiliere(${f.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

function openCreateFiliere() {
    if (cycles.length === 0) {
        showToast('Veuillez d\'abord créer un cycle avant de créer une filière.', 'warning');
        return;
    }
    document.getElementById('filiereModalTitle').textContent = 'Nouvelle filière';
    document.getElementById('filiereId').value = '';
    document.getElementById('filiereIdFiliere').value = '';
    document.getElementById('filiereNom').value = '';
    document.getElementById('filiereDescription').value = '';
    populateCycleSelect('filiereCycleId');
    new bootstrap.Modal(document.getElementById('filiereModal')).show();
}

function openEditFiliere(id) {
    const f = filieres.find(x => x.id === id);
    if (!f) return;
    document.getElementById('filiereModalTitle').textContent = 'Modifier la filière';
    document.getElementById('filiereId').value = f.id;
    document.getElementById('filiereIdFiliere').value = f.idFiliere || '';
    document.getElementById('filiereNom').value = f.nomFiliere || '';
    document.getElementById('filiereDescription').value = f.description || '';
    populateCycleSelect('filiereCycleId', f.cycleId);
    new bootstrap.Modal(document.getElementById('filiereModal')).show();
}

async function saveFiliere() {
    const id = document.getElementById('filiereId').value;
    const cycleId = document.getElementById('filiereCycleId').value;
    if (!cycleId) { showToast('Veuillez sélectionner un cycle.', 'warning'); return; }
    const nom = document.getElementById('filiereNom').value.trim();
    if (!nom) { showToast('Le nom de la filière est requis.', 'warning'); return; }
    const payload = {
        idFiliere: document.getElementById('filiereIdFiliere').value.trim(),
        nomFiliere: nom,
        description: document.getElementById('filiereDescription').value.trim(),
        cycleId: parseInt(cycleId),
    };
    try {
        if (id) {
            const updated = await apiFetch(`/filieres/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            filieres = filieres.map(f => f.id == id ? { ...updated, cycleId: payload.cycleId } : f);
            showToast('Filière modifiée.', 'success');
        } else {
            const created = await apiFetch('/filieres', { method: 'POST', body: JSON.stringify(payload) });
            if (!created.cycleId) created.cycleId = payload.cycleId;
            filieres.push(created);
            showToast('Filière créée.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('filiereModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

async function deleteFiliere(id) {
    const f = filieres.find(x => x.id === id);
    if (!await showConfirm(`Supprimer "${f?.nomFiliere}" ?`, 'Cette action est irréversible.')) return;
    try {
        await apiFetch(`/filieres/${id}`, { method: 'DELETE' });
        filieres = filieres.filter(x => x.id !== id);
        updateCounts(); renderCurrentTab();
        showToast('Filière supprimée.', 'success');
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Remplit la liste déroulante des filières
function populateFiliereSelect(selectId, selectedId = null) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = `<option value="">-- Sélectionnez une filière --</option>`;
    filieres.forEach(f => {
        const cycle = cycles.find(c => c.id == f.cycleId);
        const opt = document.createElement('option');
        opt.value = f.id;
        opt.textContent = cycle ? `${f.nomFiliere} (${cycle.nomCycle})` : f.nomFiliere;
        if (selectedId != null && f.id == selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Assign niveaux to a filière (via the Niveau owning side of the ManyToMany)
function openAssignNiveaux(filiereId) {
    const filiere = filieres.find(f => f.id == filiereId);
    document.getElementById('assignNiveauTitle').textContent =
        filiere ? `Attribuer des niveaux — ${filiere.nomFiliere}` : 'Attribuer des niveaux';
    renderAssignNiveauList(filiereId);
    new bootstrap.Modal(document.getElementById('assignNiveauModal')).show();
}

function renderAssignNiveauList(filiereId) {
    const container = document.getElementById('assignNiveauList');
    if (niveaux.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucun niveau disponible. Créez-en d'abord un.</p>`;
        return;
    }
    const sorted = [...niveaux].sort((a, b) => {
        const aAssigned = a.filiereId == filiereId;
        const bAssigned = b.filiereId == filiereId;
        return bAssigned - aAssigned;
    });
    container.innerHTML = sorted.map(n => {
        const isAssigned = n.filiereId == filiereId;
        const badge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribué</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribué</span>`;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#7c3aed;font-size:0.8rem;flex-shrink:0;">
                    ${(n.nom || n.nomNiveau || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${n.nom || n.nomNiveau || '—'} <span class="text-muted fw-normal">(${n.idNiveau || '?'})</span></div>
                    <div class="mt-1">${badge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${isAssigned ? 'btn-outline-danger' : 'btn-outline-success'} rounded-3 px-3"
                    onclick="doAssignNiveau(${n.id},${filiereId},${isAssigned})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

// Assign or remove a niveau from a filière (simple filiereId overwrite)
async function doAssignNiveau(niveauId, filiereId, isCurrentlyAssigned) {
    const n = niveaux.find(x => x.id == niveauId);
    if (!n) return;

    const newFiliereId = isCurrentlyAssigned ? null : filiereId;
    const payload = { idNiveau: n.idNiveau, nom: n.nom || n.nomNiveau, filiereId: newFiliereId };
    try {
        const updated = await apiFetch(`/niveaux/${niveauId}`, { method: 'PUT', body: JSON.stringify(payload) });
        if (updated.filiereId === undefined) updated.filiereId = newFiliereId;
        niveaux = niveaux.map(x => x.id == niveauId ? updated : x);
        showToast(`Niveau "${n.nom || n.nomNiveau}" ${isCurrentlyAssigned ? 'retiré' : 'attribué'}.`, 'success');
        renderAssignNiveauList(filiereId);
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// ─── NIVEAUX ────────────────────────────────────────────────────────────────
// Hierarchy: Niveau belongs to Filière (via filiereId), Niveau → Spécialité (OneToMany)

function renderNiveaux() {
    const tbody = document.getElementById('niveauxTableBody');
    if (!tbody) return;
    if (niveaux.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucun niveau enregistré</td></tr>`;
        return;
    }
    tbody.innerHTML = niveaux.map(n => {
        // Filière parente
        const filiere = filieres.find(f => f.id == n.filiereId);
        // Spécialités enfants
        const specialitesN = specialites.filter(s => s.niveauId == n.id);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${n.nom || n.nomNiveau || '—'}</td>
            <td><span class="badge text-white" style="background:#7c3aed;">${n.idNiveau || '—'}</span></td>
            <td>${filiere
                ? `<span class="badge bg-info text-dark">${filiere.nomFiliere}</span>`
                : '<span class="text-muted fst-italic">—</span>'}
            </td>
            <td class="small">
                ${specialitesN.length > 0
                ? specialitesN.map(s => `<span class="badge me-1 mb-1" style="background:#f59e0b;color:white;">${s.nom || s.nomSpecialite}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune spécialité</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-warning border-0 me-1" title="Attribuer des spécialités" onclick="openAssignSpecialites(${n.id})">
                    <i class="bi bi-plus-circle"></i>
                </button>
                <button class="btn btn-sm btn-outline-primary border-0 me-1" title="Modifier" onclick="openEditNiveau(${n.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger border-0" title="Supprimer" onclick="deleteNiveau(${n.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

function openCreateNiveau() {
    if (filieres.length === 0) {
        showToast('Veuillez d\'abord créer une filière avant de créer un niveau.', 'warning');
        return;
    }
    document.getElementById('niveauModalTitle').textContent = 'Nouveau niveau';
    document.getElementById('niveauId').value = '';
    document.getElementById('niveauIdNiveau').value = '';
    document.getElementById('niveauNom').value = '';
    populateFiliereSelect('niveauFiliereId');
    new bootstrap.Modal(document.getElementById('niveauModal')).show();
}

function openEditNiveau(id) {
    const n = niveaux.find(x => x.id === id);
    if (!n) return;
    document.getElementById('niveauModalTitle').textContent = 'Modifier le niveau';
    document.getElementById('niveauId').value = n.id;
    document.getElementById('niveauIdNiveau').value = n.idNiveau || '';
    document.getElementById('niveauNom').value = n.nom || n.nomNiveau || '';
    populateFiliereSelect('niveauFiliereId', n.filiereId);
    new bootstrap.Modal(document.getElementById('niveauModal')).show();
}

async function saveNiveau() {
    const id = document.getElementById('niveauId').value;
    const nom = document.getElementById('niveauNom').value.trim();
    const idNiv = document.getElementById('niveauIdNiveau').value.trim();
    if (!nom) { showToast('Le nom du niveau est requis.', 'warning'); return; }
    const filiereId = document.getElementById('niveauFiliereId').value;
    if (!filiereId) { showToast('Veuillez sélectionner une filière.', 'warning'); return; }
    const payload = { idNiveau: idNiv, nom, filiereId: parseInt(filiereId) };
    try {
        if (id) {
            const updated = await apiFetch(`/niveaux/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            if (!updated.filiereId) updated.filiereId = payload.filiereId;
            niveaux = niveaux.map(n => n.id == id ? updated : n);
            showToast('Niveau modifié.', 'success');
        } else {
            const created = await apiFetch('/niveaux', { method: 'POST', body: JSON.stringify(payload) });
            if (!created.filiereId) created.filiereId = payload.filiereId;
            niveaux.push(created);
            showToast('Niveau créé.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('niveauModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

async function deleteNiveau(id) {
    const n = niveaux.find(x => x.id === id);
    if (!await showConfirm(`Supprimer "${n?.nom || n?.nomNiveau}" ?`, 'Les spécialités associées seront affectées.')) return;
    try {
        await apiFetch(`/niveaux/${id}`, { method: 'DELETE' });
        niveaux = niveaux.filter(x => x.id !== id);
        updateCounts(); renderCurrentTab();
        showToast('Niveau supprimé.', 'success');
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Remplit la liste déroulante des niveaux
function populateNiveauSelect(selectId, selectedId = null) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = `<option value="">-- Sélectionnez un niveau --</option>`;
    niveaux.forEach(n => {
        const opt = document.createElement('option');
        opt.value = n.id;
        opt.textContent = n.nom || n.nomNiveau;
        if (selectedId != null && n.id == selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// ─── SPÉCIALITÉS ────────────────────────────────────────────────────────────
// Hierarchy: Spécialité belongs to Niveau (via niveauId)

function renderSpecialites() {
    const tbody = document.getElementById('specialitesTableBody');
    if (!tbody) return;
    if (specialites.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucune spécialité enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = specialites.map(s => {
        const niveau = niveaux.find(n => n.id == s.niveauId);
        const classesS = classes.filter(cl => (cl.specialiteId || cl.speciliteId) == s.id);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${s.nom || s.nomSpecialite || '—'}</td>
            <td><span class="badge" style="background:#f59e0b;color:white;">${s.idSpecialite || '—'}</span></td>
            <td>${niveau
                ? `<span class="badge" style="background:#7c3aed;color:white;">${niveau.nom || niveau.nomNiveau}</span>`
                : '<span class="text-muted fst-italic">—</span>'}
            </td>
            <td class="small">
                ${classesS.length > 0
                ? classesS.map(cl => `<span class="badge bg-dark me-1">${cl.nomClasse}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune classe</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-success border-0 me-1" title="Attribuer des classes" onclick="openAssignClasses(${s.id})">
                    <i class="bi bi-plus-circle"></i>
                </button>
                <button class="btn btn-sm btn-outline-primary border-0 me-1" title="Modifier" onclick="openEditSpecialite(${s.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger border-0" title="Supprimer" onclick="deleteSpecialite(${s.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

function openCreateSpecialite() {
    if (niveaux.length === 0) {
        showToast('Veuillez d\'abord créer un niveau avant de créer une spécialité.', 'warning');
        return;
    }
    document.getElementById('specialiteModalTitle').textContent = 'Nouvelle spécialité';
    document.getElementById('specialiteId').value = '';
    document.getElementById('specialiteIdSpecialite').value = '';
    document.getElementById('specialiteNom').value = '';
    populateNiveauSelect('specialiteNiveauId');
    new bootstrap.Modal(document.getElementById('specialiteModal')).show();
}

function openEditSpecialite(id) {
    const s = specialites.find(x => x.id === id);
    if (!s) return;
    document.getElementById('specialiteModalTitle').textContent = 'Modifier la spécialité';
    document.getElementById('specialiteId').value = s.id;
    document.getElementById('specialiteIdSpecialite').value = s.idSpecialite || '';
    document.getElementById('specialiteNom').value = s.nom || s.nomSpecialite || '';
    populateNiveauSelect('specialiteNiveauId', s.niveauId);
    new bootstrap.Modal(document.getElementById('specialiteModal')).show();
}

async function saveSpecialite() {
    const id = document.getElementById('specialiteId').value;
    const niveauId = document.getElementById('specialiteNiveauId').value;
    const payload = {
        idSpecialite: document.getElementById('specialiteIdSpecialite').value.trim(),
        nom: document.getElementById('specialiteNom').value.trim(),
        niveauId: niveauId ? parseInt(niveauId) : null,
    };
    if (!payload.nom) { showToast('Le nom de la spécialité est requis.', 'warning'); return; }
    if (!payload.niveauId) { showToast('Veuillez sélectionner un niveau.', 'warning'); return; }
    try {
        if (id) {
            const updated = await apiFetch(`/specialites/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            specialites = specialites.map(s => s.id == id ? { ...updated, niveauId: payload.niveauId } : s);
            showToast('Spécialité modifiée.', 'success');
        } else {
            const created = await apiFetch('/specialites', { method: 'POST', body: JSON.stringify(payload) });
            if (!created.niveauId) created.niveauId = payload.niveauId;
            specialites.push(created);
            showToast('Spécialité créée.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('specialiteModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

async function deleteSpecialite(id) {
    const s = specialites.find(x => x.id === id);
    if (!await showConfirm(`Supprimer "${s?.nom || s?.nomSpecialite}" ?`, 'Cette action est irréversible.')) return;
    try {
        await apiFetch(`/specialites/${id}`, { method: 'DELETE' });
        specialites = specialites.filter(x => x.id !== id);
        updateCounts(); renderCurrentTab();
        showToast('Spécialité supprimée.', 'success');
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Remplit la liste déroulante des spécialités
function populateSpecialiteSelect(selectId, selectedId = null) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = `<option value="">-- Sélectionnez une spécialité --</option>`;
    specialites.forEach(s => {
        const niveau = niveaux.find(n => n.id == s.niveauId);
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.textContent = niveau ? `${s.nom || s.nomSpecialite} — ${niveau.nom || niveau.nomNiveau}` : (s.nom || s.nomSpecialite);
        if (selectedId != null && s.id == selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Assign spécialités to a Niveau
function openAssignSpecialites(niveauId) {
    const niveau = niveaux.find(n => n.id == niveauId);
    document.getElementById('assignSpecialiteTitle').textContent =
        niveau ? `Attribuer des spécialités — ${niveau.nom || niveau.nomNiveau}` : 'Attribuer des spécialités';
    renderAssignSpecialiteList(niveauId);
    new bootstrap.Modal(document.getElementById('assignSpecialiteModal')).show();
}

function renderAssignSpecialiteList(niveauId) {
    const container = document.getElementById('assignSpecialiteList');
    if (specialites.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucune spécialité disponible.</p>`;
        return;
    }
    const sorted = [...specialites].sort((a, b) => (b.niveauId == niveauId) - (a.niveauId == niveauId));
    container.innerHTML = sorted.map(s => {
        const isAssigned = s.niveauId == niveauId;
        const badge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#f59e0b;font-size:0.8rem;flex-shrink:0;">
                    ${(s.nom || s.nomSpecialite || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${s.nom || s.nomSpecialite || '—'} <span class="text-muted fw-normal">(${s.idSpecialite || '?'})</span></div>
                    <div class="mt-1">${badge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${isAssigned ? 'btn-outline-danger' : 'btn-outline-success'} rounded-3 px-3"
                    onclick="doAssignSpecialite(${s.id},${isAssigned ? null : niveauId},${niveauId})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

async function doAssignSpecialite(specialiteId, newNiveauId, currentNiveauId) {
    const s = specialites.find(x => x.id == specialiteId);
    if (!s) return;
    const payload = { idSpecialite: s.idSpecialite, nom: s.nom || s.nomSpecialite, niveauId: newNiveauId };
    try {
        const updated = await apiFetch(`/specialites/${specialiteId}`, { method: 'PUT', body: JSON.stringify(payload) });
        updated.niveauId = newNiveauId;
        specialites = specialites.map(x => x.id == specialiteId ? updated : x);
        showToast(`Spécialité "${s.nom || s.nomSpecialite}" ${newNiveauId ? 'attribuée' : 'retirée'}.`, 'success');
        renderAssignSpecialiteList(currentNiveauId);
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// ─── CLASSES ────────────────────────────────────────────────────────────────

// ClasseDTO retourne 'speciliteId' (faute de frappe backend) — on accepte les deux
function getSpecialiteIdFromClasse(cl) {
    return cl.specialiteId || cl.speciliteId || null;
}

function renderClasses() {
    const tbody = document.getElementById('classesTableBody');
    if (!tbody) return;
    if (classes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucune classe enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = classes.map(cl => {
        const spId = getSpecialiteIdFromClasse(cl);
        const specialite = specialites.find(s => s.id == spId);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${cl.nomClasse || '—'}</td>
            <td><span class="badge bg-dark">${cl.idClasse || '—'}</span></td>
            <td>${specialite
                ? `<span class="badge" style="background:#f59e0b;color:white;">${specialite.nom || specialite.nomSpecialite}</span>`
                : '<span class="text-muted fst-italic">—</span>'}
            </td>
            <td class="text-muted small">${cl.anneeAcademique || '—'}</td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-primary border-0 me-1" title="Modifier" onclick="openEditClasse(${cl.id})">
                    <i class="bi bi-pencil"></i>
                </button>
                <button class="btn btn-sm btn-outline-danger border-0 me-1" title="Supprimer" onclick="deleteClasse(${cl.id})">
                    <i class="bi bi-trash"></i>
                </button>
                <button class="btn btn-sm btn-outline-warning border-0" title="Migrer des étudiants" onclick="openMigrationModal(${cl.id})">
                    <i class="bi bi-arrow-left-right"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

// Auto-remplit l'année scolaire : Spécialité → Niveau → Filière → Cycle
function autoFillClasseAnnee() {
    const spId = document.getElementById('classeSpecialiteId').value;
    if (!spId) return;
    const sp = specialites.find(s => s.id == spId);
    if (!sp || !sp.niveauId) return;
    const niv = niveaux.find(n => n.id == sp.niveauId);
    if (!niv || !niv.filiereId) return;
    const fil = filieres.find(f => f.id == niv.filiereId);
    if (!fil || !fil.cycleId) return;
    const cycle = cycles.find(c => c.id == fil.cycleId);
    if (!cycle) return;
    const annee = getAnneeAcademiqueFromCycle(cycle);
    const sel = document.getElementById('classeAnneeScolaire');
    if (sel && annee) {
        populateAnneeScolaireClasseSelect(annee);
        sel.value = annee;
    }
}

function openCreateClasse() {
    if (specialites.length === 0) {
        showToast('Veuillez d\'abord créer une spécialité avant de créer une classe.', 'warning');
        return;
    }
    document.getElementById('classeModalTitle').textContent = 'Nouvelle classe';
    document.getElementById('classeId').value = '';
    document.getElementById('classeIdClasse').value = '';
    document.getElementById('classeNom').value = '';
    const capEl = document.getElementById('classeCapacite');
    if (capEl) capEl.value = '';
    populateSpecialiteSelect('classeSpecialiteId');
    populateAnneeScolaireClasseSelect();
    new bootstrap.Modal(document.getElementById('classeModal')).show();
}

function openEditClasse(id) {
    const cl = classes.find(x => x.id === id);
    if (!cl) return;
    const spId = getSpecialiteIdFromClasse(cl);
    document.getElementById('classeModalTitle').textContent = 'Modifier la classe';
    document.getElementById('classeId').value = cl.id;
    document.getElementById('classeIdClasse').value = cl.idClasse || '';
    document.getElementById('classeNom').value = cl.nomClasse || '';
    const capEl = document.getElementById('classeCapacite');
    if (capEl) capEl.value = cl.capacite || '';
    populateSpecialiteSelect('classeSpecialiteId', spId);
    populateAnneeScolaireClasseSelect(cl.anneeAcademique || '');
    new bootstrap.Modal(document.getElementById('classeModal')).show();
}

async function saveClasse() {
    const id = document.getElementById('classeId').value;
    const specialiteId = document.getElementById('classeSpecialiteId').value;
    const anneeSel = document.getElementById('classeAnneeScolaire');
    const annee = anneeSel ? anneeSel.value : '';
    const cap = document.getElementById('classeCapacite');
    const payload = {
        idClasse: document.getElementById('classeIdClasse').value.trim(),
        nomClasse: document.getElementById('classeNom').value.trim(),
        anneeAcademique: annee,
        capacite: cap && cap.value ? parseInt(cap.value) : null,
        specialiteId: specialiteId ? parseInt(specialiteId) : null,
    };
    if (!payload.nomClasse) { showToast('Le nom de la classe est requis.', 'warning'); return; }
    if (!payload.specialiteId) { showToast('Veuillez sélectionner une spécialité.', 'warning'); return; }
    try {
        if (id) {
            const updated = await apiFetch(`/classes/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            const spIdFinal = updated.specialiteId || updated.speciliteId || payload.specialiteId;
            updated.specialiteId = spIdFinal;
            classes = classes.map(cl => cl.id == id ? updated : cl);
            showToast('Classe modifiée.', 'success');
        } else {
            const created = await apiFetch('/classes', { method: 'POST', body: JSON.stringify(payload) });
            const spIdFinal = created.specialiteId || created.speciliteId || payload.specialiteId;
            created.specialiteId = spIdFinal;
            classes.push(created);
            showToast('Classe créée.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('classeModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

async function deleteClasse(id) {
    const cl = classes.find(x => x.id === id);
    if (!await showConfirm(`Supprimer "${cl?.nomClasse}" ?`, 'Cette action est irréversible.')) return;
    try {
        await apiFetch(`/classes/${id}`, { method: 'DELETE' });
        classes = classes.filter(x => x.id !== id);
        updateCounts(); renderCurrentTab();
        showToast('Classe supprimée.', 'success');
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

function openAssignClasses(specialiteId) {
    const specialite = specialites.find(s => s.id == specialiteId);
    document.getElementById('assignClasseTitle').textContent =
        specialite ? `Attribuer des classes — ${specialite.nom || specialite.nomSpecialite}` : 'Attribuer des classes';
    renderAssignClasseList(specialiteId);
    new bootstrap.Modal(document.getElementById('assignClasseModal')).show();
}

function renderAssignClasseList(specialiteId) {
    const container = document.getElementById('assignClasseList');
    if (classes.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucune classe disponible.</p>`;
        return;
    }
    const sorted = [...classes].sort((a, b) => (b.specialiteId == specialiteId) - (a.specialiteId == specialiteId));
    container.innerHTML = sorted.map(cl => {
        const isAssigned = cl.specialiteId == specialiteId;
        const badge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#0ea5e9;font-size:0.8rem;flex-shrink:0;">
                    ${(cl.nomClasse || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${cl.nomClasse || '—'} <span class="badge bg-dark ms-1" style="font-size:0.65rem;">${cl.idClasse || ''}</span></div>
                    <div class="mt-1">${badge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${isAssigned ? 'btn-outline-danger' : 'btn-outline-success'} rounded-3 px-3"
                    onclick="doAssignClasse(${cl.id},${isAssigned ? null : specialiteId},${specialiteId})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

async function doAssignClasse(classeId, newSpecialiteId, currentSpecialiteId) {
    const cl = classes.find(x => x.id == classeId);
    if (!cl) return;
    const payload = { idClasse: cl.idClasse, nomClasse: cl.nomClasse, anneeAcademique: cl.anneeAcademique || '', specialiteId: newSpecialiteId };
    try {
        const updated = await apiFetch(`/classes/${classeId}`, { method: 'PUT', body: JSON.stringify(payload) });
        updated.specialiteId = newSpecialiteId;
        classes = classes.map(x => x.id == classeId ? updated : x);
        showToast(`Classe "${cl.nomClasse}" ${newSpecialiteId ? 'attribuée' : 'retirée'}.`, 'success');
        renderAssignClasseList(currentSpecialiteId);
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// ─── INIT ────────────────────────────────────────────────────────────────────

window.switchTab = function (name) {
    activeTab = name;
    renderCurrentTab();
};

window.autoFillClasseAnnee = autoFillClasseAnnee;

window.saveCycle = saveCycle;
window.openCreateCycle = openCreateCycle;
window.openEditCycle = openEditCycle;
window.deleteCycle = deleteCycle;

window.saveFiliere = saveFiliere;
window.openCreateFiliere = openCreateFiliere;
window.openEditFiliere = openEditFiliere;
window.deleteFiliere = deleteFiliere;
window.openAssignNiveaux = openAssignNiveaux;
window.doAssignNiveau = doAssignNiveau;

window.saveNiveau = saveNiveau;
window.openCreateNiveau = openCreateNiveau;
window.openEditNiveau = openEditNiveau;
window.deleteNiveau = deleteNiveau;
window.openAssignSpecialites = openAssignSpecialites;
window.doAssignSpecialite = doAssignSpecialite;

window.saveSpecialite = saveSpecialite;
window.openCreateSpecialite = openCreateSpecialite;
window.openEditSpecialite = openEditSpecialite;
window.deleteSpecialite = deleteSpecialite;
window.openAssignClasses = openAssignClasses;
window.doAssignClasse = doAssignClasse;

window.saveClasse = saveClasse;
window.openCreateClasse = openCreateClasse;
window.openEditClasse = openEditClasse;
window.deleteClasse = deleteClasse;

// Années Scolaires
window.openCreateAnnee = openCreateAnnee;
window.openEditAnnee = openEditAnnee;
window.saveAnnee = saveAnnee;
window.deleteAnnee = deleteAnnee;
window.activerAnnee = activerAnnee;

// Semestres
window.openCreateSemestre = openCreateSemestre;
window.openEditSemestre = openEditSemestre;
window.saveSemestre = saveSemestre;
window.deleteSemestre = deleteSemestre;
window.activerSemestre = activerSemestre;

// Migration
window.openMigrationModal = openMigrationModal;
window.executeMigration = executeMigration;
window.toggleAllMigration = toggleAllMigration;

document.addEventListener('DOMContentLoaded', () => {
    loadAll();
});

// ─── ANNÉES SCOLAIRES ──────────────────────────────────────────────────────

function renderAnnees() {
    const tbody = document.getElementById('anneesTableBody');
    if (!anneesAcademiques.length) {
        tbody.innerHTML = '<tr><td colspan="7" class="text-center text-muted py-4">Aucune année scolaire enregistrée.</td></tr>';
        return;
    }
    tbody.innerHTML = anneesAcademiques.map(a => {
        const statutBadge = a.actif
            ? '<span class="badge bg-success">Actif</span>'
            : '<span class="badge bg-secondary">Inactif</span>';
        const activerBtn = a.actif
            ? '<button class="btn btn-sm btn-outline-secondary" disabled title="Déjà actif"><i class="bi bi-check-circle"></i></button>'
            : `<button class="btn btn-sm btn-outline-success" title="Activer" onclick="activerAnnee(${a.id})"><i class="bi bi-lightning"></i></button>`;
        const semCount = a.nombreSemestres || 0;
        return `<tr>
            <td class="ps-4 fw-semibold">${a.nomAnneeAcademique || ''}</td>
            <td><code>${a.idAnneeAcademique || ''}</code></td>
            <td>${a.anneeDebut || '-'}</td>
            <td>${a.anneeFin || '-'}</td>
            <td><span class="badge bg-light text-dark">${semCount} semestre(s)</span></td>
            <td>${statutBadge}</td>
            <td class="text-center pe-4">
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" onclick="openEditAnnee(${a.id})"><i class="bi bi-pencil"></i></button>
                    <button class="btn btn-outline-danger" onclick="deleteAnnee(${a.id})"><i class="bi bi-trash"></i></button>
                    ${activerBtn}
                </div>
            </td>
        </tr>`;
    }).join('');
}

function openCreateAnnee() {
    document.getElementById('anneeModalTitle').textContent = 'Nouvelle année scolaire';
    document.getElementById('anneeId').value = '';
    document.getElementById('anneeNom').value = '';
    document.getElementById('anneeCode').value = '';
    document.getElementById('anneeDebut').value = '';
    document.getElementById('anneeFin').value = '';
    new bootstrap.Modal(document.getElementById('anneeModal')).show();
}

function openEditAnnee(id) {
    const a = anneesAcademiques.find(x => x.id === id);
    if (!a) return;
    document.getElementById('anneeModalTitle').textContent = 'Modifier l\'année scolaire';
    document.getElementById('anneeId').value = a.id;
    document.getElementById('anneeNom').value = a.nomAnneeAcademique || '';
    document.getElementById('anneeCode').value = a.idAnneeAcademique || '';
    document.getElementById('anneeDebut').value = a.anneeDebut || '';
    document.getElementById('anneeFin').value = a.anneeFin || '';
    new bootstrap.Modal(document.getElementById('anneeModal')).show();
}

async function saveAnnee() {
    const id = document.getElementById('anneeId').value;
    const dto = {
        nomAnneeAcademique: document.getElementById('anneeNom').value.trim(),
        idAnneeAcademique: document.getElementById('anneeCode').value.trim(),
        anneeDebut: document.getElementById('anneeDebut').value || null,
        anneeFin: document.getElementById('anneeFin').value || null,
        actif: false
    };
    if (!dto.nomAnneeAcademique || !dto.idAnneeAcademique) {
        showToast('Nom et code requis.', 'warning');
        return;
    }
    try {
        if (id) {
            await apiFetch(`/annees-academiques/${id}`, { method: 'PUT', body: JSON.stringify(dto) });
            showToast('Année mise à jour.', 'success');
        } else {
            await apiFetch('/annees-academiques', { method: 'POST', body: JSON.stringify(dto) });
            showToast('Année créée.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('anneeModal'))?.hide();
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}

async function deleteAnnee(id) {
    const ok = await showConfirm('Supprimer', 'Supprimer cette année scolaire ?');
    if (!ok) return;
    try {
        await apiFetch(`/annees-academiques/${id}`, { method: 'DELETE' });
        showToast('Année supprimée.', 'success');
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}

async function activerAnnee(id) {
    const ok = await showConfirm('Activer', 'Activer cette année ? L\'année actuellement active sera désactivée.');
    if (!ok) return;
    try {
        await apiFetch(`/annees-academiques/${id}/activer`, { method: 'PATCH' });
        showToast('Année activée.', 'success');
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}

// ─── SEMESTRES ─────────────────────────────────────────────────────────────

function renderSemestres() {
    const tbody = document.getElementById('semestresTableBody');
    if (!semestres.length) {
        tbody.innerHTML = '<tr><td colspan="5" class="text-center text-muted py-4">Aucun semestre enregistré.</td></tr>';
        return;
    }
    tbody.innerHTML = semestres.map(s => {
        const statutBadge = s.statut
            ? '<span class="badge bg-success">Actif</span>'
            : '<span class="badge bg-secondary">Inactif</span>';
        const activerBtn = s.statut
            ? '<button class="btn btn-sm btn-outline-secondary" disabled title="Déjà actif"><i class="bi bi-check-circle"></i></button>'
            : `<button class="btn btn-sm btn-outline-success" title="Activer" onclick="activerSemestre(${s.id})"><i class="bi bi-lightning"></i></button>`;
        return `<tr>
            <td class="ps-4 fw-semibold">${s.nomSemestre || ''}</td>
            <td><code>${s.idSemestre || ''}</code></td>
            <td>${s.anneeNom || '-'}</td>
            <td>${statutBadge}</td>
            <td class="text-center pe-4">
                <div class="btn-group btn-group-sm">
                    <button class="btn btn-outline-primary" onclick="openEditSemestre(${s.id})"><i class="bi bi-pencil"></i></button>
                    <button class="btn btn-outline-danger" onclick="deleteSemestre(${s.id})"><i class="bi bi-trash"></i></button>
                    ${activerBtn}
                </div>
            </td>
        </tr>`;
    }).join('');
}

function populateAnneeSelect(selectId, selectedId = '') {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = '<option value="">-- Sélectionnez une année --</option>';
    anneesAcademiques.forEach(a => {
        const opt = document.createElement('option');
        opt.value = a.id;
        opt.textContent = a.nomAnneeAcademique;
        if (String(a.id) === String(selectedId)) opt.selected = true;
        sel.appendChild(opt);
    });
}

function openCreateSemestre() {
    document.getElementById('semestreModalTitle').textContent = 'Nouveau semestre';
    document.getElementById('semestreId').value = '';
    document.getElementById('semestreNom').value = '';
    document.getElementById('semestreCode').value = '';
    populateAnneeSelect('semestreAnneeId');
    new bootstrap.Modal(document.getElementById('semestreModal')).show();
}

function openEditSemestre(id) {
    const s = semestres.find(x => x.id === id);
    if (!s) return;
    document.getElementById('semestreModalTitle').textContent = 'Modifier le semestre';
    document.getElementById('semestreId').value = s.id;
    document.getElementById('semestreNom').value = s.nomSemestre || '';
    document.getElementById('semestreCode').value = s.idSemestre || '';
    populateAnneeSelect('semestreAnneeId', s.anneeAcademiqueId);
    new bootstrap.Modal(document.getElementById('semestreModal')).show();
}

async function saveSemestre() {
    const id = document.getElementById('semestreId').value;
    const anneeId = document.getElementById('semestreAnneeId').value;
    const dto = {
        nomSemestre: document.getElementById('semestreNom').value.trim(),
        idSemestre: document.getElementById('semestreCode').value.trim(),
        statut: false,
        anneeAcademiqueId: anneeId ? parseInt(anneeId) : null
    };
    if (!dto.nomSemestre || !dto.idSemestre) {
        showToast('Nom et code requis.', 'warning');
        return;
    }
    try {
        if (id) {
            await apiFetch(`/semestres/${id}`, { method: 'PUT', body: JSON.stringify(dto) });
            showToast('Semestre mis à jour.', 'success');
        } else {
            await apiFetch('/semestres', { method: 'POST', body: JSON.stringify(dto) });
            showToast('Semestre créé.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('semestreModal'))?.hide();
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}

async function deleteSemestre(id) {
    const ok = await showConfirm('Supprimer', 'Supprimer ce semestre ?');
    if (!ok) return;
    try {
        await apiFetch(`/semestres/${id}`, { method: 'DELETE' });
        showToast('Semestre supprimé.', 'success');
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}

async function activerSemestre(id) {
    const ok = await showConfirm('Activer', 'Activer ce semestre ? Le semestre actuellement actif sera désactivé.');
    if (!ok) return;
    try {
        await apiFetch(`/semestres/${id}/activer`, { method: 'PATCH' });
        showToast('Semestre activé.', 'success');
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}

// ─── MIGRATION ────────────────────────────────────────────────────────────

async function openMigrationModal(classeId) {
    const cls = classes.find(c => c.id === classeId);
    if (!cls) return;

    document.getElementById('migrationSourceClasseId').value = classeId;
    document.getElementById('migrationSourceNom').value = cls.nomClasse;

    // Charger les étudiants de cette classe
    try {
        const etudiants = await apiFetch(`/etudiants/classe/${classeId}`);
        const list = document.getElementById('migrationEtudiantsList');
        if (!etudiants || !etudiants.length) {
            list.innerHTML = '<p class="text-muted text-center py-3">Aucun étudiant dans cette classe.</p>';
        } else {
            list.innerHTML = etudiants.map(e => `
                <div class="form-check">
                    <input class="form-check-input migration-cb" type="checkbox" value="${e.id}" id="mig-${e.id}" onchange="updateMigrationCount()">
                    <label class="form-check-label" for="mig-${e.id}">
                        <strong>${e.nom} ${e.prenom}</strong> <small class="text-muted">(${e.matricule || '-'})</small>
                    </label>
                </div>
            `).join('');
        }
    } catch (e) {
        document.getElementById('migrationEtudiantsList').innerHTML = '<p class="text-danger">Erreur de chargement.</p>';
    }

    // Remplir le select destination (exclure la classe source)
    const destSel = document.getElementById('migrationDestClasse');
    destSel.innerHTML = '<option value="">-- Sélectionnez --</option>';
    classes.filter(c => c.id !== classeId).forEach(c => {
        destSel.innerHTML += `<option value="${c.id}">${c.nomClasse}</option>`;
    });

    updateMigrationCount();
    new bootstrap.Modal(document.getElementById('migrationModal')).show();
}

function toggleAllMigration(checked) {
    document.querySelectorAll('.migration-cb').forEach(cb => cb.checked = checked);
    updateMigrationCount();
}

function updateMigrationCount() {
    const count = document.querySelectorAll('.migration-cb:checked').length;
    const btn = document.getElementById('migrationSubmitBtn');
    if (btn) btn.textContent = `Migrer (${count} étudiant${count > 1 ? 's' : ''})`;
}
window.updateMigrationCount = updateMigrationCount;

async function executeMigration() {
    const selected = [...document.querySelectorAll('.migration-cb:checked')].map(cb => parseInt(cb.value));
    const destId = document.getElementById('migrationDestClasse').value;
    const motif = document.getElementById('migrationMotif').value;

    if (!selected.length) { showToast('Sélectionnez au moins un étudiant.', 'warning'); return; }
    if (!destId) { showToast('Sélectionnez une classe destination.', 'warning'); return; }

    const ok = await showConfirm('Migration', `Migrer ${selected.length} étudiant(s) ? Cette action est irréversible.`);
    if (!ok) return;

    try {
        await apiFetch('/etudiants/migrer', {
            method: 'POST',
            body: JSON.stringify({
                etudiantIds: selected,
                classeDestinationId: parseInt(destId),
                motif: motif
            })
        });
        showToast(`${selected.length} étudiant(s) migré(s) avec succès.`, 'success');
        bootstrap.Modal.getInstance(document.getElementById('migrationModal'))?.hide();
        await loadAll();
    } catch (e) { showToast(parseError(e), 'danger'); }
}
