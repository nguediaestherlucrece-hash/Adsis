

const API_BASE = 'http://localhost:8081/api';

let cycles = [];
let filieres = [];
let classes = [];

let activeTab = 'cycles';

// Affiche une notification (toast) à l'écran
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

// Affiche une boîte de dialogue de confirmation et renvoie la réponse
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
            const ok = document.getElementById('confirmOkBtn');
            const cancel = document.getElementById('confirmCancelBtn');
            ok.replaceWith(ok.cloneNode(true));
            cancel.replaceWith(cancel.cloneNode(true));
            modal.hide();
            resolve(val);
        };
        document.getElementById('confirmOkBtn').addEventListener('click', () => done(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => done(false), { once: true });
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}

// Effectue un appel réseau vers l'API et traite la réponse JSON
async function apiFetch(path, options = {}) {
    const res = await fetch(`${API_BASE}${path}`, {
        headers: { 'Content-Type': 'application/json', ...options.headers },
        ...options
    });
    if (res.status === 204) return null;
    if (!res.ok) {
        const text = await res.text();
        throw new Error(text || `Erreur HTTP ${res.status}`);
    }
    return res.json();
}

// Extrait un message d'erreur lisible à partir d'une exception
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

// Gère une liste des années scolaires (5 ans passés à 8 ans futurs)
function generateAnneesScolaires() {
    const now = new Date().getFullYear();
    const start = now - 5;
    const end = now + 8;
    const years = [];
    for (let y = start; y <= end; y++) {
        years.push(`${y}-${y + 1}`);
    }
    return years;
}

// Récupère l'année académique au format YYYY-YYYY depuis un cycle
function getAnneeAcademiqueFromCycle(cycle) {
    if (!cycle) return '';
    if (cycle.anneeDebut && cycle.anneeFin) return `${cycle.anneeDebut}-${cycle.anneeFin}`;
    if (cycle.anneeDebut) return `${cycle.anneeDebut}-${cycle.anneeDebut + 1}`;
    return '';
}

// Remplit la liste déroulante des années scolaires
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

// Charge l'ensemble des données (cycles, filières, classes) en parallèle
async function loadAll() {
    try {
        [cycles, filieres, classes] = await Promise.all([
            apiFetch('/cycles'),
            apiFetch('/filieres'),
            apiFetch('/classes')
        ]);
    } catch (e) {
        showToast('Erreur de chargement : ' + parseError(e), 'danger');
        cycles = []; filieres = []; classes = [];
    }
    updateCounts();
    renderCurrentTab();
}

// Change l'onglet actif et met à jour l'affichage
function switchTab(name) {
    activeTab = name;
    document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
    const tab = document.querySelector(`.filter-tab[data-tab="${name}"]`);
    if (tab) tab.classList.add('active');

    document.querySelectorAll('.tab-pane').forEach(p => p.classList.remove('show', 'active'));
    const pane = document.getElementById(`pane-${name}`);
    if (pane) pane.classList.add('show', 'active');

    renderCurrentTab();
}

// Rend le contenu de l'onglet sélectionné
function renderCurrentTab() {
    if (activeTab === 'cycles') renderCycles();
    if (activeTab === 'filieres') renderFilieres();
    if (activeTab === 'classes') renderClasses();
}

// Met à jour les compteurs des onglets
function updateCounts() {
    document.querySelectorAll('.filter-tab').forEach(t => {
        const name = t.dataset.tab;
        const label = { cycles: 'Cycles', filieres: 'Filières', classes: 'Classes' };
        const count = { cycles: cycles.length, filieres: filieres.length, classes: classes.length };
        t.textContent = `${label[name]} (${count[name] ?? 0})`;
    });
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

        const filieresAssociees = filieres.filter(f => f.cycleId == c.id);
        const anneeLabel = getAnneeAcademiqueFromCycle(c);
        return `
        <tr>
            <td class="ps-4 fw-semibold">${c.nomCycle || '—'}</td>
            <td><span class="badge bg-primary">${c.idCycle || '—'}</span></td>
            <td class="text-muted small">${anneeLabel || '—'}</td>
            <td class="small">
                ${filieresAssociees.length > 0
                ? filieresAssociees.map(f => `<span class="badge bg-secondary me-1 mb-1">${f.nomFiliere}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune filière</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-success border-0 me-1" title="Attribuer des fili\u00e8res" onclick="openAssignFilieres(${c.id})">
                    <i class="bi bi-plus-circle"></i>
                </button>
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

// Affiche la liste des filières dans le tableau
function renderFilieres() {
    const tbody = document.getElementById('filieresTableBody');
    if (!tbody) return;
    if (filieres.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Aucune filière enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = filieres.map(f => {

        const cycle = cycles.find(c => c.id == f.cycleId);

        const classesF = classes.filter(cl => cl.filiereId == f.id);

        return `
        <tr>
            <td class="ps-4 fw-semibold">${f.nomFiliere || '—'}</td>
            <td><span class="badge bg-info text-dark">${f.idFiliere || '—'}</span></td>
            <td>
                ${cycle
                ? `<span class="badge bg-primary">${cycle.nomCycle}</span>`
                : '<span class="text-muted fst-italic">—</span>'}
            </td>
            <td class="text-muted small" style="max-width:160px; overflow:hidden; text-overflow:ellipsis; white-space:nowrap;" title="${f.description || ''}">
                ${f.description || '<span class="fst-italic">—</span>'}
            </td>
            <td class="small">
                ${classesF.length > 0
                ? classesF.map(cl => `<span class="badge bg-secondary me-1 mb-1">${cl.nomClasse}</span>`).join('')
                : '<span class="text-muted fst-italic">Aucune classe</span>'}
            </td>
            <td class="text-center pe-4">
                <button class="btn btn-sm btn-outline-success border-0 me-1" title="Attribuer des classes" onclick="openAssignClasses(${f.id})">
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

// Affiche la liste des classes dans le tableau
function renderClasses() {
    const tbody = document.getElementById('classesTableBody');
    if (!tbody) return;
    if (classes.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-muted">Aucune classe enregistrée</td></tr>`;
        return;
    }
    tbody.innerHTML = classes.map(cl => {

        const filiere = filieres.find(f => f.id == cl.filiereId);

        const cycle = filiere ? cycles.find(c => c.id == filiere.cycleId) : null;

        return `
        <tr>
            <td class="ps-4 fw-semibold">${cl.nomClasse || '—'}</td>
            <td><span class="badge bg-dark">${cl.idClasse || '—'}</span></td>
            <td class="text-muted small">${cl.niveau ? `Niveau ${cl.niveau}` : '—'}</td>
            <td class="text-muted small">${cl.anneeAcademique || '—'}</td>
            <td class="small">
                ${filiere
                ? `<span class="badge bg-info text-dark me-1">${filiere.nomFiliere}</span>`
                : '<span class="text-muted fst-italic">—</span>'}
                ${cycle
                ? `<span class="badge bg-primary">${cycle.nomCycle}</span>`
                : ''}
            </td>
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

// Ouvre la fenêtre modal pour créer un nouveau cycle
function openCreateCycle() {
    document.getElementById('cycleModalTitle').textContent = 'Nouveau cycle';
    document.getElementById('cycleId').value = '';
    document.getElementById('cycleIdCycle').value = '';
    document.getElementById('cycleNom').value = '';
    document.getElementById('cycleAnneeCreation').value = '';

    const now = new Date().getFullYear();
    const defYear = `${now}-${now + 1}`;
    populateAnneeScolaireSelect(defYear);

    new bootstrap.Modal(document.getElementById('cycleModal')).show();
}

// Ouvre la fenêtre modal pour modifier un cycle existant
function openEditCycle(id) {
    const c = cycles.find(x => x.id === id);
    if (!c) return;
    document.getElementById('cycleModalTitle').textContent = 'Modifier le cycle';
    document.getElementById('cycleId').value = c.id;
    document.getElementById('cycleIdCycle').value = c.idCycle || '';
    document.getElementById('cycleNom').value = c.nomCycle || '';
    document.getElementById('cycleAnneeCreation').value = c.anneeDeCreation ?? '';

    const anneeSelectionnee = getAnneeAcademiqueFromCycle(c);
    populateAnneeScolaireSelect(anneeSelectionnee);

    new bootstrap.Modal(document.getElementById('cycleModal')).show();
}

// Sauvegarde la création/modification d'un cycle en DB
async function saveCycle() {
    const id = document.getElementById('cycleId').value;
    const anneeScolaire = document.getElementById('cycleAnneeScolaire').value;

    if (!anneeScolaire) { showToast('Veuillez sélectionner une année scolaire.', 'warning'); return; }

    const parts = anneeScolaire.split('-');
    const anneeDebut = parseInt(parts[0]);
    const anneeFin = parseInt(parts[1]);

    const payload = {
        idCycle: document.getElementById('cycleIdCycle').value.trim(),
        nomCycle: document.getElementById('cycleNom').value.trim(),
        anneeDeCreation: parseInt(document.getElementById('cycleAnneeCreation').value) || null,
        anneeDebut,
        anneeFin,
    };

    if (!payload.nomCycle) { showToast('Le nom du cycle est requis.', 'warning'); return; }

    const btn = document.getElementById('cycleSubmitBtn');
    btn.disabled = true;
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
        updateCounts();
        renderCurrentTab();
    } catch (e) {
        showToast('Erreur : ' + parseError(e), 'danger');
    } finally {
        btn.disabled = false;
    }
}

// Supprime un cycle après confirmation
async function deleteCycle(id) {
    const c = cycles.find(x => x.id === id);
    const confirmed = await showConfirm(
        `Supprimer le cycle "${c?.nomCycle}" ?`,
        'Cette action est irréversible. Les filières associées seront également affectées.'
    );
    if (!confirmed) return;
    try {
        await apiFetch(`/cycles/${id}`, { method: 'DELETE' });
        cycles = cycles.filter(x => x.id !== id);
        updateCounts();
        renderCurrentTab();
        showToast('Cycle supprimé.', 'success');
    } catch (e) {
        showToast('Erreur suppression : ' + parseError(e), 'danger');
    }
}

// Remplit la liste déroulante avec les cycles disponibles
function populateCycleSelect(selectId, selectedCycleId = null) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = `<option value="">-- Sélectionnez un cycle --</option>`;
    cycles.forEach(c => {
        const annee = getAnneeAcademiqueFromCycle(c);
        const opt = document.createElement('option');
        opt.value = c.id;
        opt.textContent = annee ? `${c.nomCycle} (${annee})` : c.nomCycle;
        if (selectedCycleId != null && c.id == selectedCycleId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Ouvre la fenêtre modal pour créer une filière
function openCreateFiliere() {

    document.getElementById('filiereModalTitle').textContent = 'Nouvelle filière';
    document.getElementById('filiereId').value = '';
    document.getElementById('filiereIdFiliere').value = '';
    document.getElementById('filiereNom').value = '';
    document.getElementById('filiereDescription').value = '';
    populateCycleSelect('filiereCycleId');
    document.getElementById('filiereCycleId').disabled = false;
    new bootstrap.Modal(document.getElementById('filiereModal')).show();
}

// Ouvre la fenêtre modal pour attribuer une/des filières à un cycle
function openAssignFilieres(cycleId) {
    const cycle = cycles.find(c => c.id == cycleId);
    document.getElementById('assignFiliereTitle').textContent =
        cycle ? `Attribuer des filières — ${cycle.nomCycle}` : 'Attribuer des filières';
    renderAssignFiliereList(cycleId);
    new bootstrap.Modal(document.getElementById('assignFiliereModal')).show();
}

// Affiche la liste des filières dans la fenêtre d'attribution avec leurs statuts
function renderAssignFiliereList(cycleId) {
    const container = document.getElementById('assignFiliereList');
    if (filieres.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucune filière disponible. Créez-en d'abord une via "Nouvelle filière".</p>`;
        return;
    }
    const sorted = [...filieres].sort((a, b) => (b.cycleId == cycleId) - (a.cycleId == cycleId));
    container.innerHTML = sorted.map(f => {
        const isAssigned = f.cycleId == cycleId;
        const otherCycle = !isAssigned && f.cycleId ? cycles.find(c => c.id == f.cycleId) : null;
        const isTransfer = !!otherCycle;
        const statusBadge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : isTransfer
                ? `<span class="badge rounded-pill" style="background:#fef9c3;color:#854d0e;"><i class="bi bi-arrow-left-right me-1"></i>${otherCycle.nomCycle}</span>`
                : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        const btnLabel = isAssigned
            ? `<i class="bi bi-x-lg me-1"></i>Retirer`
            : isTransfer
                ? `<i class="bi bi-arrow-left-right me-1"></i>Transférer`
                : `<i class="bi bi-plus-lg me-1"></i>Attribuer`;
        const btnClass = isAssigned ? 'btn-outline-danger' : isTransfer ? 'btn-outline-warning' : 'btn-outline-success';
        const newCycleId = isAssigned ? null : cycleId;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#6366f1;font-size:0.8rem;flex-shrink:0;">
                    ${(f.nomFiliere || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${f.nomFiliere || '—'} <span class="text-muted fw-normal">(${f.idFiliere || '?'})</span></div>
                    <div class="mt-1">${statusBadge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${btnClass} rounded-3 px-3" onclick="doAssignFiliere(${f.id},${newCycleId},${cycleId})">
                ${btnLabel}
            </button>
        </div>`;
    }).join('');
}

// Relie ou détache une filière à un cycle via l'API
async function doAssignFiliere(filiereId, newCycleId, currentCycleId) {
    const f = filieres.find(x => x.id == filiereId);
    if (!f) return;
    const payload = { idFiliere: f.idFiliere, nomFiliere: f.nomFiliere, description: f.description || '', cycleId: newCycleId };
    try {
        const updated = await apiFetch(`/filieres/${filiereId}`, { method: 'PUT', body: JSON.stringify(payload) });
        // S'assure que si on retire la filière (null), cela s'applique bien dans la liste locale
        updated.cycleId = newCycleId;
        filieres = filieres.map(x => x.id == filiereId ? updated : x);
        showToast(`Filière "${f.nomFiliere}" ${newCycleId ? 'attribuée' : 'retirée'} avec succès.`, 'success');
        renderAssignFiliereList(currentCycleId);
        updateCounts();
        renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Ouvre la fenêtre modal pour modifier une filière existante
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

// Sauvegarde la création/modification d'une filière en DB
async function saveFiliere() {
    const id = document.getElementById('filiereId').value;
    const cycleId = document.getElementById('filiereCycleId').value;
    const payload = {
        idFiliere: document.getElementById('filiereIdFiliere').value.trim(),
        nomFiliere: document.getElementById('filiereNom').value.trim(),
        description: document.getElementById('filiereDescription').value.trim(),
        cycleId: cycleId ? parseInt(cycleId) : null,
    };

    if (!payload.nomFiliere) { showToast('Le nom de la filière est requis.', 'warning'); return; }
    if (!payload.cycleId) { showToast('Veuillez sélectionner un cycle.', 'warning'); return; }

    const btn = document.getElementById('filiereSubmitBtn');
    btn.disabled = true;
    try {
        if (id) {
            const updated = await apiFetch(`/filieres/${id}`, { method: 'PUT', body: JSON.stringify(payload) });
            filieres = filieres.map(f => f.id == id ? updated : f);
            showToast('Filière modifiée avec succès.', 'success');
        } else {
            const created = await apiFetch('/filieres', { method: 'POST', body: JSON.stringify(payload) });
            filieres.push(created);
            showToast('Filière créée avec succès.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('filiereModal'))?.hide();
        updateCounts();
        renderCurrentTab();
    } catch (e) {
        showToast('Erreur : ' + parseError(e), 'danger');
    } finally {
        btn.disabled = false;
    }
}

// Supprime une filière après confirmation
async function deleteFiliere(id) {
    const f = filieres.find(x => x.id === id);
    const confirmed = await showConfirm(
        `Supprimer la filière "${f?.nomFiliere}" ?`,
        'Cette action est irréversible. Les classes associées seront également affectées.'
    );
    if (!confirmed) return;
    try {
        await apiFetch(`/filieres/${id}`, { method: 'DELETE' });
        filieres = filieres.filter(x => x.id !== id);
        updateCounts();
        renderCurrentTab();
        showToast('Filière supprimée.', 'success');
    } catch (e) {
        showToast('Erreur suppression : ' + parseError(e), 'danger');
    }
}

// Remplit la liste déroulante avec les filières disponibles dans les sélecteurs
function populateFiliereSelect(selectId, selectedFiliereId = null) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = `<option value="">-- Sélectionnez une filière --</option>`;
    filieres.forEach(f => {
        const cycle = cycles.find(c => c.id == f.cycleId);
        const opt = document.createElement('option');
        opt.value = f.id;
        opt.textContent = cycle ? `${f.nomFiliere} — ${cycle.nomCycle}` : f.nomFiliere;
        if (selectedFiliereId != null && f.id == selectedFiliereId) opt.selected = true;
        sel.appendChild(opt);
    });
}

// Préremplit l'année scolaire de la classe selon la filière/cycle sélectionnés
function onFiliereSelectChange() {
    const filiereId = document.getElementById('classeFiliereId').value;
    const anneeInput = document.getElementById('classeAnneeAcademique');
    if (!filiereId) { anneeInput.value = ''; return; }
    const filiere = filieres.find(f => f.id == filiereId);
    const cycle = filiere ? cycles.find(c => c.id == filiere.cycleId) : null;
    anneeInput.value = cycle ? getAnneeAcademiqueFromCycle(cycle) : '';
}

// Ouvre la fenêtre modal pour créer une nouvelle classe
function openCreateClasse() {

    document.getElementById('classeModalTitle').textContent = 'Nouvelle classe';
    document.getElementById('classeId').value = '';
    document.getElementById('classeIdClasse').value = '';
    document.getElementById('classeNom').value = '';
    document.getElementById('classeNiveau').value = '';
    document.getElementById('classeAnneeAcademique').value = '';
    populateFiliereSelect('classeFiliereId');
    const filiereSelect = document.getElementById('classeFiliereId');
    filiereSelect.disabled = false;
    filiereSelect.onchange = onFiliereSelectChange;
    new bootstrap.Modal(document.getElementById('classeModal')).show();
}

// Ouvre la fenêtre modal pour attribuer une/des classes à une filière
function openAssignClasses(filiereId) {
    const filiere = filieres.find(f => f.id == filiereId);
    document.getElementById('assignClasseTitle').textContent =
        filiere ? `Attribuer des classes — ${filiere.nomFiliere}` : 'Attribuer des classes';
    renderAssignClasseList(filiereId);
    new bootstrap.Modal(document.getElementById('assignClasseModal')).show();
}

// Affiche la liste des classes dans la fenêtre d'attribution
function renderAssignClasseList(filiereId) {
    const container = document.getElementById('assignClasseList');
    if (classes.length === 0) {
        container.innerHTML = `<p class="text-muted text-center py-4">Aucune classe disponible. Créez-en d'abord une via "Nouvelle classe".</p>`;
        return;
    }
    const sorted = [...classes].sort((a, b) => (b.filiereId == filiereId) - (a.filiereId == filiereId));
    container.innerHTML = sorted.map(cl => {
        const isAssigned = cl.filiereId == filiereId;
        const otherFiliere = !isAssigned && cl.filiereId ? filieres.find(f => f.id == cl.filiereId) : null;
        const isTransfer = !!otherFiliere;
        const statusBadge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : isTransfer
                ? `<span class="badge rounded-pill" style="background:#fef9c3;color:#854d0e;"><i class="bi bi-arrow-left-right me-1"></i>${otherFiliere.nomFiliere}</span>`
                : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        const btnLabel = isAssigned
            ? `<i class="bi bi-x-lg me-1"></i>Retirer`
            : isTransfer
                ? `<i class="bi bi-arrow-left-right me-1"></i>Transférer`
                : `<i class="bi bi-plus-lg me-1"></i>Attribuer`;
        const btnClass = isAssigned ? 'btn-outline-danger' : isTransfer ? 'btn-outline-warning' : 'btn-outline-success';
        const newFiliereId = isAssigned ? null : filiereId;
        const targetFil = newFiliereId ? filieres.find(f => f.id == newFiliereId) : null;
        const targetCyc = targetFil ? cycles.find(c => c.id == targetFil.cycleId) : null;
        const anneeAuto = targetCyc ? getAnneeAcademiqueFromCycle(targetCyc) : (cl.anneeAcademique || '');
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#0ea5e9;font-size:0.8rem;flex-shrink:0;">
                    ${(cl.nomClasse || '?').charAt(0).toUpperCase()}
                </div>
                <div>
                    <div class="fw-semibold small">${cl.nomClasse || '—'}
                        <span class="badge bg-dark ms-1" style="font-size:0.65rem;">${cl.idClasse || ''}</span>
                        ${cl.niveau ? ` <span class="text-muted fw-normal">· Niv.${cl.niveau}</span>` : ''}
                    </div>
                    <div class="mt-1">${statusBadge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${btnClass} rounded-3 px-3" onclick="doAssignClasse(${cl.id},${newFiliereId},${filiereId},'${anneeAuto}')">
                ${btnLabel}
            </button>
        </div>`;
    }).join('');
}

// Relie ou détache une classe à une filière via l'API
async function doAssignClasse(classeId, newFiliereId, currentFiliereId, anneeAcademique) {
    const cl = classes.find(x => x.id == classeId);
    if (!cl) return;
    const payload = { idClasse: cl.idClasse, nomClasse: cl.nomClasse, niveau: cl.niveau || '', anneeAcademique: anneeAcademique || cl.anneeAcademique || '', filiereId: newFiliereId };
    try {
        const updated = await apiFetch(`/classes/${classeId}`, { method: 'PUT', body: JSON.stringify(payload) });
        // Force également la mise à jour locale de l'ID, notamment lorsqu'on retire la classe (null)
        updated.filiereId = newFiliereId;
        classes = classes.map(x => x.id == classeId ? updated : x);
        showToast(`Classe "${cl.nomClasse}" ${newFiliereId ? 'attribuée' : 'retirée'} avec succès.`, 'success');
        renderAssignClasseList(currentFiliereId);
        updateCounts();
        renderCurrentTab();
    } catch (e) { showToast('Erreur : ' + parseError(e), 'danger'); }
}

// Ouvre la fenêtre modal pour modifier une classe existante
function openEditClasse(id) {
    const cl = classes.find(x => x.id === id);
    if (!cl) return;
    document.getElementById('classeModalTitle').textContent = 'Modifier la classe';
    document.getElementById('classeId').value = cl.id;
    document.getElementById('classeIdClasse').value = cl.idClasse || '';
    document.getElementById('classeNom').value = cl.nomClasse || '';
    document.getElementById('classeNiveau').value = cl.niveau || '';
    document.getElementById('classeAnneeAcademique').value = cl.anneeAcademique || '';
    populateFiliereSelect('classeFiliereId', cl.filiereId);
    document.getElementById('classeFiliereId').onchange = onFiliereSelectChange;
    new bootstrap.Modal(document.getElementById('classeModal')).show();
}

// Sauvegarde la création/modification d'une classe en DB
async function saveClasse() {
    const id = document.getElementById('classeId').value;
    const filiereId = document.getElementById('classeFiliereId').value;
    const payload = {
        idClasse: document.getElementById('classeIdClasse').value.trim(),
        nomClasse: document.getElementById('classeNom').value.trim(),
        niveau: document.getElementById('classeNiveau').value,
        anneeAcademique: document.getElementById('classeAnneeAcademique').value.trim(),
        filiereId: filiereId ? parseInt(filiereId) : null,
    };

    if (!payload.nomClasse) { showToast('Le nom de la classe est requis.', 'warning'); return; }
    if (!payload.filiereId) { showToast('Veuillez sélectionner une filière.', 'warning'); return; }
    if (!payload.niveau) { showToast('Veuillez sélectionner un niveau.', 'warning'); return; }

    const btn = document.getElementById('classeSubmitBtn');
    btn.disabled = true;
    try {
        if (id) {
            const updated = await apiFetch(`/classes/${id}`, { method: 'PUT', body: JSON.stringify(payload) });

            if (!updated.filiereId && filiereId) updated.filiereId = parseInt(filiereId);
            classes = classes.map(cl => cl.id == id ? updated : cl);
            showToast('Classe modifiée avec succès.', 'success');
        } else {
            const created = await apiFetch('/classes', { method: 'POST', body: JSON.stringify(payload) });

            if (!created.filiereId && filiereId) created.filiereId = parseInt(filiereId);
            classes.push(created);
            showToast('Classe créée avec succès.', 'success');
        }
        bootstrap.Modal.getInstance(document.getElementById('classeModal'))?.hide();
        updateCounts();
        renderCurrentTab();
    } catch (e) {
        showToast('Erreur : ' + parseError(e), 'danger');
    } finally {
        btn.disabled = false;
    }
}

// Supprime une classe après confirmation
async function deleteClasse(id) {
    const cl = classes.find(x => x.id === id);
    const confirmed = await showConfirm(
        `Supprimer la classe "${cl?.nomClasse}" ?`,
        'Cette action est irréversible.'
    );
    if (!confirmed) return;
    try {
        await apiFetch(`/classes/${id}`, { method: 'DELETE' });
        classes = classes.filter(x => x.id !== id);
        updateCounts();
        renderCurrentTab();
        showToast('Classe supprimée.', 'success');
    } catch (e) {
        showToast('Erreur suppression : ' + parseError(e), 'danger');
    }
}

document.addEventListener('DOMContentLoaded', () => {
    loadAll();
    switchTab('cycles');
});
