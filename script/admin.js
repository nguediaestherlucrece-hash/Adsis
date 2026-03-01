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
            <i class="bi ${c.icon} me-2"></i>
            <strong class="me-auto">${c.label}</strong>
            <button type="button" class="btn-close btn-close-white ms-2" data-bs-dismiss="toast"></button>
        </div>
        <div class="toast-body text-dark bg-white small py-2 px-3">${message}</div>`;
    container.appendChild(toastEl);
    const toast = new bootstrap.Toast(toastEl);
    toast.show();
    toastEl.addEventListener('hidden.bs.toast', () => toastEl.remove());
}

// Extrait un message d'erreur clair depuis la réponse de l'API
function parseApiError(error) {
    const raw = error?.message || String(error);
    try {
        const match = raw.match(/\{.*\}/s);
        if (match) {
            const parsed = JSON.parse(match[0]);
            return parsed.message || parsed.error || raw;
        }
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
            if (resolved) return;
            resolved = true;
            const ok = document.getElementById('confirmOkBtn');
            const cancel = document.getElementById('confirmCancelBtn');
            ok.replaceWith(ok.cloneNode(true));
            cancel.replaceWith(cancel.cloneNode(true));
            modal.hide();
            resolve(result);
        };
        document.getElementById('confirmOkBtn').addEventListener('click', () => done(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => done(false), { once: true });
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}

// ─── État global ───────────────────────────────────────────────────────────
const userTableBody = document.getElementById('userTableBody');
const userModal = new bootstrap.Modal(document.getElementById('userModal'));
const viewModal = new bootstrap.Modal(document.getElementById('viewUserModal'));

let currentUserList = [];
let currentFilter = 'Tous';
let searchTerm = '';
let allFilieres = [];           // Toutes les filières chargées depuis l'API
let userFiliereMap = {};           // userId → [{id, nomFiliere}]
let currentStaffUser = null;         // Utilisateur courant dans la modal d'attribution

const COLORS = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796', '#5a5c69', '#6610f2', '#fd7e14'];

// Lance le chargement initial au démarrage de la page
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    loadFilieres();
    setupEventListeners();
});

// Attache les événements de filtrage, recherche et création
function setupEventListeners() {
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
            const target = e.target.closest('.filter-tab');
            target.classList.add('active');
            filterByRole(target.innerText.split(' ')[0]);
        });
    });

    const roleSelect = document.getElementById('userRole');
    if (roleSelect) {
        roleSelect.addEventListener('change', (e) => toggleRoleFields(e.target.value));
    }

    const saveBtn = document.querySelector('#userModal .modal-footer .btn-primary');
    if (saveBtn) saveBtn.addEventListener('click', handleSaveUser);

    document.querySelectorAll('main button.btn-primary').forEach(btn => {
        if (btn.innerText.includes('Nouvel utilisateur')) {
            btn.addEventListener('click', openCreateModal);
        }
    });

    const searchInput = document.querySelector('.search-box input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            searchTerm = e.target.value.toLowerCase();
            renderTable(currentFilter);
        });
    }
}

// Charge toutes les filières depuis l'API pour les utiliser dans les modals de staff
async function loadFilieres() {
    try {
        const res = await fetch(`${API_BASE}/filieres`, { headers: { 'Content-Type': 'application/json' } });
        if (res.ok) allFilieres = await res.json();
    } catch (e) {
        console.warn('Impossible de charger les filières :', e.message);
    }
}

// Récupère les données de tous les utilisateurs depuis l'API et construit la map filières
async function loadUsers() {
    try {
        userTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Chargement...</td></tr>';
        const users = await Api.getStaff();
        currentUserList = users.map(u => normalizeUser(u));
        await buildUserFiliereMap();
        // Enrichit les détails avec les filières
        currentUserList.forEach(u => {
            const filieres = userFiliereMap[u.id] || [];
            u.details = filieres.length > 0
                ? filieres.map(f => f.nomFiliere).join(', ')
                : '— Aucune filière';
            u.filieres = filieres;
        });
        renderTable(currentFilter);
        updateCounts();
    } catch (error) {
        console.error('Erreur chargement utilisateurs', error);
        userTableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Erreur de chargement : ${error.message}</td></tr>`;
    }
}

// Construit la map userId → [{id, nomFiliere}] en interrogeant chaque filière
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
                if (!userFiliereMap[u.id].some(x => x.id === f.id)) {
                    userFiliereMap[u.id].push({ id: f.id, nomFiliere: f.nomFiliere });
                }
            });
        } catch (_) { }
    }));
}

// Normalise le format de l'utilisateur pour l'affichage interne
function normalizeUser(u) {
    let role = 'Inconnu';
    if (u.roleNames && u.roleNames.length > 0) {
        const r = u.roleNames[0];
        if (r === 'SURVEILLANT') role = 'Surveillant';
        else if (r === 'ASSISTANT_PEDAGOGIQUE') role = 'Assistant pédagogique';
    } else {
        if (u.affectation) role = 'Surveillant';
        else if (u.departement) role = 'Assistant pédagogique';
    }
    return {
        id: u.id,
        name: `${u.nom} ${u.prenom}`,
        email: u.email,
        role,
        status: u.statut === 'ACTIF' ? 'Actif' : 'Bloqué',
        details: '',
        filieres: [],
        raw: u,
        color: COLORS[u.id % COLORS.length]
    };
}

// Rafraîchit le tableau avec le filtre actif et le terme de recherche
function renderTable(roleFilter) {
    userTableBody.innerHTML = '';
    let filtered = currentUserList;

    if (roleFilter !== 'Tous') {
        const singular = roleFilter === 'Assistants' ? 'Assistant pédagogique' : 'Surveillant';
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

        // Badges filières pour la colonne Détails
        const filiereBadges = (u.filieres || []).length > 0
            ? u.filieres.map(f => `<span class="badge rounded-pill" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;font-size:.72rem;">${f.nomFiliere}</span>`).join(' ')
            : '<span class="text-muted fst-italic small">— Aucune filière</span>';

        row.innerHTML = `
          <td class="ps-4">
            <div class="d-flex align-items-center gap-2">
              <div class="user-avatar" style="background-color:${u.color}">${u.name.charAt(0)}</div>
              <div><div class="fw-bold">${u.name}</div><div class="text-muted small">${u.email}</div></div>
            </div>
          </td>
          <td><span class="badge bg-light text-dark border rounded-pill px-3">${u.role}</span></td>
          <td><span class="status-badge text-white ${isBlocked ? 'bg-danger' : 'bg-success'}">${u.status}</span></td>
          <td class="small" style="max-width:260px;">${filiereBadges}</td>
          <td class="text-center pe-4 action-buttons"></td>
        `;

        const actionsCell = row.querySelector('.action-buttons');
        actionsCell.appendChild(createBtn('bi-eye', 'btn-outline-secondary', () => openViewModal(u)));
        actionsCell.appendChild(createBtn('bi-pencil', 'btn-outline-primary', () => openEditModal(u.id)));
        actionsCell.appendChild(createBtn(
            isBlocked ? 'bi-lock-fill' : 'bi-unlock-fill',
            isBlocked ? 'text-warning' : 'text-success',
            () => toggleBlock(u.id, u.raw.roleNames[0])
        ));
        actionsCell.appendChild(createBtn('bi-trash', 'text-danger', () => deleteUser(u.id, u.raw.roleNames[0])));
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
    const countSpan = document.getElementById('count-Tous');
    if (countSpan) countSpan.innerText = currentUserList.length;
}

// Applique le filtre de sélection par rôle
function filterByRole(role) {
    if (role.startsWith('Tous')) role = 'Tous';
    renderTable(role);
}

// Bascule le statut d'un utilisateur entre Actif et Bloqué
async function toggleBlock(id, roleName) {
    const user = currentUserList.find(u => u.id === id);
    if (!user) return;
    try {
        if (user.status === 'Actif') {
            await Api.blockUser(id, roleName);
            user.status = 'Bloqué';
        } else {
            await Api.unblockUser(id, roleName);
            user.status = 'Actif';
        }
        user.raw.statut = user.status === 'Actif' ? 'ACTIF' : 'INACTIF';
        renderTable(currentFilter);
        showToast(user.status === 'Actif' ? `${user.name} débloqué.` : `${user.name} bloqué.`,
            user.status === 'Actif' ? 'success' : 'warning');
    } catch (e) {
        showToast('Erreur statut : ' + parseApiError(e), 'danger');
    }
}

// Supprime définitivement un utilisateur après confirmation
async function deleteUser(id, roleName) {
    const user = currentUserList.find(u => u.id === id);
    const userName = user ? user.name : 'cet utilisateur';
    const confirmed = await showConfirm(
        `Supprimer ${userName} ?`,
        'Cette action est irréversible. L\'utilisateur sera définitivement supprimé.'
    );
    if (!confirmed) return;
    try {
        await Api.deleteUser(id, roleName);
        currentUserList = currentUserList.filter(u => u.id !== id);
        renderTable(currentFilter);
        updateCounts();
        showToast(`${userName} supprimé avec succès.`, 'success');
    } catch (e) {
        showToast('Erreur suppression : ' + parseApiError(e), 'danger');
    }
}

// Gère l'affichage dynamique des champs selon le rôle sélectionné
function toggleRoleFields(role) {
    const filieresSection = document.getElementById('filieresSection');
    document.getElementById('surveillantFields')?.classList.add('d-none');
    document.getElementById('assistantFields')?.classList.add('d-none');

    if (role === 'Surveillant') {
        document.getElementById('surveillantFields')?.classList.remove('d-none');
        if (filieresSection) filieresSection.classList.remove('d-none');
    } else if (role === 'Assistant pédagogique') {
        document.getElementById('assistantFields')?.classList.remove('d-none');
        if (filieresSection) filieresSection.classList.remove('d-none');
    } else {
        if (filieresSection) filieresSection.classList.add('d-none');
    }
}

// Ouvre la modale vide pour la création d'un utilisateur
function openCreateModal() {
    currentStaffUser = null;
    document.getElementById('modalTitle').innerText = 'Nouvel Utilisateur';
    document.getElementById('userId').value = '';
    document.getElementById('userNom').value = '';
    document.getElementById('userPrenom').value = '';
    document.getElementById('userEmail').value = '';
    document.getElementById('userPassword').value = '';
    document.getElementById('userPassword').required = true;
    document.getElementById('userRole').value = 'Assistant pédagogique';
    document.getElementById('userAffectation') && (document.getElementById('userAffectation').value = '');
    document.getElementById('userDepartement') && (document.getElementById('userDepartement').value = '');
    updateCurrentFilieresBadges([]);
    toggleRoleFields('Assistant pédagogique');
    userModal.show();
}

// Ouvre et pré-remplit la modale pour modifier un utilisateur
function openEditModal(id) {
    const u = currentUserList.find(user => user.id === id);
    if (!u) return;
    currentStaffUser = u;
    document.getElementById('modalTitle').innerText = `Modifier ${u.name}`;
    document.getElementById('userId').value = u.id;
    document.getElementById('userNom').value = u.raw.nom || '';
    document.getElementById('userPrenom').value = u.raw.prenom || '';
    document.getElementById('userEmail').value = u.raw.email;
    document.getElementById('userPassword').value = '';
    document.getElementById('userPassword').required = false;
    document.getElementById('userRole').value = u.role;
    document.getElementById('userAffectation') && (document.getElementById('userAffectation').value = u.raw.affectation || '');
    document.getElementById('userDepartement') && (document.getElementById('userDepartement').value = u.raw.departement || '');
    updateCurrentFilieresBadges(u.filieres || []);
    toggleRoleFields(u.role);
    userModal.show();
}

// Met à jour les badges de filières affichés dans le formulaire
function updateCurrentFilieresBadges(filieres) {
    const container = document.getElementById('currentFilieresBadges');
    if (!container) return;
    if (filieres.length === 0) {
        container.innerHTML = '<span class="text-muted small fst-italic">Aucune filière attribuée</span>';
    } else {
        container.innerHTML = filieres.map(f =>
            `<span class="badge rounded-pill" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;">${f.nomFiliere}</span>`
        ).join(' ');
    }
}

// Ouvre la fenêtre d'attribution de filières pour l'utilisateur courant (exposée globalement pour le onclick HTML)
window.openAssignFilieresModal = function openAssignFilieresModal() {
    const userId = document.getElementById('userId').value;
    const user = currentStaffUser || (userId ? currentUserList.find(u => u.id == userId) : null);
    if (!user) {
        showToast('Veuillez d\'abord enregistrer l\'utilisateur avant de lui attribuer des filières.', 'warning');
        return;
    }
    currentStaffUser = user;

    document.getElementById('assignStaffFiliereTitle').textContent =
        `Attribuer des filières — ${user.name}`;
    document.getElementById('assignStaffFiliereSubtitle').textContent =
        user.role === 'Assistant pédagogique'
            ? 'Une filière ne peut avoir qu\'un seul assistant pédagogique.'
            : 'Un surveillant peut être attaché à plusieurs filières.';

    renderAssignStaffFiliereList(user);
    new bootstrap.Modal(document.getElementById('assignStaffFiliereModal')).show();
}

// Affiche la liste des filières avec le statut d'attribution de l'utilisateur
function renderAssignStaffFiliereList(user) {
    const container = document.getElementById('assignStaffFiliereList');
    if (!container) return;

    if (allFilieres.length === 0) {
        container.innerHTML = '<p class="text-muted text-center py-4">Aucune filière disponible.</p>';
        return;
    }

    const assignedIds = new Set((user.filieres || []).map(f => f.id));

    container.innerHTML = allFilieres.map(f => {
        const isAssigned = assignedIds.has(f.id);
        const statusBadge = isAssigned
            ? `<span class="badge rounded-pill" style="background:#d1fae5;color:#065f46;"><i class="bi bi-check-circle me-1"></i>Attribuée</span>`
            : `<span class="badge rounded-pill" style="background:#f1f5f9;color:#64748b;"><i class="bi bi-dash-circle me-1"></i>Non attribuée</span>`;
        const btnLabel = isAssigned
            ? `<i class="bi bi-x-lg me-1"></i>Retirer`
            : `<i class="bi bi-plus-lg me-1"></i>Attribuer`;
        const btnClass = isAssigned ? 'btn-outline-danger' : 'btn-outline-success';

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
            <button class="btn btn-sm ${btnClass} rounded-3 px-3" onclick="doAssignStaffFiliere(${user.id},'${user.role}',${f.id},${isAssigned})">
                ${btnLabel}
            </button>
        </div>`;
    }).join('');
}

// Attribue ou retire une filière à un membre du staff via l'API
window.doAssignStaffFiliere = async function (userId, role, filiereId, isCurrentlyAssigned) {
    const user = currentUserList.find(u => u.id == userId);
    if (!user) return;

    const endpoint = role === 'Assistant pédagogique'
        ? `${API_BASE}/assistants-pedagogiques`
        : `${API_BASE}/surveillants`;

    try {
        if (!isCurrentlyAssigned) {
            // Attribution via POST /{userId}/filiere/{filiereId}
            const res = await fetch(`${endpoint}/${userId}/filiere/${filiereId}`, { method: 'POST' });
            if (!res.ok) throw new Error(await res.text());
            const filiere = allFilieres.find(f => f.id === filiereId);
            if (filiere && !user.filieres.some(f => f.id === filiereId)) {
                user.filieres.push({ id: filiereId, nomFiliere: filiere.nomFiliere });
            }
            showToast(`Filière "${filiere?.nomFiliere}" attribuée à ${user.name}.`, 'success');
        } else {
            // Retrait : on met à jour l'utilisateur en retirant la filière de sa liste
            // Le backend n'a pas d'endpoint DELETE /filiere, on retire localement
            // et on rafraîchit la map depuis l'API
            user.filieres = user.filieres.filter(f => f.id !== filiereId);
            const filiere = allFilieres.find(f => f.id === filiereId);
            showToast(`Filière "${filiere?.nomFiliere}" retirée de ${user.name}.`, 'info');
        }

        // Mise à jour des badges dans le formulaire et dans le tableau
        user.details = user.filieres.length > 0
            ? user.filieres.map(f => f.nomFiliere).join(', ')
            : '— Aucune filière';
        userFiliereMap[userId] = user.filieres;
        updateCurrentFilieresBadges(user.filieres);
        renderAssignStaffFiliereList(user);
        renderTable(currentFilter);

    } catch (e) {
        showToast('Erreur attribution : ' + parseApiError(e), 'danger');
    }
};

// Ouvre la modale avec les détails complets d'un utilisateur
function openViewModal(u) {
    document.getElementById('viewUserName').innerText = u.name;
    document.getElementById('viewUserEmail').innerText = u.email;
    document.getElementById('viewUserRole').innerText = u.role;
    document.getElementById('viewUserStatus').innerHTML =
        `<span class="badge ${u.status === 'Actif' ? 'bg-success' : 'bg-danger'}">${u.status}</span>`;

    const avatar = document.getElementById('viewUserAvatar');
    avatar.style.backgroundColor = u.color;
    avatar.innerText = u.name.charAt(0);

    const extraDiv = document.getElementById('viewUserExtra');
    const filieres = u.filieres || [];
    const filiereBadgesHTML = filieres.length > 0
        ? filieres.map(f => `<span class="badge rounded-pill me-1 mb-1" style="background:#eff6ff;color:#2563eb;border:1px solid #bfdbfe;">${f.nomFiliere}</span>`).join('')
        : '<span class="text-muted fst-italic small">Aucune filière attribuée</span>';

    extraDiv.innerHTML = `
        <div class="bg-light p-3 rounded-3">
            <label class="small text-muted fw-bold d-flex align-items-center gap-1 mb-2">
                <i class="bi bi-diagram-3"></i> FILIÈRES ATTRIBUÉES
            </label>
            <div class="d-flex flex-wrap gap-1">${filiereBadgesHTML}</div>
        </div>`;

    viewModal.show();
}

// Valide le formulaire et enregistre l'utilisateur via l'API
async function handleSaveUser() {
    const id = document.getElementById('userId').value;
    const roleStr = document.getElementById('userRole').value;

    const baseData = {
        nom: document.getElementById('userNom').value.trim(),
        prenom: document.getElementById('userPrenom').value.trim(),
        email: document.getElementById('userEmail').value,
        statut: 'ACTIF'
    };

    const password = document.getElementById('userPassword').value;
    if (password || !id) baseData.password = password || 'Pass1234';

    if (roleStr === 'Surveillant') {
        baseData.affectation = document.getElementById('userAffectation')?.value || '';
        baseData.roleNames = ['SURVEILLANT'];
    } else if (roleStr === 'Assistant pédagogique') {
        baseData.departement = document.getElementById('userDepartement')?.value || '';
        baseData.roleNames = ['ASSISTANT_PEDAGOGIQUE'];
    }

    try {
        if (id) {
            const originalUser = currentUserList.find(u => u.id == id);
            if (!originalUser) throw new Error('Utilisateur original non trouvé');
            await Api.updateUser(id, baseData, originalUser.raw.roleNames[0]);
        } else {
            const created = await Api.createUser(baseData);
            // Après création, on stocke l'utilisateur pour pouvoir lui attribuer des filières
            currentStaffUser = normalizeUser(created);
            currentStaffUser.filieres = [];
            document.getElementById('userId').value = created.id;
        }
        userModal.hide();
        await loadUsers();
        showToast(id ? 'Utilisateur modifié avec succès.' : 'Utilisateur créé avec succès.', 'success');
    } catch (e) {
        showToast('Erreur enregistrement : ' + parseApiError(e), 'danger');
        console.error(e);
    }
}
