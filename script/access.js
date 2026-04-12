import { requireRole } from './auth.js';
import { Api } from './api.js';

// Guard : seuls les ADMINs peuvent gérer les droits d'accès
if (!requireRole(['ADMIN'])) { throw new Error('Unauthorized'); }

// ============ TOAST NOTIFICATIONS ============
function showToast(message, type = 'info') {
    const cfg = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger:  { icon: 'bi-x-circle-fill',     label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info:    { icon: 'bi-info-circle-fill',   label: 'Info',   bg: '#1a6fa8', delay: 4000 },
    };
    const c = cfg[type] || cfg.info;
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'position-fixed top-0 end-0 p-3';
        container.style.zIndex = '9999';
        document.body.appendChild(container);
    }
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

const PERMISSION_DEFINITIONS = {
    'COURSE_VIEW': { group: 'Cours', desc: 'Voir les cours' },
    'COURSE_MANAGE': { group: 'Cours', desc: 'Gérer les cours' },
    'USER_VIEW': { group: 'Utilisateurs', desc: 'Voir les utilisateurs' },
    'USER_MANAGE': { group: 'Utilisateurs', desc: 'Gérer les utilisateurs' },
    'STUDENT_VIEW': { group: 'Étudiants', desc: 'Voir les étudiants' },
    'ATTENDANCE_VIEW': { group: 'Présences', desc: 'Voir les présences' },
    'ATTENDANCE_MANAGE': { group: 'Présences', desc: 'Gérer les présences' },
    'JUSTIFICATION_VALIDATE': { group: 'Justificatifs', desc: 'Valider les justificatifs' },
    'STATISTICS_VIEW': { group: 'Statistiques', desc: 'Voir les statistiques' }
};

let allRoles = [];
let currentRole = null;

// Lance le chargement initial de la page
document.addEventListener('DOMContentLoaded', () => {
    loadRoles();
    setupSaveButton();
});

// Attache l'événement de sauvegarde au bouton sans le dupliquer
function setupSaveButton() {
    const saveBtn = document.querySelector('.btn-primary');
    if (saveBtn) {
        const newBtn = saveBtn.cloneNode(true);
        saveBtn.replaceWith(newBtn);
        newBtn.addEventListener('click', savePermissions);
    }
}

// Récupère les rôles depuis le backend et les affiche
async function loadRoles() {
    try {
        allRoles = await Api.getRoles();
        renderRolesList(allRoles);
        if (allRoles.length > 0) selectRole(allRoles[0]);
    } catch (e) {
        console.error(e);
        showToast('Erreur lors du chargement des rôles. Vérifiez le backend.', 'danger');
    }
}

// Construit le menu latéral avec la liste des rôles
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

// Met à jour l'interface lorsqu'un rôle est cliqué
function selectRole(role) {
    currentRole = role;
    const badge = document.getElementById('role-badge');
    if (badge) badge.innerText = role.name;
    renderPermissions(role.permissions || []);
}

// Affiche les différentes permissions groupées par catégories
function renderPermissions(activePermissions) {
    const container = document.getElementById('permissionsContainer');
    if (!container) return;
    container.innerHTML = '';

    const groups = {};
    Object.keys(PERMISSION_DEFINITIONS).forEach(key => {
        const def = PERMISSION_DEFINITIONS[key];
        if (!groups[def.group]) groups[def.group] = [];
        groups[def.group].push({ ...def, key });
    });

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
                    <input class="form-check-input perm-check" type="checkbox" id="${perm.key}" ${isChecked ? 'checked' : ''}>
                </div>
            `;
            itemsContainer.appendChild(itemDiv);
        });
        container.appendChild(itemsContainer);
    });

    document.querySelectorAll('.perm-check').forEach(cb => {
        cb.addEventListener('change', updateCount);
    });
    updateCount();
}

// Compte et affiche le nombre de permissions activées
function updateCount() {
    const total = document.querySelectorAll('.perm-check').length;
    const checked = document.querySelectorAll('.perm-check:checked').length;
    const countText = document.getElementById('count-text');
    if (countText) countText.innerText = `${checked} / ${total} activées`;
}

// Envoie les permissions modifiées au backend pour le rôle actif
async function savePermissions() {
    if (!currentRole) return;
    const newPerms = Array.from(document.querySelectorAll('.perm-check:checked')).map(cb => cb.id);
    try {
        await Api.updateRolePermissions(currentRole.id, newPerms);
        currentRole.permissions = newPerms;
        showToast('Permissions sauvegardées avec succès.', 'success');

        const btn = document.querySelector('.btn-primary');
        const oldText = btn.innerText;
        btn.innerText = "Sauvegardé !";
        btn.classList.add('btn-success');
        setTimeout(() => {
            btn.innerText = oldText;
            btn.classList.remove('btn-success');
        }, 2000);
    } catch (e) {
        showToast('Erreur de sauvegarde : ' + e.message, 'danger');
    }
}
