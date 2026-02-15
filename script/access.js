import { Api } from './api.js';

// Définitions des permissions pour l'interface
const PERMISSION_DEFINITIONS = {
    // Cours
    'COURSE_VIEW': { group: 'Cours', desc: 'Voir les cours' },
    'COURSE_MANAGE': { group: 'Cours', desc: 'Gérer les cours' },

    // Utilisateurs
    'USER_VIEW': { group: 'Utilisateurs', desc: 'Voir les utilisateurs' },
    'USER_MANAGE': { group: 'Utilisateurs', desc: 'Gérer les utilisateurs' },

    // Étudiants
    'STUDENT_VIEW': { group: 'Étudiants', desc: 'Voir les étudiants' },

    // Présences
    'ATTENDANCE_VIEW': { group: 'Présences', desc: 'Voir les présences' },
    'ATTENDANCE_MANAGE': { group: 'Présences', desc: 'Gérer les présences' },

    // Justificatifs
    'JUSTIFICATION_VALIDATE': { group: 'Justificatifs', desc: 'Valider les justificatifs' },

    // Statistiques
    'STATISTICS_VIEW': { group: 'Statistiques', desc: 'Voir les statistiques' }
};

// État local
let allRoles = [];
let currentRole = null;

// Initialisation
document.addEventListener('DOMContentLoaded', () => {
    loadRoles();
    setupSaveButton();
});

// Configuration bouton sauvegarde
function setupSaveButton() {
    const saveBtn = document.querySelector('.btn-primary');
    if (saveBtn) {
        // Remplace l'élément pour supprimer les anciens listeners
        const newBtn = saveBtn.cloneNode(true);
        saveBtn.replaceWith(newBtn);
        newBtn.addEventListener('click', savePermissions);
    }
}

// Chargement des rôles depuis l'API
async function loadRoles() {
    try {
        allRoles = await Api.getRoles();
        renderRolesList(allRoles);

        if (allRoles.length > 0) {
            selectRole(allRoles[0]);
        }
    } catch (e) {
        console.error("Erreur chargement rôles", e);
        alert("Erreur chargement rôles. Vérifiez le backend.");
    }
}

// Affichage liste des rôles
function renderRolesList(roles) {
    const listContainer = document.querySelector('.d-flex.flex-column.gap-3');
    if (!listContainer) return;

    listContainer.innerHTML = '';

    roles.forEach(role => {
        const div = document.createElement('div');
        div.className = 'role-card';
        div.innerHTML = `<h6>${role.name}</h6><small class="text-muted">${role.description || '-'}</small>`;

        div.addEventListener('click', () => {
            selectRole(role);
            document.querySelectorAll('.role-card').forEach(c => c.classList.remove('active'));
            div.classList.add('active');
        });

        listContainer.appendChild(div);
    });
}

// Sélection d'un rôle
function selectRole(role) {
    currentRole = role;

    const badge = document.getElementById('role-badge');
    if (badge) badge.innerText = role.name;

    renderPermissions(role.permissions || []);
}

// Affichage des permissions
function renderPermissions(activePermissions) {
    const container = document.getElementById('permissionsContainer');
    if (!container) return;

    container.innerHTML = '';

    // Groupement
    const groups = {};
    Object.keys(PERMISSION_DEFINITIONS).forEach(key => {
        const def = PERMISSION_DEFINITIONS[key];
        if (!groups[def.group]) groups[def.group] = [];
        groups[def.group].push({ ...def, key });
    });

    // Rendu par groupe
    Object.keys(groups).forEach(groupName => {
        const titleDiv = document.createElement('div');
        titleDiv.className = 'perm-group-title text-uppercase';
        titleDiv.innerText = groupName;
        container.appendChild(titleDiv);

        const itemsContainer = document.createElement('div');
        itemsContainer.className = 'd-flex flex-wrap gap-3';

        groups[groupName].forEach(perm => {
            const isChecked = activePermissions.includes(perm.key);
            const itemDiv = document.createElement('div');
            itemDiv.className = 'perm-item';
            itemDiv.innerHTML = `
                <div><strong>${perm.desc}</strong></div>
                <div class="form-check form-switch">
                    <input class="form-check-input perm-check" type="checkbox" 
                           id="${perm.key}" ${isChecked ? 'checked' : ''}>
                </div>
            `;
            itemsContainer.appendChild(itemDiv);
        });
        container.appendChild(itemsContainer);
    });

    // Listeners sur les checkbox
    document.querySelectorAll('.perm-check').forEach(cb => {
        cb.addEventListener('change', updateCount);
    });

    updateCount();
}

// Mise à jour compteur
function updateCount() {
    const total = document.querySelectorAll('.perm-check').length;
    const checked = document.querySelectorAll('.perm-check:checked').length;
    const countText = document.getElementById('count-text');
    if (countText) countText.innerText = `${checked} / ${total} activées`;
}

// Sauvegarde des permissions
async function savePermissions() {
    if (!currentRole) return;

    const newPerms = Array.from(document.querySelectorAll('.perm-check:checked')).map(cb => cb.id);

    try {
        await Api.updateRolePermissions(currentRole.id, newPerms);
        currentRole.permissions = newPerms;

        // Feedback visuel
        const btn = document.querySelector('.btn-primary');
        const oldText = btn.innerText;
        btn.innerText = "Sauvegardé !";
        btn.classList.add('btn-success');

        setTimeout(() => {
            btn.innerText = oldText;
            btn.classList.remove('btn-success');
        }, 2000);

    } catch (e) {
        alert("Erreur sauvegarde: " + e.message);
    }
}
