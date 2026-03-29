// script/matiere.js

let matieres = [];
let specialites = [];
let enseignants = [];
let isEditing = false;
let currentMatiereId = null;

const API_BASE_URL = 'http://localhost:8081/api';

// ==================== TOAST & CONFIRM ====================
function showToast(message, type = 'info') {
    const config = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger: { icon: 'bi-x-circle-fill', label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info: { icon: 'bi-info-circle-fill', label: 'Info', bg: '#1a6fa8', delay: 4000 },
    };
    const c = config[type] || config.info;
    const container = document.getElementById('toastContainer');
    const toastEl = document.createElement('div');
    toastEl.className = 'toast border-0 rounded-3 shadow-lg overflow-hidden';
    toastEl.setAttribute('role', 'alert');
    toastEl.setAttribute('data-bs-delay', c.delay);
    toastEl.style.minWidth = '280px';
    toastEl.style.maxWidth = '360px';
    toastEl.innerHTML = `
        <div class="toast-header border-0 text-white py-2 px-3" style="background:${c.bg}">
            <i class="bi ${c.icon} me-2"></i><strong class="me-auto">${c.label}</strong>
            <button type="button" class="btn-close btn-close-white ms-2" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body text-dark bg-white small py-2 px-3">${message}</div>`;
    container.appendChild(toastEl);
    new bootstrap.Toast(toastEl).show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

function showConfirm(title = 'Confirmer', message = 'Êtes-vous sûr ?') {
    return new Promise((resolve) => {
        const modalEl = document.getElementById('confirmModal');
        document.getElementById('confirmModalTitle').innerText = title;
        document.getElementById('confirmModalMessage').innerText = message;
        const modal = new bootstrap.Modal(modalEl);
        let resolved = false;
        const done = (result) => {
            if (resolved) return; resolved = true;
            document.getElementById('confirmOkBtn').replaceWith(document.getElementById('confirmOkBtn').cloneNode(true));
            document.getElementById('confirmCancelBtn').replaceWith(document.getElementById('confirmCancelBtn').cloneNode(true));
            modal.hide(); resolve(result);
        };
        document.getElementById('confirmOkBtn').addEventListener('click', () => done(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => done(false), { once: true });
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}

// ==================== API ====================
async function fetchSpecialites() {
    try {
        const r = await fetch(`${API_BASE_URL}/specialites`);
        if (r.ok) {
            specialites = await r.json();
            populateSpecialiteSelects();
        }
    } catch (e) { console.error("Erreur réseau (spécialités):", e); }
}

async function fetchEnseignants() {
    try {
        const r = await fetch(`${API_BASE_URL}/enseignants/actifs`);
        if (r.ok) enseignants = await r.json();
    } catch (e) { console.error("Erreur réseau (enseignants):", e); }
}



async function fetchMatieres() {
    try {
        const r = await fetch(`${API_BASE_URL}/cours-ue`);
        if (r.ok) {
            matieres = await r.json();
            renderMatieres();
        }
    } catch (e) { console.error("Erreur réseau (matières):", e); }
}

// ==================== SELECTS ====================
function populateSpecialiteSelects() {
    // Modal matière
    const sel = document.getElementById('specialiteSelect');
    sel.innerHTML = '<option value="">-- Sélectionner --</option>';
    specialites.forEach(s => {
        const o = document.createElement('option');
        o.value = s.id;
        o.textContent = s.nom || s.nomSpecialite || s.idSpecialite;
        sel.appendChild(o);
    });
    // Filtre enseignant modal
    const filter = document.getElementById('enseignantFilterSpecialite');
    if (filter) {
        filter.innerHTML = '<option value="">Toutes spécialités</option>';
        specialites.forEach(s => {
            const o = document.createElement('option');
            o.value = s.id;
            o.textContent = s.nom || s.nomSpecialite || s.idSpecialite;
            filter.appendChild(o);
        });
    }
}

function renderMatieres() {
    const container = document.getElementById('matieresContainer');
    let html = '';

    matieres.forEach(matiere => {

        const ensHtml = (matiere.enseignants && matiere.enseignants.length > 0)
            ? matiere.enseignants.map(e => `<span class="badge bg-info text-dark me-1"><i class="fas fa-chalkboard-teacher me-1"></i>${e.nom} ${e.prenom || ''}</span>`).join('')
            : '<span class="badge bg-light text-muted">Aucun enseignant</span>';

        const specName = matiere.specialiteNom || 'Non définie';

        html += `
            <div class="matiere-item d-flex align-items-center flex-wrap gap-2 p-2 mb-2 rounded-3 border" style="background:#fafbff;">
                <span class="matiere-code badge bg-primary">${matiere.codeUE}</span>
                <span class="matiere-nom fw-bold">${matiere.intitule}</span>
                <span class="badge bg-secondary">${specName}</span>
                <div class="d-flex flex-wrap gap-1 me-auto">${ensHtml}</div>
                <div class="btn-group btn-group-sm ms-2">
                    <button class="btn btn-outline-info" title="Enseignants" onclick="openEnseignantModal(${matiere.id})"><i class="fas fa-users"></i></button>
                    <button class="btn btn-outline-primary" title="Modifier" onclick="openEditModal(${matiere.id})"><i class="fas fa-edit"></i></button>
                    <button class="btn btn-outline-danger" title="Supprimer" onclick="supprimerMatiere(${matiere.id})"><i class="fas fa-trash"></i></button>
                </div>
            </div>
        `;
    });

    if (matieres.length === 0) {
        html = '<div class="p-4 text-center text-muted"><i class="fas fa-inbox fa-2x mb-2 d-block"></i>Aucune matière trouvée.</div>';
    }

    container.innerHTML = html;
}

// ==================== MODAL MATIÈRE ====================
function openCreateModal() {
    document.getElementById('matiereModalTitle').innerHTML = '<i class="bi bi-book me-2"></i>Nouvelle matière';
    document.getElementById('editMatiereId').value = '';
    document.getElementById('codeInput').value = '';
    document.getElementById('nomInput').value = '';
    document.getElementById('specialiteSelect').value = '';
    document.getElementById('saveMatiereBtn').textContent = 'Créer';
    new bootstrap.Modal(document.getElementById('matiereModal')).show();
}

function openEditModal(id) {
    const m = matieres.find(x => x.id === id);
    if (!m) return;
    document.getElementById('matiereModalTitle').innerHTML = '<i class="bi bi-pencil me-2"></i>Modifier la matière';
    document.getElementById('editMatiereId').value = m.id;
    document.getElementById('codeInput').value = m.codeUE;
    document.getElementById('nomInput').value = m.intitule;
    document.getElementById('specialiteSelect').value = m.specialiteId || '';
    document.getElementById('saveMatiereBtn').textContent = 'Enregistrer';
    new bootstrap.Modal(document.getElementById('matiereModal')).show();
}

document.getElementById('saveMatiereBtn').addEventListener('click', async () => {
    const code = document.getElementById('codeInput').value.trim();
    const nom = document.getElementById('nomInput').value.trim();
    const specialiteId = document.getElementById('specialiteSelect').value;
    const editId = document.getElementById('editMatiereId').value;

    if (!code || !nom || !specialiteId) {
        showToast("Veuillez remplir le code, l'intitulé et la spécialité.", "warning");
        return;
    }

    const dto = {
        idCours: editId ? undefined : "C-" + code + "-" + Date.now().toString().slice(-4),
        codeUE: code,
        intitule: nom,
        volumeHoraire: 0,
        specialiteId: parseInt(specialiteId),
        enseignantIds: []
    };

    // Si édition, conserver les enseignants existants
    if (editId) {
        const existing = matieres.find(m => m.id == editId);
        if (existing && existing.enseignants) {
            dto.enseignantIds = existing.enseignants.map(e => e.id);
        }
        dto.idCours = existing ? existing.idCours : dto.idCours;
    }

    try {
        const url = editId ? `${API_BASE_URL}/cours-ue/${editId}` : `${API_BASE_URL}/cours-ue`;
        const method = editId ? 'PUT' : 'POST';
        const r = await fetch(url, {
            method,
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (r.ok) {
            bootstrap.Modal.getInstance(document.getElementById('matiereModal'))?.hide();
            showToast(editId ? "Matière modifiée." : "Matière créée.", "success");
            await fetchMatieres();
        } else {
            const err = await r.text();
            showToast("Erreur : " + err, "danger");
        }
    } catch (e) { showToast("Erreur réseau.", "danger"); }
});

// ==================== MODAL ENSEIGNANTS ====================
function openEnseignantModal(matiereId) {
    document.getElementById('enseignantMatiereId').value = matiereId;
    const m = matieres.find(x => x.id === matiereId);
    document.getElementById('enseignantModalTitle').innerHTML = `<i class="bi bi-people me-2"></i>Enseignants — ${m ? m.intitule : ''}`;
    document.getElementById('enseignantSearch').value = '';
    if (document.getElementById('enseignantFilterSpecialite')) {
        document.getElementById('enseignantFilterSpecialite').value = '';
    }
    renderEnseignantList('', '');
    new bootstrap.Modal(document.getElementById('enseignantModal')).show();
}

function renderEnseignantList(searchTerm, filterSpecId) {
    const container = document.getElementById('enseignantListContainer');
    const matiereId = parseInt(document.getElementById('enseignantMatiereId').value);
    const m = matieres.find(x => x.id === matiereId);
    const assignedIds = (m && m.enseignants) ? m.enseignants.map(e => e.id) : [];

    let filtered = enseignants;
    if (searchTerm) {
        const q = searchTerm.toLowerCase();
        filtered = filtered.filter(e => (e.nom + ' ' + (e.prenom || '')).toLowerCase().includes(q));
    }
    if (filterSpecId) {
        // Note: enseignant may not have specialiteId, filter by cours enseignés if available
        // For now, show all if filter doesn't apply
    }

    if (filtered.length === 0) {
        container.innerHTML = '<p class="text-muted text-center p-3">Aucun enseignant trouvé.</p>';
        return;
    }

    let html = '<div class="list-group list-group-flush">';
    filtered.forEach(e => {
        const checked = assignedIds.includes(e.id) ? 'checked' : '';
        html += `
            <label class="list-group-item d-flex align-items-center gap-3 py-2" style="cursor:pointer;">
                <input type="checkbox" class="form-check-input ens-checkbox" value="${e.id}" ${checked}>
                <div class="d-flex align-items-center gap-2 flex-grow-1">
                    <div class="bg-primary text-white rounded-circle d-flex align-items-center justify-content-center fw-bold" style="width:32px;height:32px;font-size:0.8rem;">
                        ${(e.nom || '?')[0]}${(e.prenom || '?')[0]}
                    </div>
                    <div>
                        <div class="fw-semibold">${e.nom} ${e.prenom || ''}</div>
                        <small class="text-muted">${e.email || ''}</small>
                    </div>
                </div>
            </label>`;
    });
    html += '</div>';
    container.innerHTML = html;
}

// Recherche et filtre en temps réel
document.getElementById('enseignantSearch')?.addEventListener('input', (e) => {
    renderEnseignantList(e.target.value, document.getElementById('enseignantFilterSpecialite')?.value || '');
});
document.getElementById('enseignantFilterSpecialite')?.addEventListener('change', (e) => {
    renderEnseignantList(document.getElementById('enseignantSearch')?.value || '', e.target.value);
});

// Sauvegarder les enseignants assignés
document.getElementById('saveEnseignantsBtn').addEventListener('click', async () => {
    const matiereId = parseInt(document.getElementById('enseignantMatiereId').value);
    const m = matieres.find(x => x.id === matiereId);
    if (!m) return;

    const selectedIds = Array.from(document.querySelectorAll('.ens-checkbox:checked')).map(cb => parseInt(cb.value));

    const dto = {
        idCours: m.idCours,
        codeUE: m.codeUE,
        intitule: m.intitule,
        volumeHoraire: m.volumeHoraire || 0,
        specialiteId: m.specialiteId,
        enseignantIds: selectedIds
    };

    try {
        const r = await fetch(`${API_BASE_URL}/cours-ue/${matiereId}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        if (r.ok) {
            bootstrap.Modal.getInstance(document.getElementById('enseignantModal'))?.hide();
            showToast("Enseignants mis à jour.", "success");
            await fetchMatieres();
        } else {
            showToast("Erreur lors de la mise à jour.", "danger");
        }
    } catch (e) { showToast("Erreur réseau.", "danger"); }
});

// ==================== SUPPRESSION ====================
async function supprimerMatiere(id) {
    const confirmed = await showConfirm("Supprimer cette matière ?", "Cette action est irréversible.");
    if (!confirmed) return;
    try {
        const r = await fetch(`${API_BASE_URL}/cours-ue/${id}`, { method: 'DELETE' });
        if (r.ok || r.status === 204) {
            showToast("Matière supprimée.", "success");
            await fetchMatieres();
        } else {
            showToast("Erreur lors de la suppression.", "danger");
        }
    } catch (e) { showToast("Erreur réseau.", "danger"); }
}

// ==================== INIT ====================
window.openEditModal = openEditModal;
window.openEnseignantModal = openEnseignantModal;
window.supprimerMatiere = supprimerMatiere;

document.addEventListener('DOMContentLoaded', async () => {
    document.getElementById('openCreateModalBtn').addEventListener('click', openCreateModal);

    await fetchSpecialites();
    await fetchEnseignants();

    await fetchMatieres();
});