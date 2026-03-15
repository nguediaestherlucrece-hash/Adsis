const API_BASE = 'http://localhost:8081/api';

// État global de l'application
let cycles = [];
let niveaux = [];
let filieres = [];
let specialites = [];
let classes = [];

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
    const timer = setTimeout(() => controller.abort(), 8000); // 8s timeout
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
        [cycles, niveaux, filieres, specialites, classes] = await Promise.all([
            safeLoad('/cycles'),
            safeLoad('/niveaux'),
            safeLoad('/filieres'),
            safeLoad('/specialites'),
            safeLoad('/classes'),
        ]);
    } catch (e) {
        showToast('Erreur critique de chargement : ' + parseError(e), 'danger');
        cycles = []; niveaux = []; filieres = []; specialites = []; classes = [];
    }
    updateCounts();
    renderCurrentTab();
}

// Met à jour les compteurs affichés dans chaque onglet
function updateCounts() {
    const counts = { cycles: cycles.length, niveaux: niveaux.length, filieres: filieres.length, specialites: specialites.length, classes: classes.length };
    const labels = { cycles: 'Cycles', niveaux: 'Niveaux', filieres: 'Filières', specialites: 'Spécialités', classes: 'Classes' };
    document.querySelectorAll('.filter-tab').forEach(t => {
        const name = t.dataset.tab;
        if (labels[name] !== undefined) t.textContent = `${labels[name]} (${counts[name] ?? 0})`;
    });
}

// Rafraîchit le contenu du tableau actif
function renderCurrentTab() {
    if (activeTab === 'cycles') renderCycles();
    if (activeTab === 'niveaux') renderNiveaux();
    if (activeTab === 'filieres') renderFilieres();
    if (activeTab === 'specialites') renderSpecialites();
    if (activeTab === 'classes') renderClasses();
}


// Affiche la liste des cycles dans le tableau
function renderCycles() {
    const tbody = document.getElementById('cyclesTableBody');
    if (!tbody) return;
    if (cycles.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucun cycle enregistré</td></tr>`;
        return;
    }
    tbody.innerHTML = cycles.map(c => {
        // Cycle → Niveau → Filière : on cherche les niveaux de ce cycle puis leurs filières
        const niveauxDuCycle = niveaux.filter(n => n.cycleId == c.id);
        const niveauxIds = niveauxDuCycle.map(n => n.id);
        const filieresDuCycle = filieres.filter(f => niveauxIds.includes(f.niveauId));
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

// Ouvre la modale vide pour créer un nouveau cycle
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

// Ouvre la modale préremplie pour modifier un cycle
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

// Enregistre un cycle (création ou modification)
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
    const btn = document.getElementById('cycleSubmitBtn');
    if (btn) btn.disabled = true;
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
    finally { if (btn) btn.disabled = false; }
}

// Supprime un cycle après confirmation
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

// ─── NIVEAUX ────────────────────────────────────────────────────────────────

// Affiche la liste des niveaux : nom auto-généré = nomCycle + numéro
function renderNiveaux() {
    const tbody = document.getElementById('niveauxTableBody');
    if (!tbody) return;
    if (niveaux.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucun niveau enregistré</td></tr>`;
        return;
    }
    tbody.innerHTML = niveaux.map(n => {
        const cycle = cycles.find(c => c.id == n.cycleId);
        const filieresN = filieres.filter(f => f.niveauId == n.id);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${n.nom || n.nomNiveau || '—'}</td>
            <td><span class="badge text-white" style="background:#7c3aed;">${n.idNiveau || '—'}</span></td>
            <td>${cycle ? `<span class="badge bg-primary">${cycle.nomCycle}</span>` : '<span class="text-muted">—</span>'}</td>
            <td class="small">
                ${filieresN.length > 0
                ? filieresN.map(f => `<span class="badge bg-info text-dark me-1">${f.nomFiliere}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune filière</span>'}
            </td>
            <td class="text-center pe-4">
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

// Auto-génère le nom (NomCycle + numéro) et le code (codeCycle + numéro) dans la modale
function autoFillNiveau() {
    const cycleId = document.getElementById('niveauCycleId').value;
    const numero = document.getElementById('niveauNumero').value;
    const cycle = cycles.find(c => c.id == cycleId);
    if (cycle && numero) {
        document.getElementById('niveauNom').value = `${cycle.nomCycle} ${numero}`;
        document.getElementById('niveauIdNiveau').value = `${cycle.idCycle || cycle.nomCycle.substring(0, 1).toUpperCase()}${numero}`;
    } else {
        document.getElementById('niveauNom').value = '';
        document.getElementById('niveauIdNiveau').value = '';
    }
}

// Ouvre la modale vide pour créer un nouveau niveau
function openCreateNiveau() {
    document.getElementById('niveauModalTitle').textContent = 'Nouveau niveau';
    document.getElementById('niveauId').value = '';
    document.getElementById('niveauIdNiveau').value = '';
    document.getElementById('niveauNom').value = '';
    const numSel = document.getElementById('niveauNumero');
    if (numSel) numSel.value = '';
    populateCycleSelect('niveauCycleId');
    new bootstrap.Modal(document.getElementById('niveauModal')).show();
}

// Ouvre la modale préremplie pour modifier un niveau existant
function openEditNiveau(id) {
    const n = niveaux.find(x => x.id === id);
    if (!n) return;
    document.getElementById('niveauModalTitle').textContent = 'Modifier le niveau';
    document.getElementById('niveauId').value = n.id;
    document.getElementById('niveauIdNiveau').value = n.idNiveau || '';
    document.getElementById('niveauNom').value = n.nom || n.nomNiveau || '';
    populateCycleSelect('niveauCycleId', n.cycleId);
    // Déduit le numéro depuis le nom ou l'idNiveau
    const numSel = document.getElementById('niveauNumero');
    if (numSel) {
        const match = (n.idNiveau || '').match(/\d+$/);
        numSel.value = match ? match[0] : '';
    }
    new bootstrap.Modal(document.getElementById('niveauModal')).show();
}

// Enregistre un niveau (création ou modification) avec nom et code auto-générés
async function saveNiveau() {
    const id = document.getElementById('niveauId').value;
    const cycleId = document.getElementById('niveauCycleId').value;
    const numero = document.getElementById('niveauNumero') ? document.getElementById('niveauNumero').value : '';
    const nom = document.getElementById('niveauNom').value.trim();
    const idNiv = document.getElementById('niveauIdNiveau').value.trim();
    if (!cycleId) { showToast('Veuillez sélectionner un cycle.', 'warning'); return; }
    if (!nom) { showToast('Veuillez choisir un numéro de niveau (nom requis).', 'warning'); return; }
    const payload = { idNiveau: idNiv, nom, cycleId: parseInt(cycleId) };
    try {
        if (id) {
            const updated = await apiFetch(`/niveaux/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            niveaux = niveaux.map(n => n.id == id ? updated : n);
            showToast('Niveau modifié.', 'success');
        } else {
            const created = await apiFetch('/niveaux', { method: 'POST', body: JSON.stringify(payload) });
            niveaux.push(created);
            showToast('Niveau créé.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('niveauModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Supprime un niveau après confirmation
async function deleteNiveau(id) {
    const n = niveaux.find(x => x.id === id);
    if (!await showConfirm(`Supprimer "${n?.nom || n?.nomNiveau}" ?`, 'Les filières associées seront affectées.')) return;
    try {
        await apiFetch(`/niveaux/${id}`, { method: 'DELETE' });
        niveaux = niveaux.filter(x => x.id !== id);
        updateCounts(); renderCurrentTab();
        showToast('Niveau supprimé.', 'success');
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Remplit la liste déroulante des niveaux (objets avec id, nom, cycleId)
function populateNiveauSelect(selectId, selectedIds = []) {
    const sel = document.getElementById(selectId);
    if (!sel) return;
    sel.innerHTML = sel.multiple ? '' : `<option value="">-- Sélectionnez un niveau --</option>`;
    const idsArray = Array.isArray(selectedIds) ? selectedIds : (selectedIds ? [selectedIds] : []);
    niveaux.forEach(n => {
        const cycle = cycles.find(c => c.id == n.cycleId);
        const opt = document.createElement('option');
        opt.value = n.id;
        opt.textContent = cycle ? `${n.nom || n.nomNiveau} (${cycle.nomCycle})` : (n.nom || n.nomNiveau);
        if (idsArray.includes(n.id)) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Ouvre la modale d'attribution des niveaux à un cycle
function openAssignNiveaux(cycleId) {
    const cycle = cycles.find(c => c.id == cycleId);
    document.getElementById('assignNiveauTitle').textContent =
        cycle ? `Attribuer des niveaux — ${cycle.nomCycle}` : 'Attribuer des niveaux';
    renderAssignNiveauList(cycleId);
    new bootstrap.Modal(document.getElementById('assignNiveauModal')).show();
}

// Affiche la liste des niveaux dans la modale d'attribution (cycle)
function renderAssignNiveauList(cycleId) {
    const container = document.getElementById('assignNiveauList');
    if (niveaux.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucun niveau disponible. Créez-en d'abord un.</p>`;
        return;
    }
    const sorted = [...niveaux].sort((a, b) => (b.cycleId == cycleId) - (a.cycleId == cycleId));
    container.innerHTML = sorted.map(n => {
        const isAssigned = n.cycleId == cycleId;
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
                    onclick="doAssignNiveau(${n.id},${isAssigned ? null : cycleId},${cycleId})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

// Attribue ou retire un niveau d'un cycle via l'API
async function doAssignNiveau(niveauId, newCycleId, currentCycleId) {
    const n = niveaux.find(x => x.id == niveauId);
    if (!n) return;
    const payload = { idNiveau: n.idNiveau, nom: n.nom || n.nomNiveau, cycleId: newCycleId };
    try {
        const updated = await apiFetch(`/niveaux/${niveauId}`, { method: 'PUT', body: JSON.stringify(payload) });
        updated.cycleId = newCycleId;
        niveaux = niveaux.map(x => x.id == niveauId ? updated : x);
        showToast(`Niveau "${n.nom || n.nomNiveau}" ${newCycleId ? 'attribué' : 'retiré'}.`, 'success');
        renderAssignNiveauList(currentCycleId);
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// ─── FILIÈRES ───────────────────────────────────────────────────────────────

// Affiche la liste des filières dans le tableau
function renderFilieres() {
    const tbody = document.getElementById('filieresTableBody');
    if (!tbody) return;
    if (filieres.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Aucune filière enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = filieres.map(f => {
        const specialitesF = specialites.filter(s => s.filiereId == f.id);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${f.nomFiliere || '—'}</td>
            <td><span class="badge bg-info text-dark">${f.idFiliere || '—'}</span></td>
            <td>${(() => {
                if (!f.niveauIds || f.niveauIds.length === 0) {
                    return '<span class="text-muted fst-italic">—</span>';
                }
                return f.niveauIds.map(nid => {
                    const niv = niveaux.find(n => n.id == nid);
                    return niv ? `<span class="badge me-1 mb-1" style="background:#7c3aed;color:white;">${niv.nom || niv.nomNiveau}</span>` : '';
                }).join('');
            })()
            }
            </td>
            <td class="text-muted small" style="max-width:160px;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;" title="${f.description || ''}">
                ${f.description || '<span class="fst-italic">—</span>'}
            </td>
            <td class="small">
                ${specialitesF.length > 0
                ? specialitesF.map(s => `<span class="badge me-1 mb-1" style="background:#f59e0b;color:white;">${s.nom || s.nomSpecialite}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune spécialité</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-warning border-0 me-1" title="Attribuer des spécialités" onclick="openAssignSpecialites(${f.id})">
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

// Ouvre la modale vide pour créer une nouvelle filière
function openCreateFiliere() {
    document.getElementById('filiereModalTitle').textContent = 'Nouvelle filière';
    document.getElementById('filiereId').value = '';
    document.getElementById('filiereIdFiliere').value = '';
    document.getElementById('filiereNom').value = '';
    document.getElementById('filiereDescription').value = '';
    populateNiveauSelect('filiereNiveauId', []);
    new bootstrap.Modal(document.getElementById('filiereModal')).show();
}

// Ouvre la modale préremplie pour modifier une filière
function openEditFiliere(id) {
    const f = filieres.find(x => x.id === id);
    if (!f) return;
    document.getElementById('filiereModalTitle').textContent = 'Modifier la filière';
    document.getElementById('filiereId').value = f.id;
    document.getElementById('filiereIdFiliere').value = f.idFiliere || '';
    document.getElementById('filiereNom').value = f.nomFiliere || '';
    document.getElementById('filiereDescription').value = f.description || '';
    // filiereNiveauId attend un tableau d'ids de niveaux
    populateNiveauSelect('filiereNiveauId', f.niveauIds || []);
    new bootstrap.Modal(document.getElementById('filiereModal')).show();
}

// Enregistre une filière (création ou modification) — avec gestion de niveaux multiples
async function saveFiliere() {
    const id = document.getElementById('filiereId').value;
    const niveauSelect = document.getElementById('filiereNiveauId');
    const selectedNiveauIds = Array.from(niveauSelect.selectedOptions).map(opt => parseInt(opt.value)).filter(v => !isNaN(v));

    if (selectedNiveauIds.length === 0) { showToast('Veuillez sélectionner au moins un niveau.', 'warning'); return; }
    const nom = document.getElementById('filiereNom').value.trim();
    if (!nom) { showToast('Le nom de la filière est requis.', 'warning'); return; }
    const payload = {
        idFiliere: document.getElementById('filiereIdFiliere').value.trim(),
        nomFiliere: nom,
        description: document.getElementById('filiereDescription').value.trim(),
        niveauIds: selectedNiveauIds,   // Liste de FK vers les objets Niveau
    };
    try {
        if (id) {
            const updated = await apiFetch(`/filieres/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            updated.niveauIds = payload.niveauIds;
            filieres = filieres.map(f => f.id == id ? updated : f);
            showToast('Filière modifiée.', 'success');
        } else {
            const created = await apiFetch('/filieres', { method: 'POST', body: JSON.stringify(payload) });
            if (!created.niveauIds) created.niveauIds = payload.niveauIds;
            filieres.push(created);
            showToast('Filière créée.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('filiereModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Supprime une filière après confirmation
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
        // Find the "primary" niveau for display if multiple exist
        const primaryNiveauId = (f.niveauIds && f.niveauIds.length > 0) ? f.niveauIds[0] : null;
        const niveau = niveaux.find(n => n.id == primaryNiveauId);
        const opt = document.createElement('option');
        opt.value = f.id;
        opt.textContent = niveau ? `${f.nomFiliere} — ${niveau.nom || niveau.nomNiveau}` : f.nomFiliere;
        if (selectedId != null && f.id == selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Ouvre la modale d'attribution des filières à un niveau
function openAssignFilieres(niveauId) {
    const niveau = niveaux.find(n => n.id == niveauId);
    document.getElementById('assignFiliereTitle').textContent =
        niveau ? `Attribuer des filières — ${niveau.nom || niveau.nomNiveau}` : 'Attribuer des filières';
    renderAssignFiliereList(niveauId);
    new bootstrap.Modal(document.getElementById('assignFiliereModal')).show();
}

// Affiche la liste des filières dans la modale d'attribution (niveau)
function renderAssignFiliereList(niveauId) {
    const container = document.getElementById('assignFiliereList');
    if (filieres.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucune filière disponible.</p>`;
        return;
    }
    const sorted = [...filieres].sort((a, b) => {
        const aAssigned = a.niveauIds && a.niveauIds.includes(niveauId);
        const bAssigned = b.niveauIds && b.niveauIds.includes(niveauId);
        return bAssigned - aAssigned;
    });
    container.innerHTML = sorted.map(f => {
        const isAssigned = f.niveauIds && f.niveauIds.includes(niveauId);
        const badge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#6366f1;font-size:0.8rem;flex-shrink:0;">
                    ${(f.nomFiliere || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${f.nomFiliere || '—'} <span class="text-muted fw-normal">(${f.idFiliere || '?'})</span></div>
                    <div class="mt-1">${badge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${isAssigned ? 'btn-outline-danger' : 'btn-outline-success'} rounded-3 px-3"
                    onclick="doAssignFiliere(${f.id},${isAssigned ? null : niveauId},${niveauId})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

// Attribue ou retire une filière d'un niveau via l'API
async function doAssignFiliere(filiereId, newNiveauId, currentNiveauId) {
    const f = filieres.find(x => x.id == filiereId);
    if (!f) return;

    // Manage array composition instead of simple overwrite
    let updatedNiveauIds = Array.isArray(f.niveauIds) ? [...f.niveauIds] : [];
    if (newNiveauId) {
        if (!updatedNiveauIds.includes(newNiveauId)) updatedNiveauIds.push(newNiveauId);
    } else {
        updatedNiveauIds = updatedNiveauIds.filter(id => id !== currentNiveauId);
    }

    const payload = { idFiliere: f.idFiliere, nomFiliere: f.nomFiliere, description: f.description || '', niveauIds: updatedNiveauIds };
    try {
        const updated = await apiFetch(`/filieres/${filiereId}`, { method: 'PUT', body: JSON.stringify(payload) });
        updated.niveauIds = updatedNiveauIds;
        filieres = filieres.map(x => x.id == filiereId ? updated : x);
        showToast(`Filière "${f.nomFiliere}" ${newNiveauId ? 'attribuée' : 'retirée'}.`, 'success');
        renderAssignFiliereList(currentNiveauId);
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// ─── SPÉCIALITÉS ────────────────────────────────────────────────────────────

// Affiche la liste des spécialités dans le tableau
function renderSpecialites() {
    const tbody = document.getElementById('specialitesTableBody');
    if (!tbody) return;
    if (specialites.length === 0) {
        tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-muted">Aucune spécialité enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = specialites.map(s => {
        const filiere = filieres.find(f => f.id == s.filiereId);
        const classesS = classes.filter(cl => cl.specialiteId == s.id);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${s.nom || s.nomSpecialite || '—'}</td>
            <td><span class="badge" style="background:#f59e0b;color:white;">${s.idSpecialite || '—'}</span></td>
            <td>${filiere
                ? `<span class="badge bg-info text-dark">${filiere.nomFiliere}</span>`
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

// Ouvre la modale vide pour créer une nouvelle spécialité
function openCreateSpecialite() {
    document.getElementById('specialiteModalTitle').textContent = 'Nouvelle spécialité';
    document.getElementById('specialiteId').value = '';
    document.getElementById('specialiteIdSpecialite').value = '';
    document.getElementById('specialiteNom').value = '';
    populateFiliereSelect('specialiteFiliereId');
    new bootstrap.Modal(document.getElementById('specialiteModal')).show();
}

// Ouvre la modale préremplie pour modifier une spécialité
function openEditSpecialite(id) {
    const s = specialites.find(x => x.id === id);
    if (!s) return;
    document.getElementById('specialiteModalTitle').textContent = 'Modifier la spécialité';
    document.getElementById('specialiteId').value = s.id;
    document.getElementById('specialiteIdSpecialite').value = s.idSpecialite || '';
    document.getElementById('specialiteNom').value = s.nom || s.nomSpecialite || '';
    populateFiliereSelect('specialiteFiliereId', s.filiereId);
    new bootstrap.Modal(document.getElementById('specialiteModal')).show();
}

// Enregistre une spécialité (création ou modification) avec filiereId
async function saveSpecialite() {
    const id = document.getElementById('specialiteId').value;
    const filiereId = document.getElementById('specialiteFiliereId').value;
    const payload = {
        idSpecialite: document.getElementById('specialiteIdSpecialite').value.trim(),
        nom: document.getElementById('specialiteNom').value.trim(),
        filiereId: filiereId ? parseInt(filiereId) : null,
    };
    if (!payload.nom) { showToast('Le nom de la spécialité est requis.', 'warning'); return; }
    if (!payload.filiereId) { showToast('Veuillez sélectionner une filière.', 'warning'); return; }
    try {
        if (id) {
            const updated = await apiFetch(`/specialites/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            specialites = specialites.map(s => s.id == id ? { ...updated, filiereId: payload.filiereId } : s);
            showToast('Spécialité modifiée.', 'success');
        } else {
            const created = await apiFetch('/specialites', { method: 'POST', body: JSON.stringify(payload) });
            if (!created.filiereId) created.filiereId = payload.filiereId;
            specialites.push(created);
            showToast('Spécialité créée.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('specialiteModal'))?.hide();
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Supprime une spécialité après confirmation
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
        const filiere = filieres.find(f => f.id == s.filiereId);
        const opt = document.createElement('option');
        opt.value = s.id;
        opt.textContent = filiere ? `${s.nom || s.nomSpecialite} — ${filiere.nomFiliere}` : (s.nom || s.nomSpecialite);
        if (selectedId != null && s.id == selectedId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Ouvre la modale d'attribution des spécialités à une filière
function openAssignSpecialites(filiereId) {
    const filiere = filieres.find(f => f.id == filiereId);
    document.getElementById('assignSpecialiteTitle').textContent =
        filiere ? `Attribuer des spécialités — ${filiere.nomFiliere}` : 'Attribuer des spécialités';
    renderAssignSpecialiteList(filiereId);
    new bootstrap.Modal(document.getElementById('assignSpecialiteModal')).show();
}

// Affiche la liste des spécialités dans la modale d'attribution (filière)
function renderAssignSpecialiteList(filiereId) {
    const container = document.getElementById('assignSpecialiteList');
    if (specialites.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucune spécialité disponible.</p>`;
        return;
    }
    const sorted = [...specialites].sort((a, b) => (b.filiereId == filiereId) - (a.filiereId == filiereId));
    container.innerHTML = sorted.map(s => {
        const isAssigned = s.filiereId == filiereId;
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
                    onclick="doAssignSpecialite(${s.id},${isAssigned ? null : filiereId},${filiereId})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

// Attribue ou retire une spécialité d'une filière via l'API
async function doAssignSpecialite(specialiteId, newFiliereId, currentFiliereId) {
    const s = specialites.find(x => x.id == specialiteId);
    if (!s) return;
    const payload = { idSpecialite: s.idSpecialite, nom: s.nom || s.nomSpecialite, filiereId: newFiliereId };
    try {
        const updated = await apiFetch(`/specialites/${specialiteId}`, { method: 'PUT', body: JSON.stringify(payload) });
        updated.filiereId = newFiliereId;
        specialites = specialites.map(x => x.id == specialiteId ? updated : x);
        showToast(`Spécialité "${s.nom || s.nomSpecialite}" ${newFiliereId ? 'attribuée' : 'retirée'}.`, 'success');
        renderAssignSpecialiteList(currentFiliereId);
        updateCounts(); renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// ─── CLASSES ────────────────────────────────────────────────────────────────

// Affiche la liste des classes dans le tableau
// Note: ClasseDTO retourne 'speciliteId' (faute de frappe backend) — on accepte les deux
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
                <button class="btn btn-sm btn-outline-danger border-0" title="Supprimer" onclick="deleteClasse(${cl.id})">
                    <i class="bi bi-trash"></i>
                </button>
            </td>
        </tr>`;
    }).join('');
}

// Auto-remplit l'année scolaire de la classe depuis le cycle parent (Spécialité → Filière → Niveau → Cycle)
function autoFillClasseAnnee() {
    const spId = document.getElementById('classeSpecialiteId').value;
    if (!spId) return;
    const sp = specialites.find(s => s.id == spId);
    if (!sp) return;
    const fil = filieres.find(f => f.id == sp.filiereId);
    if (!fil) return;
    const niv = niveaux.find(n => n.id == fil.niveauId);
    if (!niv) return;
    const cycle = cycles.find(c => c.id == niv.cycleId);
    if (!cycle) return;
    const annee = getAnneeAcademiqueFromCycle(cycle);
    const sel = document.getElementById('classeAnneeScolaire');
    if (sel && annee) {
        populateAnneeScolaireClasseSelect(annee);
        sel.value = annee;
    }
}

// Ouvre la modale vide pour créer une nouvelle classe
function openCreateClasse() {
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

// Ouvre la modale préremplie pour modifier une classe
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

// Enregistre une classe (création ou modification)
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
            // Compat faute de frappe : speciliteId -> conserver notre specialiteId local
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

// Supprime une classe après confirmation
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

// Ouvre la modale d'attribution des classes à une spécialité
function openAssignClasses(specialiteId) {
    const specialite = specialites.find(s => s.id == specialiteId);
    document.getElementById('assignClasseTitle').textContent =
        specialite ? `Attribuer des classes — ${specialite.nom || specialite.nomSpecialite}` : 'Attribuer des classes';
    renderAssignClasseList(specialiteId);
    new bootstrap.Modal(document.getElementById('assignClasseModal')).show();
}

// Affiche la liste des classes dans la modale d'attribution (spécialité)
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

// Attribue ou retire une classe d'une spécialité via l'API
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

// Expose les fonctions nécessaires aux boutons HTML onclick
window.autoFillNiveau = autoFillNiveau;
// Expose switchTab pour que le HTML puisse changer d'onglet et déclencher le rendu
window.switchTab = function (name) {
    activeTab = name;
    renderCurrentTab();
};

window.saveCycle = saveCycle;
window.openCreateCycle = openCreateCycle;
window.autoFillNiveau = autoFillNiveau;
window.autoFillClasseAnnee = autoFillClasseAnnee;

window.openEditCycle = openEditCycle;
window.deleteCycle = deleteCycle;
window.openAssignNiveaux = openAssignNiveaux;
window.doAssignNiveau = doAssignNiveau;

window.saveNiveau = saveNiveau;
window.openCreateNiveau = openCreateNiveau;
window.openEditNiveau = openEditNiveau;
window.deleteNiveau = deleteNiveau;
window.openAssignFilieres = openAssignFilieres;
window.doAssignFiliere = doAssignFiliere;

window.saveFiliere = saveFiliere;
window.openCreateFiliere = openCreateFiliere;
window.openEditFiliere = openEditFiliere;
window.deleteFiliere = deleteFiliere;
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

// Lance le chargement initial au démarrage de la page
document.addEventListener('DOMContentLoaded', () => {
    loadAll();
});
