import { Api } from './api.js';

const API_BASE = 'http://localhost:8081/api';

// Affiche une notification en cas de succès ou d'erreur
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

// Extrait un message d'erreur clair depuis la réponse de l'API
function parseApiError(error) {
    const raw = error?.message || String(error);
    try {
        const match = raw.match(/\{.*\}/s);
        if (match) { const p = JSON.parse(match[0]); return p.message || p.error || raw; }
    } catch (_) { }
    return raw;
}

// Affiche une fenêtre de confirmation avant une action critique
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

// ─── État global ────────────────────────────────────────────────────────────
const userTableBody = document.getElementById('userTableBody');
const userModal = new bootstrap.Modal(document.getElementById('userModal'));
const viewModal = new bootstrap.Modal(document.getElementById('viewUserModal'));

let currentUserList = [];
let currentFilter = 'Tous';
let searchTerm = '';
let allFilieres = [];
let allMatieres = [];
let allRoles = [];
let userFiliereMap = {};
let userMatiereMap = {};
let currentStaffUser = null;

const COLORS = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796', '#5a5c69', '#6610f2', '#fd7e14'];

// Descriptions lisibles pour chaque permission
const PERM_LABELS = {
    'COURSE_VIEW': 'Voir les cours',
    'COURSE_MANAGE': 'Gérer les cours',
    'USER_VIEW': 'Voir les utilisateurs',
    'USER_MANAGE': 'Gérer les utilisateurs',
    'STUDENT_VIEW': 'Voir les étudiants',
    'ATTENDANCE_VIEW': 'Voir les présences',
    'ATTENDANCE_MANAGE': 'Gérer les présences',
    'GRADE_VIEW': 'Voir les notes',
    'ASSIGNMENT_SUBMIT': 'Soumettre les devoirs',
    'JUSTIFICATION_VALIDATE': 'Valider les justificatifs',
    'STATISTICS_VIEW': 'Voir les statistiques'
};

// Lance le chargement initial au démarrage de la page
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    loadFilieres();
    loadMatieres();
    loadRoles();
    setupEventListeners();
});

// Attache les événements de filtrage, recherche et création
function setupEventListeners() {
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
            const target = e.target.closest('.filter-tab');
            target.classList.add('active');
            const label = target.innerText.replace(/\(.*\)/, '').trim();
            filterByRole(label);
        });
    });

    const roleSelect = document.getElementById('userRole');
    if (roleSelect) {
        roleSelect.addEventListener('change', (e) => {
            toggleRoleFields(e.target.value);
            renderPermissionToggles(e.target.value, []);
        });
    }

    const saveBtn = document.querySelector('#userModal .modal-footer .btn-primary');
    if (saveBtn) saveBtn.addEventListener('click', handleSaveUser);

    document.querySelectorAll('main button.btn-primary').forEach(btn => {
        if (btn.innerText.includes('Nouvel utilisateur')) btn.addEventListener('click', openCreateModal);
    });

    const searchInput = document.querySelector('.search-box input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            searchTerm = e.target.value.toLowerCase();
            renderTable(currentFilter);
        });
    }
}

// Charge toutes les filières depuis l'API
async function loadFilieres() {
    try {
        const res = await fetch(`${API_BASE}/filieres`, { headers: { 'Content-Type': 'application/json' } });
        if (res.ok) allFilieres = await res.json();
    } catch (e) { console.warn('Filières non chargées:', e.message); }
}

// Charge toutes les matières (CoursUE) depuis l'API
// Charge tous les rôles et leurs permissions depuis l'API
async function loadRoles() {
    try {
        allRoles = await Api.getRoles();
    } catch (e) { console.warn('Rôles non chargés:', e.message); }
}

async function loadMatieres() {
    try {
        const res = await fetch(`${API_BASE}/cours`, { headers: { 'Content-Type': 'application/json' } });
        if (res.ok) allMatieres = await res.json();
    } catch (e) { console.warn('Matières non chargées:', e.message); }
}

// Charge et normalise tous les utilisateurs, puis construit les cartes de filières/matières
async function loadUsers() {
    try {
        userTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Chargement...</td></tr>';
        const users = await Api.getStaff();
        currentUserList = users.map(u => normalizeUser(u));
        await buildUserFiliereMap();
        await buildUserMatiereMap();
        // Enrichit les objets avec les collections chargées
        currentUserList.forEach(u => {
            u.filieres = userFiliereMap[u.id] || [];
            u.matieres = userMatiereMap[u.id] || [];
            if (u.role === 'Enseignant') {
                u.details = u.matieres.length > 0 ? u.matieres.map(m => m.nomCours).join(', ') : '— Aucune matière';
            } else {
                u.details = u.filieres.length > 0 ? u.filieres.map(f => f.nomFiliere).join(', ') : '— Aucune filière';
            }
        });
        renderTable(currentFilter);
        updateCounts();
    } catch (error) {
        console.error('Erreur chargement utilisateurs', error);
        userTableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Erreur: ${error.message}</td></tr>`;
    }
}

// Construit la map userId→filieres via les endpoints /filiere/{id}
async function buildUserFiliereMap() {
    userFiliereMap = {};
    if (allFilieres.length === 0) await loadFilieres();
    await Promise.all(allFilieres.map(async (f) => {
        try {
            const [aps, survs] = await Promise.all([
                fetch(`${API_BASE}/assistants-pedagogiques/filiere/${f.id}`).then(r => r.ok ? r.json() : []).catch(() => []),
                fetch(`${API_BASE}/surveillants/filiere/${f.id}`).then(r => r.ok ? r.json() : []).catch(() => []),
            ]);
            [...aps, ...survs].forEach(u => {
                if (!userFiliereMap[u.id]) userFiliereMap[u.id] = [];
                if (!userFiliereMap[u.id].some(x => x.id === f.id))
                    userFiliereMap[u.id].push({ id: f.id, nomFiliere: f.nomFiliere });
            });
        } catch (_) { }
    }));
}

// Construit la map userId→matieres pour les enseignants depuis leurs coursEnseignes
async function buildUserMatiereMap() {
    userMatiereMap = {};
    currentUserList.filter(u => u.role === 'Enseignant').forEach(u => {
        const cours = u.raw.coursEnseignes || [];
        userMatiereMap[u.id] = cours.map(c => ({ id: c.id, nomCours: c.nomCours || c.nom || '?', code: c.code || '' }));
    });
}

// Normalise le format de l'utilisateur pour l'affichage interne
function normalizeUser(u) {
    let role = 'Inconnu';
    if (u.roleNames && u.roleNames.length > 0) {
        const r = u.roleNames[0];
        if (r === 'ENSEIGNANT') role = 'Enseignant';
        else if (r === 'SURVEILLANT') role = 'Surveillant';
        else if (r === 'ASSISTANT_PEDAGOGIQUE') role = 'Assistant pédagogique';
    } else {
        if (u.specialite || u.grade) role = 'Enseignant';
        else if (u.affectation) role = 'Surveillant';
        else if (u.departement) role = 'Assistant pédagogique';
    }
    return {
        id: u.id, name: `${u.nom} ${u.prenom}`, email: u.email, role,
        status: u.statut === 'ACTIF' ? 'Actif' : 'Bloqué',
        details: '', filieres: [], matieres: [],
        raw: u, color: COLORS[u.id % COLORS.length]
    };
}

// Rafraîchit le tableau avec le filtre actif et le terme de recherche
function renderTable(roleFilter) {
    userTableBody.innerHTML = '';
    let filtered = currentUserList;

    if (roleFilter !== 'Tous') {
        const singularMap = { 'Enseignants': 'Enseignant', 'Assistants': 'Assistant pédagogique', 'Surveillants': 'Surveillant' };
        const singular = singularMap[roleFilter] || roleFilter;
        filtered = filtered.filter(u => u.role === singular);
    }
    if (searchTerm) {
        filtered = filtered.filter(u =>
            u.name.toLowerCase().includes(searchTerm) ||
            u.email.toLowerCase().includes(searchTerm) ||
            u.details.toLowerCase().includes(searchTerm)
        );
    }
    currentFilter = roleFilter;

    if (filtered.length === 0) {
        userTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Aucun utilisateur trouvé</td></tr>';
        return;
    }

    filtered.forEach(u => {
        const isBlocked = u.status === 'Bloqué';
        const row = document.createElement('tr');

        let detailsHTML;
        if (u.role === 'Enseignant') {
            detailsHTML = u.matieres.length > 0
                ? u.matieres.map(m => `<span class="badge rounded-pill" style="background:#fef9c3;color:#78350f;border:1px solid #fde68a;font-size:.72rem;">${m.nomCours}</span>`).join(' ')
                : '<span class="text-muted fst-italic small">— Aucune matière</span>';
        } else {
            detailsHTML = u.filieres.length > 0
                ? u.filieres.map(f => `<span class="badge rounded-pill" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;font-size:.72rem;">${f.nomFiliere}</span>`).join(' ')
                : '<span class="text-muted fst-italic small">— Aucune filière</span>';
        }

        row.innerHTML = `
          <td class="ps-4"><div class="d-flex align-items-center gap-2">
            <div class="user-avatar" style="background-color:${u.color}">${u.name.charAt(0)}</div>
            <div><div class="fw-bold">${u.name}</div><div class="text-muted small">${u.email}</div></div>
          </div></td>
          <td><span class="badge bg-light text-dark border rounded-pill px-3">${u.role}</span></td>
          <td><span class="status-badge text-white ${isBlocked ? 'bg-danger' : 'bg-success'}">${u.status}</span></td>
          <td class="small" style="max-width:260px;">${detailsHTML}</td>
          <td class="text-center pe-4 action-buttons"></td>`;

        const cell = row.querySelector('.action-buttons');
        cell.appendChild(createBtn('bi-eye', 'btn-outline-secondary', () => openViewModal(u)));
        cell.appendChild(createBtn('bi-pencil', 'btn-outline-primary', () => openEditModal(u.id)));
        cell.appendChild(createBtn(
            isBlocked ? 'bi-lock-fill' : 'bi-unlock-fill',
            isBlocked ? 'text-warning' : 'text-success',
            () => toggleBlock(u.id, u.raw.roleNames[0])
        ));
        cell.appendChild(createBtn('bi-trash', 'text-danger', () => deleteUser(u.id, u.raw.roleNames[0])));
        userTableBody.appendChild(row);
    });
}

// Génère un bouton d'action pour chaque ligne
function createBtn(iconClass, btnClass, onClick) {
    const btn = document.createElement('button');
    btn.className = `btn btn-sm border-0 mx-1 ${btnClass}`;
    btn.innerHTML = `<i class="bi ${iconClass}"></i>`;
    btn.onclick = onClick;
    return btn;
}

// Met à jour le compteur total dans l'onglet "Tous"
function updateCounts() {
    const s = document.getElementById('count-Tous');
    if (s) s.innerText = currentUserList.length;
}

// Applique le filtre par rôle
function filterByRole(role) {
    if (role.startsWith('Tous')) role = 'Tous';
    renderTable(role);
}

// Bascule le statut Actif/Bloqué d'un utilisateur
async function toggleBlock(id, roleName) {
    const user = currentUserList.find(u => u.id === id);
    if (!user) return;
    try {
        if (user.status === 'Actif') { await Api.blockUser(id, roleName); user.status = 'Bloqué'; }
        else { await Api.unblockUser(id, roleName); user.status = 'Actif'; }
        user.raw.statut = user.status === 'Actif' ? 'ACTIF' : 'INACTIF';
        renderTable(currentFilter);
        showToast(user.status === 'Actif' ? `${user.name} débloqué.` : `${user.name} bloqué.`,
            user.status === 'Actif' ? 'success' : 'warning');
    } catch (e) { showToast('Erreur statut : ' + parseApiError(e), 'danger'); }
}

// Supprime définitivement un utilisateur après confirmation
async function deleteUser(id, roleName) {
    const user = currentUserList.find(u => u.id === id);
    const userName = user ? user.name : 'cet utilisateur';
    const confirmed = await showConfirm(`Supprimer ${userName} ?`, 'Cette action est irréversible.');
    if (!confirmed) return;
    try {
        await Api.deleteUser(id, roleName);
        currentUserList = currentUserList.filter(u => u.id !== id);
        renderTable(currentFilter); updateCounts();
        showToast(`${userName} supprimé.`, 'success');
    } catch (e) { showToast('Erreur : ' + parseApiError(e), 'danger'); }
}

// Affiche les champs spécifiques selon le rôle sélectionné
function toggleRoleFields(role) {
    document.getElementById('enseignantFields')?.classList.add('d-none');
    document.getElementById('surveillantFields')?.classList.add('d-none');
    document.getElementById('assistantFields')?.classList.add('d-none');
    if (role === 'Enseignant') document.getElementById('enseignantFields')?.classList.remove('d-none');
    else if (role === 'Surveillant') document.getElementById('surveillantFields')?.classList.remove('d-none');
    else if (role === 'Assistant pédagogique') document.getElementById('assistantFields')?.classList.remove('d-none');
}

// Ouvre la modale vide pour la création d'un utilisateur
function openCreateModal() {
    currentStaffUser = null;
    document.getElementById('modalTitle').innerText = 'Nouvel Utilisateur';
    document.getElementById('userId').value = '';
    document.getElementById('userNom').value = '';
    document.getElementById('userPrenom').value = '';
    document.getElementById('userEmail').value = '';
    document.getElementById('userMatricule').value = '';
    document.getElementById('userPassword').value = '';
    document.getElementById('userPassword').required = true;
    document.getElementById('userRole').value = 'Enseignant';
    document.getElementById('userSpecialite') && (document.getElementById('userSpecialite').value = '');
    document.getElementById('userGrade') && (document.getElementById('userGrade').value = '');
    document.getElementById('userAffectation') && (document.getElementById('userAffectation').value = '');
    document.getElementById('userDepartement') && (document.getElementById('userDepartement').value = '');
    updateMatieresBadges([]); updateFilieresBadges([], 'currentFilieresBadges');
    updateFilieresBadges([], 'currentFilieresBadgesAP');
    toggleRoleFields('Enseignant');
    renderPermissionToggles('Enseignant', []);
    userModal.show();
}

// Ouvre la modale pré-remplie pour modifier un utilisateur
function openEditModal(id) {
    const u = currentUserList.find(user => user.id === id);
    if (!u) return;
    currentStaffUser = u;
    document.getElementById('modalTitle').innerText = `Modifier ${u.name}`;
    document.getElementById('userId').value = u.id;
    document.getElementById('userNom').value = u.raw.nom || '';
    document.getElementById('userPrenom').value = u.raw.prenom || '';
    document.getElementById('userEmail').value = u.raw.email;
    document.getElementById('userMatricule').value = u.raw.matricule || '';
    document.getElementById('userPassword').value = '';
    document.getElementById('userPassword').required = false;
    document.getElementById('userRole').value = u.role;
    document.getElementById('userSpecialite') && (document.getElementById('userSpecialite').value = u.raw.specialite || '');
    document.getElementById('userGrade') && (document.getElementById('userGrade').value = u.raw.grade || '');
    document.getElementById('userAffectation') && (document.getElementById('userAffectation').value = u.raw.affectation || '');
    document.getElementById('userDepartement') && (document.getElementById('userDepartement').value = u.raw.departement || '');
    updateMatieresBadges(u.matieres || []);
    updateFilieresBadges(u.filieres || [], 'currentFilieresBadges');
    updateFilieresBadges(u.filieres || [], 'currentFilieresBadgesAP');
    toggleRoleFields(u.role);
    // Charger les permissions retirées existantes
    const removedPerms = u.raw.removedPermissions || [];
    renderPermissionToggles(u.role, Array.isArray(removedPerms) ? removedPerms : [...removedPerms]);
    userModal.show();
}

// Met à jour les badges de matières dans le formulaire enseignant
function updateMatieresBadges(matieres) {
    const c = document.getElementById('currentMatieresBadges');
    if (!c) return;
    c.innerHTML = matieres.length > 0
        ? matieres.map(m => `<span class="badge rounded-pill" style="background:#fef9c3;color:#78350f;border:1px solid #fde68a;">${m.nomCours}</span>`).join(' ')
        : '<span class="text-muted small fst-italic">Aucune matière attribuée</span>';
}

// Met à jour les badges de filières dans le formulaire staff
function updateFilieresBadges(filieres, containerId = 'currentFilieresBadges') {
    const c = document.getElementById(containerId);
    if (!c) return;
    c.innerHTML = filieres.length > 0
        ? filieres.map(f => `<span class="badge rounded-pill" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;">${f.nomFiliere}</span>`).join(' ')
        : '<span class="text-muted small fst-italic">Aucune filière attribuée</span>';
}

// Ouvre la modale d'attribution des matières pour l'enseignant courant
window.openAssignMatieresModal = function () {
    const userId = document.getElementById('userId').value;
    const user = currentStaffUser || (userId ? currentUserList.find(u => u.id == userId) : null);
    if (!user) { showToast('Enregistrez d\'abord l\'utilisateur.', 'warning'); return; }
    currentStaffUser = user;
    document.getElementById('assignMatiereTitle').textContent = `Attribuer des matières — ${user.name}`;
    renderAssignMatiereList(user);
    new bootstrap.Modal(document.getElementById('assignMatiereModal')).show();
};

// Affiche la liste des matières avec leur statut d'attribution
function renderAssignMatiereList(user) {
    const container = document.getElementById('assignMatiereList');
    if (!container) return;
    if (allMatieres.length === 0) {
        container.innerHTML = '<p class="text-muted text-center py-4">Aucune matière disponible.</p>';
        return;
    }
    const assignedIds = new Set((user.matieres || []).map(m => m.id));
    container.innerHTML = allMatieres.map(m => {
        const isAssigned = assignedIds.has(m.id);
        const badge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#f59e0b;font-size:0.8rem;flex-shrink:0;">${(m.nomCours || m.nom || '?').charAt(0).toUpperCase()}</div>
                <div>
                    <div class="fw-semibold small">${m.nomCours || m.nom || '—'} <span class="text-muted fw-normal">(${m.code || '?'})</span></div>
                    <div class="mt-1">${badge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${isAssigned ? 'btn-outline-danger' : 'btn-outline-success'} rounded-3 px-3"
                    onclick="doToggleMatiere(${user.id},${m.id},${isAssigned})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

// Attribue ou retire une matière à un enseignant via PATCH /api/enseignants/{id}/cours
window.doToggleMatiere = async function (userId, matiereId, isCurrentlyAssigned) {
    const user = currentUserList.find(u => u.id == userId);
    if (!user) return;
    const m = allMatieres.find(x => x.id === matiereId);
    try {
        let newList;
        if (!isCurrentlyAssigned) {
            newList = [...(user.matieres || []).map(x => x.id), matiereId];
        } else {
            newList = (user.matieres || []).filter(x => x.id !== matiereId).map(x => x.id);
        }
        const res = await fetch(`${API_BASE}/enseignants/${userId}/cours`, {
            method: 'PATCH',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(newList)
        });
        if (!res.ok) throw new Error(await res.text());
        // Mise à jour locale
        if (!isCurrentlyAssigned && m) user.matieres.push({ id: matiereId, nomCours: m.nomCours || m.nom, code: m.code });
        else user.matieres = user.matieres.filter(x => x.id !== matiereId);
        user.details = user.matieres.length > 0 ? user.matieres.map(x => x.nomCours).join(', ') : '— Aucune matière';
        userMatiereMap[userId] = user.matieres;
        updateMatieresBadges(user.matieres);
        renderAssignMatiereList(user);
        renderTable(currentFilter);
        showToast(isCurrentlyAssigned ? `"${m?.nomCours}" retiré.` : `"${m?.nomCours}" attribué.`, 'success');
    } catch (e) { showToast('Erreur attribution : ' + parseApiError(e), 'danger'); }
};

// Ouvre la modale d'attribution des filières pour Surveillant/AP
window.openAssignFilieresModal = function () {
    const userId = document.getElementById('userId').value;
    const user = currentStaffUser || (userId ? currentUserList.find(u => u.id == userId) : null);
    if (!user) { showToast('Enregistrez d\'abord l\'utilisateur.', 'warning'); return; }
    currentStaffUser = user;
    document.getElementById('assignStaffFiliereTitle').textContent = `Attribuer des filières — ${user.name}`;
    document.getElementById('assignStaffFiliereSubtitle').textContent =
        user.role === 'Assistant pédagogique'
            ? 'Une filière ne peut avoir qu\'un seul assistant pédagogique.'
            : 'Un surveillant peut être attaché à plusieurs filières.';
    renderAssignStaffFiliereList(user);
    new bootstrap.Modal(document.getElementById('assignStaffFiliereModal')).show();
};

// Affiche la liste des filières avec le statut d'attribution
function renderAssignStaffFiliereList(user) {
    const container = document.getElementById('assignStaffFiliereList');
    if (!container) return;
    if (allFilieres.length === 0) { container.innerHTML = '<p class="text-muted text-center py-4">Aucune filière disponible.</p>'; return; }
    const assignedIds = new Set((user.filieres || []).map(f => f.id));
    container.innerHTML = allFilieres.map(f => {
        const isAssigned = assignedIds.has(f.id);
        const badge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        return `
        <div class="d-flex align-items-center justify-content-between px-3 py-2 rounded-3 border">
            <div class="d-flex align-items-center gap-3">
                <div class="rounded-circle d-flex align-items-center justify-content-center fw-bold text-white"
                     style="width:36px;height:36px;background:#6366f1;font-size:0.8rem;flex-shrink:0;">${(f.nomFiliere || '?').charAt(0).toUpperCase()}</div>
                <div>
                    <div class="fw-semibold small">${f.nomFiliere} <span class="text-muted fw-normal">(${f.idFiliere || '?'})</span></div>
                    <div class="mt-1">${badge}</div>
                </div>
            </div>
            <button class="btn btn-sm ${isAssigned ? 'btn-outline-danger' : 'btn-outline-success'} rounded-3 px-3"
                    onclick="doAssignStaffFiliere(${user.id},'${user.role}',${f.id},${isAssigned})">
                ${isAssigned ? '<i class="bi bi-x-lg me-1"></i>Retirer' : '<i class="bi bi-plus-lg me-1"></i>Attribuer'}
            </button>
        </div>`;
    }).join('');
}

// Attribue ou retire une filière à un Surveillant ou AP
window.doAssignStaffFiliere = async function (userId, role, filiereId, isCurrentlyAssigned) {
    const user = currentUserList.find(u => u.id == userId);
    if (!user) return;
    const endpoint = role === 'Assistant pédagogique' ? `${API_BASE}/assistants-pedagogiques` : `${API_BASE}/surveillants`;
    const filiere = allFilieres.find(f => f.id === filiereId);
    try {
        if (!isCurrentlyAssigned) {
            const res = await fetch(`${endpoint}/${userId}/filiere/${filiereId}`, { method: 'POST' });
            if (!res.ok) throw new Error(await res.text());
            if (filiere && !user.filieres.some(f => f.id === filiereId))
                user.filieres.push({ id: filiereId, nomFiliere: filiere.nomFiliere });
            showToast(`"${filiere?.nomFiliere}" attribuée.`, 'success');
        } else {
            user.filieres = user.filieres.filter(f => f.id !== filiereId);
            showToast(`"${filiere?.nomFiliere}" retirée.`, 'info');
        }
        user.details = user.filieres.length > 0 ? user.filieres.map(f => f.nomFiliere).join(', ') : '— Aucune filière';
        userFiliereMap[userId] = user.filieres;
        updateFilieresBadges(user.filieres, 'currentFilieresBadges');
        updateFilieresBadges(user.filieres, 'currentFilieresBadgesAP');
        renderAssignStaffFiliereList(user);
        renderTable(currentFilter);
    } catch (e) { showToast('Erreur attribution : ' + parseApiError(e), 'danger'); }
};

// Ouvre la card de visualisation des détails d'un utilisateur
function openViewModal(u) {
    document.getElementById('viewUserName').innerText = u.name;
    document.getElementById('viewUserEmail').innerText = u.email;
    document.getElementById('viewUserRole').innerText = u.role;
    document.getElementById('viewUserMatricule').innerText = u.raw.matricule || '—';
    document.getElementById('viewUserStatus').innerHTML =
        `<span class="badge ${u.status === 'Actif' ? 'bg-success' : 'bg-danger'}">${u.status}</span>`;
    const avatar = document.getElementById('viewUserAvatar');
    avatar.style.backgroundColor = u.color;
    avatar.innerText = u.name.charAt(0);

    const extraDiv = document.getElementById('viewUserExtra');
    if (u.role === 'Enseignant') {
        const matieresBadges = u.matieres.length > 0
            ? u.matieres.map(m => `<span class="badge rounded-pill me-1 mb-1" style="background:#fef9c3;color:#78350f;border:1px solid #fde68a;">${m.nomCours}</span>`).join('')
            : '<span class="text-muted fst-italic small">Aucune matière attribuée</span>';
        extraDiv.innerHTML = `
            <div class="bg-light p-3 rounded-3 mb-2">
                <label class="small text-muted fw-bold">SPÉCIALITÉ</label>
                <div class="fw-semibold">${u.raw.specialite || '—'}</div>
            </div>
            <div class="bg-light p-3 rounded-3">
                <label class="small text-muted fw-bold d-flex align-items-center gap-1 mb-2"><i class="bi bi-book"></i> MATIÈRES</label>
                <div class="d-flex flex-wrap gap-1">${matieresBadges}</div>
            </div>`;
    } else {
        const filiereBadges = u.filieres.length > 0
            ? u.filieres.map(f => `<span class="badge rounded-pill me-1 mb-1" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;">${f.nomFiliere}</span>`).join('')
            : '<span class="text-muted fst-italic small">Aucune filière attribuée</span>';
        extraDiv.innerHTML = `
            <div class="bg-light p-3 rounded-3">
                <label class="small text-muted fw-bold d-flex align-items-center gap-1 mb-2"><i class="bi bi-diagram-3"></i> FILIÈRES ATTRIBUÉES</label>
                <div class="d-flex flex-wrap gap-1">${filiereBadges}</div>
            </div>`;
    }
    // Afficher les permissions effectives
    const permList = document.getElementById('viewPermissionsList');
    if (permList) {
        const rolePerms = u.raw.rolePermissions || [];
        const removedPerms = u.raw.removedPermissions || [];
        const effectivePerms = u.raw.effectivePermissions || [];
        const allPerms = [...new Set([...rolePerms])];
        if (allPerms.length > 0) {
            permList.innerHTML = allPerms.map(p => {
                const isRemoved = (Array.isArray(removedPerms) ? removedPerms : [...removedPerms]).includes(p);
                const label = PERM_LABELS[p] || p;
                if (isRemoved) {
                    return `<span class="badge rounded-pill me-1 mb-1" style="background:#fee2e2;color:#991b1b;border:1px solid #fca5a5;text-decoration:line-through;">
                        <i class="bi bi-x-circle me-1"></i>${label}</span>`;
                } else {
                    return `<span class="badge rounded-pill me-1 mb-1" style="background:#d1fae5;color:#065f46;border:1px solid #6ee7b7;">
                        <i class="bi bi-check-circle me-1"></i>${label}</span>`;
                }
            }).join('');
        } else {
            permList.innerHTML = '<span class="text-muted fst-italic small">Aucune permission</span>';
        }
    }
    viewModal.show();
}

// Affiche les toggles pour les permissions du rôle sélectionné
function renderPermissionToggles(roleName, removedPermissions = []) {
    const container = document.getElementById('permissionToggles');
    if (!container) return;

    // Mapper le nom frontend vers le nom backend du rôle
    const roleMap = {
        'Enseignant': 'ENSEIGNANT',
        'Surveillant': 'SURVEILLANT',
        'Assistant pédagogique': 'ASSISTANT_PEDAGOGIQUE'
    };
    const backendRoleName = roleMap[roleName] || roleName;
    const role = allRoles.find(r => r.name === backendRoleName);

    if (!role || !role.permissions || role.permissions.length === 0) {
        container.innerHTML = '<div class="text-center text-muted small py-3">Aucune permission pour ce rôle</div>';
        return;
    }

    const perms = [...role.permissions].sort();
    container.innerHTML = perms.map(perm => {
        const isRemoved = removedPermissions.includes(perm);
        const label = PERM_LABELS[perm] || perm;
        return `
            <div class="d-flex align-items-center justify-content-between py-1 px-2 border-bottom" style="border-color:#f1f1f1!important;">
                <div class="small">
                    <strong>${label}</strong>
                </div>
                <div class="form-check form-switch mb-0">
                    <input class="form-check-input perm-toggle" type="checkbox" data-perm="${perm}" ${isRemoved ? '' : 'checked'}>
                </div>
            </div>`;
    }).join('');
}

// Récupère la liste des permissions retirées depuis les toggles
function getRemovedPermissionsFromToggles() {
    const toggles = document.querySelectorAll('.perm-toggle');
    const removed = [];
    toggles.forEach(t => {
        if (!t.checked) removed.push(t.dataset.perm);
    });
    return removed;
}

// Valide le formulaire et enregistre l'utilisateur via l'API
async function handleSaveUser() {
    const id = document.getElementById('userId').value;
    const roleStr = document.getElementById('userRole').value;
    const baseData = {
        nom: document.getElementById('userNom').value.trim(),
        prenom: document.getElementById('userPrenom').value.trim(),
        email: document.getElementById('userEmail').value,
        matricule: document.getElementById('userMatricule').value.trim(),
        statut: 'ACTIF'
    };
    const password = document.getElementById('userPassword').value;
    if (password || !id) baseData.password = password || 'Pass1234';

    // Collecter les permissions retirées
    const removedPermissions = getRemovedPermissionsFromToggles();
    if (removedPermissions.length > 0) {
        baseData.removedPermissions = removedPermissions;
    }

    if (roleStr === 'Enseignant') {
        baseData.specialite = document.getElementById('userSpecialite')?.value || '';
        baseData.grade = document.getElementById('userGrade')?.value || '';
        baseData.roleNames = ['ENSEIGNANT'];
    } else if (roleStr === 'Surveillant') {
        baseData.affectation = document.getElementById('userAffectation')?.value || '';
        baseData.roleNames = ['SURVEILLANT'];
    } else if (roleStr === 'Assistant pédagogique') {
        baseData.departement = document.getElementById('userDepartement')?.value || '';
        baseData.roleNames = ['ASSISTANT_PEDAGOGIQUE'];
    }

    try {
        if (id) {
            const original = currentUserList.find(u => u.id == id);
            if (!original) throw new Error('Utilisateur original non trouvé');
            await Api.updateUser(id, baseData, original.raw.roleNames[0]);
        } else {
            const created = await Api.createUser(baseData);
            currentStaffUser = normalizeUser(created);
            currentStaffUser.filieres = []; currentStaffUser.matieres = [];
            document.getElementById('userId').value = created.id;
        }
        userModal.hide();
        await loadUsers();
        showToast(id ? 'Utilisateur modifié.' : 'Utilisateur créé.', 'success');
    } catch (e) {
        showToast('Erreur : ' + parseApiError(e), 'danger');
        console.error(e);
    }
}
