// ============ CONFIG ============
import { requireRole, authFetch } from './auth.js';

// Guard : réservé aux assistants pédagogiques, surveillants et admins
if (!requireRole(['ASSISTANT_PEDAGOGIQUE', 'SURVEILLANT', 'ADMIN'])) { throw new Error('Unauthorized'); }

// authFetch préfixe déjà avec localhost:8081
const API_BASE_URL = 'http://localhost:8081/api';

// ============ VARIABLES GLOBALES ============
let etudiants = [];
let classes = [];
let specialites = [];
let niveaux = [];

// ============ UTILITAIRES ============
function showToast(message, type = 'info') {
    const cfg = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger:  { icon: 'bi-x-circle-fill',     label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info:    { icon: 'bi-info-circle-fill',   label: 'Info',   bg: '#1a6fa8', delay: 4000 },
    };
    const c = cfg[type] || cfg.info;
    const container = document.getElementById('toastContainer');
    if (!container) return;
    const el = document.createElement('div');
    el.className = 'toast border-0 rounded-3 shadow-lg overflow-hidden';
    el.setAttribute('role', 'alert');
    el.setAttribute('data-bs-delay', c.delay);
    el.style.minWidth = '300px';
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

function showConfirm(title, message) {
    return new Promise((resolve) => {
        // Créer la modale de confirmation dynamiquement
        let modal = document.getElementById('confirmModalDynamic');
        if (!modal) {
            modal = document.createElement('div');
            modal.id = 'confirmModalDynamic';
            modal.className = 'modal fade';
            modal.tabIndex = -1;
            modal.innerHTML = `
                <div class="modal-dialog modal-dialog-centered modal-sm">
                    <div class="modal-content border-0 shadow-lg rounded-4">
                        <div class="modal-header border-0 pb-0">
                            <h5 class="modal-title fw-bold" id="confirmDynTitle">Confirmer</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body">
                            <p id="confirmDynMessage" class="text-muted small mb-0"></p>
                        </div>
                        <div class="modal-footer border-0 pt-0">
                            <button type="button" class="btn btn-light rounded-3 px-3" id="confirmDynCancel" data-bs-dismiss="modal">Annuler</button>
                            <button type="button" class="btn btn-danger rounded-3 px-3 fw-bold" id="confirmDynOk">Confirmer</button>
                        </div>
                    </div>
                </div>`;
            document.body.appendChild(modal);
        }
        document.getElementById('confirmDynTitle').innerText = title;
        document.getElementById('confirmDynMessage').innerText = message;
        const bsModal = new bootstrap.Modal(modal);
        let resolved = false;
        const done = (val) => {
            if (resolved) return;
            resolved = true;
            bsModal.hide();
            resolve(val);
        };
        const okBtn = document.getElementById('confirmDynOk');
        const cancelBtn = document.getElementById('confirmDynCancel');
        // Remove old listeners via cloning
        const newOk = okBtn.cloneNode(true);
        const newCancel = cancelBtn.cloneNode(true);
        okBtn.replaceWith(newOk);
        cancelBtn.replaceWith(newCancel);
        newOk.addEventListener('click', () => done(true), { once: true });
        newCancel.addEventListener('click', () => done(false), { once: true });
        modal.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        bsModal.show();
    });
}

function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/&/g, '&amp;').replace(/</g, '&lt;').replace(/>/g, '&gt;')
              .replace(/"/g, '&quot;').replace(/'/g, '&#039;');
}

function getInitials(nom, prenom) {
    return ((nom || '').charAt(0) + (prenom || '').charAt(0)).toUpperCase() || '?';
}

function getAvatarColor(id) {
    const colors = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#6f42c1', '#fd7e14', '#20c997'];
    return colors[(id || 0) % colors.length];
}

// ============ API LOADERS ============
async function loadNiveaux() {
    try {
        const res = await fetch(`${API_BASE_URL}/niveaux`);
        if (res.ok) niveaux = await res.json();
    } catch (e) { console.error('loadNiveaux:', e); }
}

async function loadSpecialites() {
    try {
        const res = await fetch(`${API_BASE_URL}/specialites`);
        if (res.ok) specialites = await res.json();
    } catch (e) { console.error('loadSpecialites:', e); }
}

async function loadClasses() {
    try {
        const res = await fetch(`${API_BASE_URL}/classes`);
        if (res.ok) classes = await res.json();
    } catch (e) { console.error('loadClasses:', e); }
}

async function loadEtudiants() {
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants`);
        if (res.ok) etudiants = await res.json();
    } catch (e) { console.error('loadEtudiants:', e); }
}

// ============ FILTRES (cascading) ============
function populateFilterSelects() {
    const filtreNiveau = document.getElementById('filtreNiveau');
    const filtreSpecialite = document.getElementById('filtreSpecialite');
    const filtreClasse = document.getElementById('filtreClasse');

    // Niveaux
    filtreNiveau.innerHTML = '<option value="">Tous</option>';
    niveaux.forEach(n => {
        filtreNiveau.innerHTML += `<option value="${n.id}">${escapeHtml(n.nom)}</option>`;
    });

    // All specialites initially
    populateSpecialiteSelect(specialites);
    // All classes initially
    populateClasseSelect(classes);
}

function populateSpecialiteSelect(specs) {
    const sel = document.getElementById('filtreSpecialite');
    sel.innerHTML = '<option value="">Toutes</option>';
    specs.forEach(s => {
        sel.innerHTML += `<option value="${s.id}">${escapeHtml(s.nom)}</option>`;
    });
}

function populateClasseSelect(cls) {
    const sel = document.getElementById('filtreClasse');
    sel.innerHTML = '<option value="">Toutes</option>';
    cls.forEach(c => {
        sel.innerHTML += `<option value="${c.id}">${escapeHtml(c.nomClasse)}</option>`;
    });
}

function onNiveauChange() {
    const niveauId = document.getElementById('filtreNiveau').value;
    const filtered = niveauId
        ? specialites.filter(s => s.niveauId == niveauId)
        : specialites;
    populateSpecialiteSelect(filtered);
    onSpecialiteChange();
}

function onSpecialiteChange() {
    const specId = document.getElementById('filtreSpecialite').value;
    const filtered = specId
        ? classes.filter(c => c.specialiteId == specId)
        : classes;
    populateClasseSelect(filtered);
    applyAllFilters();
}

function onClasseChange() {
    applyAllFilters();
}

function onSearchInput() {
    applyAllFilters();
}

function applyAllFilters() {
    const classeId = document.getElementById('filtreClasse').value;
    const searchTerm = (document.getElementById('searchInput').value || '').toLowerCase().trim();

    let filtered = [...etudiants];

    // Filter by classe
    if (classeId) {
        filtered = filtered.filter(e => e.classeId == classeId);
    } else {
        // If specialite selected but no specific classe, filter by all classes of that specialite
        const specId = document.getElementById('filtreSpecialite').value;
        if (specId) {
            const classeIds = classes.filter(c => c.specialiteId == specId).map(c => c.id);
            filtered = filtered.filter(e => classeIds.includes(e.classeId));
        } else {
            // If niveau selected but no specialite, filter by niveauId cascade
            const niveauId = document.getElementById('filtreNiveau').value;
            if (niveauId) {
                const specIds = specialites.filter(s => s.niveauId == niveauId).map(s => s.id);
                const classeIds = classes.filter(c => specIds.includes(c.specialiteId)).map(c => c.id);
                filtered = filtered.filter(e => classeIds.includes(e.classeId));
            }
        }
    }

    // Search filter
    if (searchTerm) {
        filtered = filtered.filter(e => {
            const fullText = `${e.nom || ''} ${e.prenom || ''} ${e.matricule || ''} ${e.email || ''}`.toLowerCase();
            return fullText.includes(searchTerm);
        });
    }

    renderEtudiantsTable(filtered);
}

// ============ RENDU ============
function renderEtudiantsTable(list) {
    const tbody = document.getElementById('studentsBody');
    const noData = document.getElementById('noDataMsg');
    const totalBadge = document.getElementById('totalBadge');

    totalBadge.textContent = `${list.length} étudiant(s)`;

    if (list.length === 0) {
        tbody.innerHTML = '';
        noData.style.display = '';
        return;
    }
    noData.style.display = 'none';

    tbody.innerHTML = list.map(e => {
        const classe = classes.find(c => c.id == e.classeId);
        const classeNom = classe ? classe.nomClasse : 'N/A';
        const initials = getInitials(e.nom, e.prenom);
        const avatarColor = getAvatarColor(e.id);
        const statut = e.statut === 'ACTIF' ? 'ACTIF' : 'INACTIF';
        const statutClass = e.statut === 'ACTIF' ? 'actif' : 'inactif';

        return `
            <tr>
                <td>
                    <div class="d-flex align-items-center gap-2">
                        <span class="student-avatar" style="background:${avatarColor}">${initials}</span>
                        <div>
                            <div class="fw-semibold">${escapeHtml(e.nom)} ${escapeHtml(e.prenom)}</div>
                            <small class="text-muted">${escapeHtml(e.email || '')}</small>
                        </div>
                    </div>
                </td>
                <td><code>${escapeHtml(e.matricule || '-')}</code></td>
                <td>${escapeHtml(e.email || '-')}</td>
                <td><span class="badge bg-light text-dark">${escapeHtml(classeNom)}</span></td>
                <td><span class="status-badge ${statutClass}">${statut}</span></td>
                <td class="text-end">
                    <div class="btn-group btn-group-sm">
                        <button class="btn btn-outline-info" title="Voir" onclick="openViewModal(${e.id})"><i class="fas fa-eye"></i></button>
                        <button class="btn btn-outline-primary" title="Modifier" onclick="openEditModal(${e.id})"><i class="fas fa-edit"></i></button>
                        <button class="btn ${e.statut === 'ACTIF' ? 'btn-outline-warning' : 'btn-outline-success'}" title="${e.statut === 'ACTIF' ? 'Bloquer' : 'Débloquer'}" onclick="toggleStatut(${e.id})">
                            <i class="fas ${e.statut === 'ACTIF' ? 'fa-lock' : 'fa-unlock'}"></i>
                        </button>
                        <button class="btn btn-outline-danger" title="Supprimer" onclick="deleteEtudiant(${e.id})"><i class="fas fa-trash"></i></button>
                        <button class="btn btn-outline-secondary" title="Historique" onclick="openHistoriqueModal(${e.id})"><i class="bi bi-clock-history"></i></button>
                    </div>
                </td>
            </tr>
        `;
    }).join('');
}

// ============ CRUD ÉTUDIANT ============
function populateModalClasseSelect(selectId) {
    const sel = document.getElementById(selectId);
    sel.innerHTML = '<option value="">-- Sélectionner --</option>';
    classes.forEach(c => {
        sel.innerHTML += `<option value="${c.id}">${escapeHtml(c.nomClasse)}</option>`;
    });
}

function openCreateModal() {
    document.getElementById('etudiantModalTitle').innerHTML = '<i class="fas fa-user-plus me-2"></i>Nouvel étudiant';
    document.getElementById('editEtudiantId').value = '';
    document.getElementById('etNomInput').value = '';
    document.getElementById('etPrenomInput').value = '';
    document.getElementById('etEmailInput').value = '';
    document.getElementById('etMatriculeInput').value = '';
    document.getElementById('etDateNaissanceInput').value = '';
    document.getElementById('etTelephoneInput').value = '';
    populateModalClasseSelect('etClasseSelect');
    document.getElementById('etClasseSelect').value = '';
    document.getElementById('saveEtudiantBtn').textContent = 'Créer';
    new bootstrap.Modal(document.getElementById('etudiantModal')).show();
}

async function openEditModal(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/${id}`);
        if (!res.ok) { showToast('Étudiant introuvable.', 'danger'); return; }
        const e = await res.json();

        document.getElementById('etudiantModalTitle').innerHTML = '<i class="fas fa-edit me-2"></i>Modifier l\'étudiant';
        document.getElementById('editEtudiantId').value = e.id;
        document.getElementById('etNomInput').value = e.nom || '';
        document.getElementById('etPrenomInput').value = e.prenom || '';
        document.getElementById('etEmailInput').value = e.email || '';
        document.getElementById('etMatriculeInput').value = e.matricule || '';
        document.getElementById('etDateNaissanceInput').value = e.dateNaissance || '';
        document.getElementById('etTelephoneInput').value = e.telephone || '';
        populateModalClasseSelect('etClasseSelect');
        document.getElementById('etClasseSelect').value = e.classeId || '';
        document.getElementById('saveEtudiantBtn').textContent = 'Enregistrer';
        new bootstrap.Modal(document.getElementById('etudiantModal')).show();
    } catch (e) { console.error(e); showToast('Erreur réseau.', 'danger'); }
}

async function openViewModal(id) {
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/${id}`);
        if (!res.ok) { showToast('Étudiant introuvable.', 'danger'); return; }
        const e = await res.json();

        const classe = classes.find(c => c.id == e.classeId);
        const classeNom = classe ? classe.nomClasse : 'N/A';
        const initials = getInitials(e.nom, e.prenom);
        const avatarColor = getAvatarColor(e.id);

        document.getElementById('viewEtudiantBody').innerHTML = `
            <div class="text-center mb-3">
                <span class="student-avatar d-inline-flex" style="background:${avatarColor};width:64px;height:64px;font-size:1.5rem;">
                    ${initials}
                </span>
                <h5 class="mt-2 mb-1">${escapeHtml(e.nom)} ${escapeHtml(e.prenom)}</h5>
                <span class="badge bg-primary">Étudiant</span>
            </div>
            <div class="detail-grid">
                <div class="detail-item"><label>Email</label><span>${escapeHtml(e.email || '-')}</span></div>
                <div class="detail-item"><label>Matricule</label><span>${escapeHtml(e.matricule || '-')}</span></div>
                <div class="detail-item"><label>Date de naissance</label><span>${e.dateNaissance || '-'}</span></div>
                <div class="detail-item"><label>Téléphone</label><span>${escapeHtml(e.telephone || '-')}</span></div>
                <div class="detail-item"><label>Classe</label><span>${escapeHtml(classeNom)}</span></div>
                <div class="detail-item"><label>Statut</label><span class="status-badge ${e.statut === 'ACTIF' ? 'actif' : 'inactif'}">${e.statut === 'ACTIF' ? 'ACTIF' : 'INACTIF'}</span></div>
            </div>
        `;
        new bootstrap.Modal(document.getElementById('viewEtudiantModal')).show();
    } catch (e) { console.error(e); showToast('Erreur réseau.', 'danger'); }
}

async function saveEtudiant() {
    const id = document.getElementById('editEtudiantId').value;
    const nom = document.getElementById('etNomInput').value.trim();
    const prenom = document.getElementById('etPrenomInput').value.trim();
    const email = document.getElementById('etEmailInput').value.trim();
    const matricule = document.getElementById('etMatriculeInput').value.trim();
    const dateNaissance = document.getElementById('etDateNaissanceInput').value;
    const telephone = document.getElementById('etTelephoneInput').value.trim();
    const classeId = document.getElementById('etClasseSelect').value;

    if (!nom || !prenom || !email || !classeId) {
        showToast('Veuillez remplir les champs obligatoires (Nom, Prénom, Email, Classe).', 'warning');
        return;
    }

    const dto = {
        nom, prenom, email, matricule: matricule || null,
        dateNaissance: dateNaissance || null,
        telephone: telephone || null,
        classeId: parseInt(classeId),
        statut: 'ACTIF'
    };

    try {
        let res;
        if (id) {
            res = await fetch(`${API_BASE_URL}/etudiants/${id}`, {
                method: 'PUT',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        } else {
            res = await fetch(`${API_BASE_URL}/etudiants`, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(dto)
            });
        }

        if (res.ok) {
            showToast(id ? 'Étudiant mis à jour.' : 'Étudiant créé.', 'success');
            bootstrap.Modal.getInstance(document.getElementById('etudiantModal'))?.hide();
            await loadEtudiants();
            applyAllFilters();
        } else {
            const errText = await res.text();
            showToast('Erreur : ' + errText, 'danger');
        }
    } catch (e) { console.error(e); showToast('Erreur réseau.', 'danger'); }
}

async function deleteEtudiant(id) {
    if (!confirm('Supprimer cet étudiant ?')) return;
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/${id}`, { method: 'DELETE' });
        if (res.ok) {
            showToast('Étudiant supprimé.', 'success');
            await loadEtudiants();
            applyAllFilters();
        } else {
            showToast('Erreur lors de la suppression.', 'danger');
        }
    } catch (e) { console.error(e); showToast('Erreur réseau.', 'danger'); }
}

// ============ IMPORT / EXPORT ============
function openImportExportModal() {
    populateModalClasseSelect('importClasseSelect');
    populateModalClasseSelect('exportClasseSelect');
    // Add "Toutes" option for export
    const exportSel = document.getElementById('exportClasseSelect');
    const firstOpt = exportSel.querySelector('option[value=""]');
    if (firstOpt) firstOpt.textContent = 'Toutes les classes';
    document.getElementById('importResult').style.display = 'none';
    new bootstrap.Modal(document.getElementById('importExportModal')).show();
}

async function downloadTemplate() {
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/template/excel`);
        if (res.ok) {
            const blob = await res.blob();
            const url = window.URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.href = url;
            a.download = 'modele_etudiants.xlsx';
            a.click();
            window.URL.revokeObjectURL(url);
            showToast('Modèle téléchargé.', 'success');
        } else {
            showToast('Erreur lors du téléchargement du modèle.', 'danger');
        }
    } catch (e) { console.error(e); showToast('Erreur réseau.', 'danger'); }
}

async function importExcel() {
    const classeId = document.getElementById('importClasseSelect').value;
    const fileInput = document.getElementById('importFileInput');
    const resultDiv = document.getElementById('importResult');

    if (!classeId) {
        showToast('Veuillez sélectionner une classe cible.', 'warning');
        return;
    }
    if (!fileInput.files || fileInput.files.length === 0) {
        showToast('Veuillez sélectionner un fichier.', 'warning');
        return;
    }

    const formData = new FormData();
    formData.append('file', fileInput.files[0]);
    formData.append('classeId', classeId);

    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/import/excel`, {
            method: 'POST',
            body: formData
        });

        resultDiv.style.display = '';
        if (res.ok) {
            const result = await res.json();
            resultDiv.innerHTML = `<div class="alert alert-success"><i class="fas fa-check-circle me-2"></i>${result.message || 'Import réussi !'}</div>`;
            await loadEtudiants();
            applyAllFilters();
        } else {
            const errText = await res.text();
            resultDiv.innerHTML = `<div class="alert alert-danger"><i class="fas fa-exclamation-circle me-2"></i>${errText}</div>`;
        }
    } catch (e) {
        console.error(e);
        resultDiv.style.display = '';
        resultDiv.innerHTML = `<div class="alert alert-danger">Erreur réseau.</div>`;
    }
}

function exportExcel() {
    const classeId = document.getElementById('exportClasseSelect').value;
    let dataToExport = [...etudiants];

    if (classeId) {
        dataToExport = dataToExport.filter(e => e.classeId == classeId);
    }

    if (dataToExport.length === 0) {
        showToast('Aucun étudiant à exporter.', 'warning');
        return;
    }

    const rows = dataToExport.map(e => {
        const classe = classes.find(c => c.id == e.classeId);
        return {
            'Nom': e.nom || '',
            'Prénom': e.prenom || '',
            'Email': e.email || '',
            'Matricule': e.matricule || '',
            'Téléphone': e.telephone || '',
            'Date Naissance': e.dateNaissance || '',
            'Classe': classe ? classe.nomClasse : '',
            'Statut': e.statut === 'ACTIF' ? 'ACTIF' : 'INACTIF'
        };
    });

    const ws = XLSX.utils.json_to_sheet(rows);
    const wb = XLSX.utils.book_new();
    XLSX.utils.book_append_sheet(wb, ws, 'Étudiants');
    XLSX.writeFile(wb, 'etudiants_export.xlsx');
    showToast('Export Excel téléchargé.', 'success');
}

// ============ TOGGLE STATUT ============
async function toggleStatut(id) {
    const etudiant = etudiants.find(e => e.id == id);
    if (!etudiant) return;
    const action = etudiant.statut === 'ACTIF' ? 'bloquer' : 'débloquer';
    if (!confirm(`Voulez-vous ${action} cet étudiant ?`)) return;
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/${id}/toggle-statut`, {
            method: 'PATCH'
        });
        if (res.ok) {
            const updated = await res.json();
            // Update local data
            const idx = etudiants.findIndex(e => e.id == id);
            if (idx !== -1) etudiants[idx] = updated;
            applyAllFilters();
            showToast(updated.statut === 'ACTIF' ? 'Étudiant débloqué.' : 'Étudiant bloqué.', 
                      updated.statut === 'ACTIF' ? 'success' : 'warning');
        } else {
            const errText = await res.text();
            showToast('Erreur : ' + errText, 'danger');
        }
    } catch (e) { console.error(e); showToast('Erreur réseau.', 'danger'); }
}

// ============ EXPOSE GLOBALS ============
window.openCreateModal = openCreateModal;
window.openEditModal = openEditModal;
window.openViewModal = openViewModal;
window.deleteEtudiant = deleteEtudiant;
window.toggleStatut = toggleStatut;
window.openHistoriqueModal = openHistoriqueModal;
window.toggleAllMigrationAssistant = toggleAllMigrationAssistant;
window.executeMigrationAssistant = executeMigrationAssistant;
window.updateMigrationCountAssistant = updateMigrationCountAssistant;

// ============ INIT ============
document.addEventListener('DOMContentLoaded', async () => {
    // Load all reference data
    await Promise.all([loadNiveaux(), loadSpecialites(), loadClasses()]);
    await loadEtudiants();

    // Populate filters
    populateFilterSelects();

    // Render initial list
    applyAllFilters();

    // Event listeners
    document.getElementById('filtreNiveau').addEventListener('change', onNiveauChange);
    document.getElementById('filtreSpecialite').addEventListener('change', onSpecialiteChange);
    document.getElementById('filtreClasse').addEventListener('change', onClasseChange);
    document.getElementById('searchInput').addEventListener('input', onSearchInput);

    document.getElementById('openCreateModalBtn').addEventListener('click', openCreateModal);
    document.getElementById('saveEtudiantBtn').addEventListener('click', saveEtudiant);

    document.getElementById('openImportExportBtn').addEventListener('click', openImportExportModal);
    document.getElementById('downloadTemplateBtn').addEventListener('click', downloadTemplate);
    document.getElementById('importExcelBtn').addEventListener('click', importExcel);
    document.getElementById('exportExcelBtn').addEventListener('click', exportExcel);

    // Migration button
    const migBtn = document.getElementById('openMigrationBtn');
    if (migBtn) migBtn.addEventListener('click', openMigrationModalAssistant);

    // Dynamic source class loading for migration
    const srcSel = document.getElementById('migrationSourceClasse');
    if (srcSel) srcSel.addEventListener('change', loadMigrationStudents);
});

// ============ MIGRATION (ASSISTANT) ============
function openMigrationModalAssistant() {
    const srcSel = document.getElementById('migrationSourceClasse');
    srcSel.innerHTML = '<option value="">-- Sélectionnez la classe source --</option>';
    classes.forEach(c => {
        srcSel.innerHTML += `<option value="${c.id}">${escapeHtml(c.nomClasse)}</option>`;
    });

    const destSel = document.getElementById('migrationDestClasse');
    destSel.innerHTML = '<option value="">-- Sélectionnez --</option>';
    classes.forEach(c => {
        destSel.innerHTML += `<option value="${c.id}">${escapeHtml(c.nomClasse)}</option>`;
    });

    document.getElementById('migrationEtudiantsList').innerHTML = '<p class="text-muted text-center py-3">Sélectionnez d\'abord une classe source.</p>';
    updateMigrationCountAssistant();
    new bootstrap.Modal(document.getElementById('migrationModal')).show();
}

async function loadMigrationStudents() {
    const classeId = document.getElementById('migrationSourceClasse').value;
    const list = document.getElementById('migrationEtudiantsList');
    if (!classeId) {
        list.innerHTML = '<p class="text-muted text-center py-3">Sélectionnez d\'abord une classe source.</p>';
        return;
    }
    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/classe/${classeId}`);
        if (!res.ok) throw new Error('Erreur');
        const students = await res.json();
        if (!students.length) {
            list.innerHTML = '<p class="text-muted text-center py-3">Aucun étudiant dans cette classe.</p>';
        } else {
            list.innerHTML = students.map(e => `
                <div class="form-check">
                    <input class="form-check-input migration-cb" type="checkbox" value="${e.id}" id="mig-${e.id}" onchange="updateMigrationCountAssistant()">
                    <label class="form-check-label" for="mig-${e.id}">
                        <strong>${escapeHtml(e.nom)} ${escapeHtml(e.prenom)}</strong> <small class="text-muted">(${escapeHtml(e.matricule || '-')})</small>
                    </label>
                </div>
            `).join('');
        }
    } catch (e) {
        list.innerHTML = '<p class="text-danger">Erreur de chargement.</p>';
    }
    updateMigrationCountAssistant();
}

function toggleAllMigrationAssistant(checked) {
    document.querySelectorAll('.migration-cb').forEach(cb => cb.checked = checked);
    updateMigrationCountAssistant();
}

function updateMigrationCountAssistant() {
    const count = document.querySelectorAll('.migration-cb:checked').length;
    const btn = document.getElementById('migrationSubmitBtn');
    if (btn) btn.textContent = `Migrer (${count} étudiant${count > 1 ? 's' : ''})`;
}

async function executeMigrationAssistant() {
    const selected = [...document.querySelectorAll('.migration-cb:checked')].map(cb => parseInt(cb.value));
    const destId = document.getElementById('migrationDestClasse').value;
    const motif = document.getElementById('migrationMotif').value;

    if (!selected.length) { showToast('Sélectionnez au moins un étudiant.', 'warning'); return; }
    if (!destId) { showToast('Sélectionnez une classe destination.', 'warning'); return; }

    const ok = await showConfirm('Migration', `Migrer ${selected.length} étudiant(s) ? Cette action est irréversible.`);
    if (!ok) return;

    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/migrer`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify({
                etudiantIds: selected,
                classeDestinationId: parseInt(destId),
                motif: motif
            })
        });
        if (!res.ok) throw new Error(await res.text());
        showToast(`${selected.length} étudiant(s) migré(s) avec succès.`, 'success');
        bootstrap.Modal.getInstance(document.getElementById('migrationModal'))?.hide();
        await loadEtudiants();
        applyAllFilters();
    } catch (e) { showToast('Erreur : ' + e.message, 'danger'); }
}

// ============ HISTORIQUE ============
async function openHistoriqueModal(etudiantId) {
    const etudiant = etudiants.find(e => e.id == etudiantId);
    const body = document.getElementById('historiqueModalBody');
    const title = document.getElementById('historiqueModalTitle');
    title.innerHTML = `<i class="bi bi-clock-history me-2"></i>Historique — ${escapeHtml(etudiant?.nom || '')} ${escapeHtml(etudiant?.prenom || '')}`;
    body.innerHTML = '<div class="text-center py-4"><div class="spinner-border text-primary" role="status"></div></div>';
    new bootstrap.Modal(document.getElementById('historiqueModal')).show();

    try {
        const res = await fetch(`${API_BASE_URL}/etudiants/${etudiantId}/historique`);
        if (!res.ok) throw new Error('Erreur');
        const historique = await res.json();

        const classeActuelle = classes.find(c => c.id == etudiant?.classeId);

        let html = `
            <div class="mb-3 p-3 bg-light rounded-3">
                <div class="fw-bold text-primary"><i class="bi bi-geo-alt me-1"></i>Classe actuelle</div>
                <div class="fs-5 fw-semibold mt-1">${escapeHtml(classeActuelle?.nomClasse || etudiant?.classeNom || 'N/A')}</div>
            </div>
        `;

        if (!historique.length) {
            html += '<p class="text-muted text-center py-3">Aucun historique de migration enregistré.</p>';
        } else {
            html += '<div class="timeline">';
            historique.forEach((h, i) => {
                const tauxColor = h.tauxPresence >= 75 ? '#22c55e' : h.tauxPresence >= 50 ? '#f59e0b' : '#ef4444';
                html += `
                    <div class="d-flex mb-3">
                        <div class="me-3 text-center" style="min-width:40px;">
                            <div class="rounded-circle bg-primary text-white d-flex align-items-center justify-content-center" style="width:32px;height:32px;font-size:0.75rem;">${i + 1}</div>
                            ${i < historique.length - 1 ? '<div style="width:2px;height:40px;background:#dee2e6;margin:4px auto;"></div>' : ''}
                        </div>
                        <div class="flex-grow-1 border rounded-3 p-3">
                            <div class="fw-bold">🗂️ ${escapeHtml(h.classeNom || 'Classe inconnue')}</div>
                            <div class="small text-muted mt-1">
                                📅 ${escapeHtml(h.anneeNom || '-')} • ${escapeHtml(h.semestreNom || '-')}
                            </div>
                            <div class="small text-muted">
                                🗓 ${h.dateEntree || '?'} → ${h.dateSortie || '?'}
                            </div>
                            <div class="mt-2">
                                <span class="badge" style="background:${tauxColor};">📊 Présence : ${h.tauxPresence?.toFixed(1) || 0}%</span>
                                <small class="text-muted ms-2">(${h.presencesCount || 0}/${h.totalSeances || 0} séances)</small>
                            </div>
                            <div class="small mt-1 fst-italic text-secondary">└─ Motif : ${escapeHtml(h.motif || 'N/A')}</div>
                        </div>
                    </div>
                `;
            });
            html += '</div>';
        }

        body.innerHTML = html;
    } catch (e) {
        body.innerHTML = '<p class="text-danger text-center py-3">Erreur lors du chargement de l\'historique.</p>';
    }
}