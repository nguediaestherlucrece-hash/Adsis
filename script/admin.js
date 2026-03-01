import { Api } from './api.js';

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
    } catch (_) { /* pas du JSON */ }
    return raw;
}

// Affiche une fenêtre de confirmation avant une action critique
function showConfirm(title = 'Confirmer', message = 'Êtes-vous sûr ?') {
    return new Promise((resolve) => {
        const modalEl = document.getElementById('confirmModal');
        document.getElementById('confirmModalTitle').innerText = title;
        document.getElementById('confirmModalMessage').innerText = message;

        const modal = new bootstrap.Modal(modalEl);
        const okBtn = document.getElementById('confirmOkBtn');
        const cancelBtn = document.getElementById('confirmCancelBtn');

        const cleanup = (result) => {
            modal.hide();
            // Remove listeners to éviter les doublons
            okBtn.replaceWith(okBtn.cloneNode(true));
            cancelBtn.replaceWith(cancelBtn.cloneNode(true));
            resolve(result);
        };

        // Re-select after cloneNode
        document.getElementById('confirmOkBtn').addEventListener('click', () => cleanup(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => cleanup(false), { once: true });
        modalEl.addEventListener('hide.bs.modal', () => resolve(false), { once: true });

        modal.show();
    });
}

// DOM Elements
const userTableBody = document.getElementById('userTableBody');
const userModal = new bootstrap.Modal(document.getElementById('userModal'));
const viewModal = new bootstrap.Modal(document.getElementById('viewUserModal'));
const userForm = document.getElementById('userForm'); // Need to create a form element wrapper in HTML if not present, or select inputs directly
// Actually the current HTML doesn't have a <form> tag wrapping inputs, inputs are direct children of modal-body divs. 
// I should probably wrap them or just select by ID. Standard approach is by ID.

// State
let currentUserList = [];
let currentFilter = 'Tous';

// Constants
const COLORS = ['#4e73df', '#1cc88a', '#36b9cc', '#f6c23e', '#e74a3b', '#858796', '#5a5c69', '#6610f2', '#fd7e14'];

// Lance le chargement initial au démarrage de la page
document.addEventListener('DOMContentLoaded', () => {
    loadUsers();
    setupEventListeners();
});

function setupEventListeners() {
    // Role filter tabs
    document.querySelectorAll('.filter-tab').forEach(tab => {
        tab.addEventListener('click', (e) => {
            document.querySelectorAll('.filter-tab').forEach(t => t.classList.remove('active'));
            // Handle clicking on the span number inside the tab
            const target = e.target.closest('.filter-tab');
            target.classList.add('active');
            filterByRole(target.innerText.split(' ')[0]); // "Tous", "Étudiants", etc.
        });
    });

    // Modal role change
    document.getElementById('userRole').addEventListener('change', toggleRoleFields);

    // Save button
    // Selector based on modal footer
    const saveBtn = document.querySelector('#userModal .modal-footer .btn-primary');
    if (saveBtn) {
        saveBtn.addEventListener('click', handleSaveUser);
    }

    // Create button in topbar
    const createBtns = document.querySelectorAll('main button.btn-primary');
    createBtns.forEach(btn => {
        if (btn.innerText.includes("Nouvel utilisateur")) {
            btn.addEventListener('click', openCreateModal);
        }
    });

    // Role select change listener
    const roleSelect = document.getElementById('userRole');
    if (roleSelect) {
        roleSelect.addEventListener('change', (e) => {
            toggleRoleFields(e.target.value);
        });
    }

    // Search input
    const searchInput = document.querySelector('.search-box input');
    if (searchInput) {
        searchInput.addEventListener('input', (e) => {
            const term = e.target.value.toLowerCase();
            const filtered = currentUserList.filter(u =>
                u.name.toLowerCase().includes(term) ||
                u.email.toLowerCase().includes(term) ||
                (u.details && u.details.toLowerCase().includes(term))
            );
            // Render filtered list, respecting current role filter if possible, 
            // but for simplicity search overrides or combines.
            // Let's verify compatibility with renderTable
            // renderTable uses currentFilter. 
            // Better to modify renderTable to accept a search term or separate filtering.
            // Simplified: Update currentUserList? No, that holds data.
            // We create a temporary render.
            // Since our architecture uses `renderTable` which pulls from `currentUserList` and filters by `currentFilter`...
            // Let's modify renderTable logic instead to consider a global searchTerm state.
            updateSearchTerm(term);
        });
    }
}

let searchTerm = "";

function updateSearchTerm(term) {
    searchTerm = term;
    renderTable(currentFilter);
}

// Récupère les données de tous les utilisateurs depuis l'API
async function loadUsers() {
    try {
        userTableBody.innerHTML = '<tr><td colspan="5" class="text-center py-4">Chargement...</td></tr>';
        const users = await Api.getUsers(); // Actually fetches all roles

        // Transform API data to match our internal structure if needed
        // API returns objects with different structures based on role.
        // We need to map them to a unified structure for the table.
        currentUserList = users.map(u => normalizeUser(u));

        renderTable(currentFilter);
        updateCounts();
    } catch (error) {
        console.error("Erreur chargement utilisateurs", error);
        userTableBody.innerHTML = `<tr><td colspan="5" class="text-center text-danger py-4">Erreur lors du chargement des données: ${error.message}</td></tr>`;
    }
}

// Normalise le format de l'utilisateur pour l'affichage interne
function normalizeUser(u) {
    // Detect role from object properties or roleNames
    // API returns roleNames: ["ETUDIANT"] or has specific fields
    let role = "Inconnu";
    let details = "";

    // Check properties to determine type if roleNames is missing (fallback)
    if (u.matricule) role = "Étudiant";
    else if (u.grade || u.specialite) role = "Enseignant";
    else if (u.affectation) role = "Surveillant";
    else if (u.departement) role = "Assistant pédagogique";

    // Override if roleNames exists (standard way)
    if (u.roleNames && u.roleNames.length > 0) {
        const r = u.roleNames[0];
        if (r === "ETUDIANT") role = "Étudiant";
        else if (r === "ENSEIGNANT") role = "Enseignant";
        else if (r === "SURVEILLANT") role = "Surveillant";
        else if (r === "ASSISTANT_PEDAGOGIQUE") role = "Assistant pédagogique";
    }

    // Details string
    if (role === "Enseignant") details = u.specialite || u.grade || "-";
    else if (role === "Étudiant") details = `Mat: ${u.matricule || "-"}`;
    else if (role === "Surveillant") details = u.affectation || "-";
    else if (role === "Assistant pédagogique") details = u.departement || "-";

    return {
        id: u.id,
        name: `${u.nom} ${u.prenom}`,
        email: u.email,
        role: role,
        status: u.statut === "ACTIF" ? "Actif" : "Bloqué",
        details: details,
        // Keep raw data for editing
        raw: u,
        // Assign random color if not present (could be hashed from name)
        color: COLORS[u.id % COLORS.length]
    };
}

// Rafraîchit l'affichage du tableau avec les données filtrées
function renderTable(roleFilter) {
    userTableBody.innerHTML = '';

    let filtered = currentUserList;
    if (roleFilter !== 'Tous') {
        // Handle plural/singular mapping
        let filterSingular = roleFilter;
        if (roleFilter === 'Étudiants') filterSingular = "Étudiant";
        if (roleFilter === 'Enseignants') filterSingular = "Enseignant";
        if (roleFilter === 'Assistants') filterSingular = "Assistant pédagogique";
        if (roleFilter === 'Surveillants') filterSingular = "Surveillant";

        filtered = filtered.filter(u => u.role === filterSingular);
    }

    // Apply search filter if exists
    if (searchTerm) {
        filtered = filtered.filter(u =>
            u.name.toLowerCase().includes(searchTerm) ||
            u.email.toLowerCase().includes(searchTerm) ||
            (u.details && u.details.toLowerCase().includes(searchTerm))
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

        row.innerHTML = `
          <td class="ps-4">
            <div class="d-flex align-items-center gap-2">
              <div class="user-avatar" style="background-color: ${u.color}">${u.name.charAt(0)}</div>
              <div><div class="fw-bold">${u.name}</div><div class="text-muted small">${u.email}</div></div>
            </div>
          </td>
          <td><span class="badge bg-light text-dark border rounded-pill px-3">${u.role}</span></td>
          <td><span class="status-badge text-white ${isBlocked ? 'bg-danger' : 'bg-success'}">${u.status}</span></td>
          <td class="small text-muted text-truncate" style="max-width: 250px;">${u.details}</td>
          <td class="text-center pe-4 action-buttons">
            <!-- Buttons injected via JS for event handling -->
          </td>
        `;

        // Add buttons with event listeners
        const actionsCell = row.querySelector('.action-buttons');

        // View
        const viewBtn = createBtn('bi-eye', 'btn-outline-secondary', () => openViewModal(u));
        actionsCell.appendChild(viewBtn);

        // Edit
        const editBtn = createBtn('bi-pencil', 'btn-outline-primary', () => openEditModal(u.id));
        actionsCell.appendChild(editBtn);

        // Block/Unblock
        const blockBtn = createBtn(
            isBlocked ? 'bi-lock-fill' : 'bi-unlock-fill',
            isBlocked ? 'text-warning' : 'text-success',
            () => toggleBlock(u.id, u.raw.roleNames[0])
        );
        actionsCell.appendChild(blockBtn);

        // Delete
        const deleteBtn = createBtn('bi-trash', 'text-danger', () => deleteUser(u.id, u.raw.roleNames[0]));
        actionsCell.appendChild(deleteBtn);

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

function updateCounts() {
    // Simple implementation for just "Tous" as requested in current DOM
    const countSpan = document.getElementById('count-Tous');
    if (countSpan) countSpan.innerText = currentUserList.length;
}

// Applique le filtre de sélection par rôle
function filterByRole(role) {
    if (role.startsWith("Tous")) role = "Tous"; // Handle "Tous (0)" text
    renderTable(role);
}

// Bascule le statut de l'utilisateur (Bloqué / Actif)
async function toggleBlock(id, roleName) {
    // L'API attend une méthode updateUser avec status
    // On doit d'abord savoir si c'est block ou unblock
    const user = currentUserList.find(u => u.id === id);
    if (!user) return;

    // Note: l'API demande "statut": "ACTIF" ou "INACTIF"
    // MAIS l'API `blockUser` dans api.js gère déjà ça.

    try {
        if (user.status === "Actif") {
            await Api.blockUser(id, roleName);
            user.status = "Bloqué";
        } else {
            await Api.unblockUser(id, roleName);
            user.status = "Actif";
        }

        // Update both local list and UI immediately without reload
        user.raw.statut = user.status === "Actif" ? "ACTIF" : "INACTIF";
        renderTable(currentFilter);
        showToast(
            user.status === "Actif" ? `${user.name} a été débloqué.` : `${user.name} a été bloqué.`,
            user.status === "Actif" ? 'success' : 'warning'
        );

    } catch (e) {
        showToast('Erreur lors de la modification du statut : ' + parseApiError(e), 'danger');
    }
}

// Supprime totalement l'utilisateur et met à jour l'interface
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
        showToast(`${userName} a été supprimé avec succès.`, 'success');
    } catch (e) {
        showToast('Erreur lors de la suppression : ' + parseApiError(e), 'danger');
    }
}

// Gère l'affichage dynamique des champs selon le rôle sélectionné
function toggleRoleFields(role) {
    const matriculeField = document.getElementById('matriculeField');
    const etudiantFields = document.getElementById('etudiantFields');
    const enseignantFields = document.getElementById('enseignantFields');
    const surveillantFields = document.getElementById('surveillantFields');
    const assistantFields = document.getElementById('assistantFields');

    // Hide all
    matriculeField.classList.add('d-none');
    etudiantFields.classList.add('d-none');
    enseignantFields.classList.add('d-none');
    surveillantFields.classList.add('d-none');
    assistantFields.classList.add('d-none');

    // Show relevant
    if (role === "Étudiant") {
        matriculeField.classList.remove('d-none');
        etudiantFields.classList.remove('d-none');
    } else if (role === "Enseignant") {
        enseignantFields.classList.remove('d-none');
    } else if (role === "Surveillant") {
        surveillantFields.classList.remove('d-none');
    } else if (role === "Assistant pédagogique") {
        assistantFields.classList.remove('d-none');
    }
}

// Ouvre la modale vide pour la création d'un utilisateur
function openCreateModal() {
    document.getElementById('modalTitle').innerText = "Nouvel Utilisateur";
    document.getElementById('userId').value = "";
    document.getElementById('userNom').value = "";
    document.getElementById('userPrenom').value = "";
    document.getElementById('userEmail').value = "";
    document.getElementById('userPassword').value = "";
    document.getElementById('userPassword').required = true;
    document.getElementById('userRole').value = "Étudiant";

    // Clear specific fields
    document.getElementById('userMatricule').value = "";
    document.getElementById('userBirthdate').value = "";
    document.getElementById('userPhone').value = "";
    document.getElementById('userSpecialite').value = "";
    document.getElementById('userAffectation').value = "";
    document.getElementById('userDepartement').value = "";

    toggleRoleFields("Étudiant");

    userModal.show();
}

// Ouvre et pré-remplit la modale pour modifier un utilisateur
function openEditModal(id) {
    const u = currentUserList.find(user => user.id === id);
    if (!u) return;

    document.getElementById('userId').value = u.id;
    document.getElementById('userNom').value = u.raw.nom || "";
    document.getElementById('userPrenom').value = u.raw.prenom || "";
    document.getElementById('userEmail').value = u.raw.email;
    document.getElementById('userPassword').value = "";
    document.getElementById('userPassword').required = false;
    document.getElementById('userRole').value = u.role;

    // Specific fields
    document.getElementById('userMatricule').value = u.raw.matricule || "";
    document.getElementById('userBirthdate').value = u.raw.dateNaissance || "";
    document.getElementById('userPhone').value = u.raw.telephone || "";
    document.getElementById('userSpecialite').value = u.raw.specialite || "";
    document.getElementById('userAffectation').value = u.raw.affectation || "";
    document.getElementById('userDepartement').value = u.raw.departement || "";

    toggleRoleFields(u.role);
    document.getElementById('modalTitle').innerText = `Modifier ${u.name}`;
    userModal.show();
}

// Ouvre la modale affichant les détails complets d'un utilisateur
function openViewModal(u) {
    document.getElementById('viewUserName').innerText = u.name;
    document.getElementById('viewUserEmail').innerText = u.email;
    document.getElementById('viewUserRole').innerText = u.role;
    document.getElementById('viewUserStatus').innerHTML = `<span class="badge ${u.status === 'Actif' ? 'bg-success' : 'bg-danger'}">${u.status}</span>`;

    // Avatar
    const avatar = document.getElementById('viewUserAvatar');
    avatar.style.backgroundColor = u.color;
    avatar.innerText = u.name.charAt(0);

    // Extra info
    const extraDiv = document.getElementById('viewUserExtra');
    extraDiv.innerHTML = "";
    if (u.role === 'Étudiant') {
        extraDiv.innerHTML = `
            <div class="bg-light p-3 rounded-3">
                <div class="row g-3">
                    <div class="col-6"><label class="small text-muted fw-bold">MATRICULE</label><div class="fw-semibold text-primary">${u.raw.matricule || "-"}</div></div>
                    <div class="col-6"><label class="small text-muted fw-bold">DATE DE NAISSANCE</label><div class="fw-semibold">${u.raw.dateNaissance || "-"}</div></div>
                    <div class="col-12"><label class="small text-muted fw-bold">TÉLÉPHONE</label><div class="fw-semibold">${u.raw.telephone || "-"}</div></div>
                </div>
            </div>`;
    } else if (u.role === 'Enseignant') {
        extraDiv.innerHTML = `
            <div class="bg-light p-3 rounded-3">
                <label class="small text-muted fw-bold">SPÉCIALITÉ</label>
                <div class="fw-semibold">${u.raw.specialite || "-"}</div>
            </div>`;
    } else if (u.role === 'Surveillant') {
        extraDiv.innerHTML = `
            <div class="bg-light p-3 rounded-3">
                <label class="small text-muted fw-bold">Étage</label>
                <div class="fw-semibold">${u.raw.affectation || "-"}</div>
            </div>`;
    } else if (u.role === 'Assistant pédagogique') {
        extraDiv.innerHTML = `
            <div class="bg-light p-3 rounded-3">
                <label class="small text-muted fw-bold">Cycle</label>
                <div class="fw-semibold">${u.raw.departement || "-"}</div>
            </div>`;
    } else {
        extraDiv.innerHTML = '';
    }

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
        statut: "ACTIF"
    };

    // Add password only if provided (required for creation, optional for update)
    const password = document.getElementById('userPassword').value;
    if (password || !id) {
        baseData.password = password || "Pass1234";
    }

    // Specific fields
    if (roleStr === "Étudiant") {
        baseData.matricule = document.getElementById('userMatricule').value;
        baseData.dateNaissance = document.getElementById('userBirthdate').value;
        baseData.telephone = document.getElementById('userPhone').value;
        baseData.roleNames = ["ETUDIANT"];
    } else if (roleStr === "Enseignant") {
        baseData.specialite = document.getElementById('userSpecialite').value;
        baseData.roleNames = ["ENSEIGNANT"];
    } else if (roleStr === "Surveillant") {
        baseData.affectation = document.getElementById('userAffectation').value;
        baseData.roleNames = ["SURVEILLANT"];
    } else if (roleStr === "Assistant pédagogique") {
        baseData.departement = document.getElementById('userDepartement').value;
        baseData.roleNames = ["ASSISTANT_PEDAGOGIQUE"];
    }

    try {
        if (id) {
            // Update
            // Need to pass the original role to know which endpoint to hit!
            // We can find the user in our list
            const originalUser = currentUserList.find(u => u.id == id); // loose equality for string id
            if (!originalUser) throw new Error("Utilisateur original non trouvé");

            // Note: API update endpoint might not support changing role (moving from /etudiants to /enseignants).
            // We assume role cannot be changed in update here for simplicity or simply utilize the endpoint logic.

            await Api.updateUser(id, baseData, originalUser.raw.roleNames[0]);
        } else {
            // Create
            await Api.createUser(baseData);
        }

        userModal.hide();
        loadUsers(); // Reload list
        showToast(id ? 'Utilisateur modifié avec succès.' : 'Utilisateur créé avec succès.', 'success');

    } catch (e) {
        showToast('Erreur lors de l\'enregistrement : ' + parseApiError(e), 'danger');
        console.error(e);
    }
}
