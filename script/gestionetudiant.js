// ==================== CONFIGURATION ====================
const API_BASE_URL = 'http://localhost:8081/api';

// ==================== GLOBAL VARIABLES ====================
let classes = [];         // Liste des classes (spécialités)
let students = [];        // Étudiants stockés en localStorage
let nextId = 1;

// ==================== TOAST NOTIFICATIONS ====================
function showToast(message, type = 'info') {
    const config = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger: { icon: 'bi-x-circle-fill', label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info: { icon: 'bi-info-circle-fill', label: 'Info', bg: '#1a6fa8', delay: 4000 }
    };
    const c = config[type] || config.info;
    let container = document.getElementById('toastContainer');
    if (!container) {
        container = document.createElement('div');
        container.id = 'toastContainer';
        container.className = 'toast-container position-fixed bottom-0 end-0 p-3';
        document.body.appendChild(container);
    }
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

// ==================== CONFIRMATION MODAL ====================
function showConfirm(title = 'Confirmer', message = 'Êtes-vous sûr ?') {
    return new Promise((resolve) => {
        let modalEl = document.getElementById('confirmModal');
        if (!modalEl) {
            modalEl = document.createElement('div');
            modalEl.id = 'confirmModal';
            modalEl.className = 'modal fade';
            modalEl.setAttribute('tabindex', '-1');
            modalEl.innerHTML = `
                <div class="modal-dialog">
                    <div class="modal-content">
                        <div class="modal-header">
                            <h5 class="modal-title" id="confirmModalTitle">Confirmer</h5>
                            <button type="button" class="btn-close" data-bs-dismiss="modal"></button>
                        </div>
                        <div class="modal-body" id="confirmModalMessage">Êtes-vous sûr ?</div>
                        <div class="modal-footer">
                            <button type="button" class="btn btn-secondary" id="confirmCancelBtn">Annuler</button>
                            <button type="button" class="btn btn-primary" id="confirmOkBtn">Confirmer</button>
                        </div>
                    </div>
                </div>`;
            document.body.appendChild(modalEl);
        }
        document.getElementById('confirmModalTitle').innerText = title;
        document.getElementById('confirmModalMessage').innerText = message;
        const modal = new bootstrap.Modal(modalEl);
        let resolved = false;
        const done = (result) => {
            if (resolved) return;
            resolved = true;
            modal.hide();
            resolve(result);
        };
        document.getElementById('confirmOkBtn').onclick = () => done(true);
        document.getElementById('confirmCancelBtn').onclick = () => done(false);
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}

// ==================== API : CHARGER LES CLASSES ====================
async function loadClasses() {
    try {
        const response = await fetch(`${API_BASE_URL}/specialites`);
        if (response.ok) {
            classes = await response.json();
            populateClassSelects();
            showToast('Classes chargées', 'success');
        } else {
            showToast('Erreur chargement des classes', 'danger');
        }
    } catch (error) {
        console.error(error);
        showToast('Impossible de contacter le serveur', 'danger');
    }
}

// ==================== GESTION DES ÉTUDIANTS (localStorage) ====================
function loadStudents() {
    const saved = localStorage.getItem('students');
    if (saved) {
        students = JSON.parse(saved);
        nextId = students.length ? Math.max(...students.map(s => s.id)) + 1 : 1;
    }
}
function saveStudents() { localStorage.setItem('students', JSON.stringify(students)); }

// Remplir les <select> avec les classes (nom de la classe)
function populateClassSelects() {
    const filtre = document.getElementById('filtreClasse');
    const modalSelect = document.getElementById('classeModalSelect');
    if (!filtre || !modalSelect) return;

    const classNames = classes.map(c => c.nom || c.libelle || c);
    filtre.innerHTML = '<option value="all">Toutes les classes</option>';
    modalSelect.innerHTML = '';
    classNames.forEach(nom => {
        filtre.innerHTML += `<option value="${nom}">${nom}</option>`;
        modalSelect.innerHTML += `<option value="${nom}">${nom}</option>`;
    });
}

// Afficher la liste des étudiants (filtre par classe)
function renderStudents() {
    const selectedClass = document.getElementById('filtreClasse').value;
    const filtered = selectedClass === 'all' ? students : students.filter(s => s.class === selectedClass);
    const container = document.getElementById('studentsList');
    const totalSpan = document.getElementById('totalEtudiants');
    totalSpan.textContent = filtered.length;

    if (!filtered.length) {
        container.innerHTML = '<div class="no-data"><i class="fas fa-user-graduate me-2"></i>Aucun étudiant dans cette classe.</div>';
        return;
    }
    container.innerHTML = filtered.map(s => `
        <div class="etudiant-item" data-id="${s.id}">
            <div class="etudiant-info">
                <span class="etudiant-nom">${escapeHtml(s.firstName)} ${escapeHtml(s.lastName)}</span>
                <span class="etudiant-classe">${escapeHtml(s.class)}</span>
            </div>
            <button class="btn-delete-etudiant" data-id="${s.id}"><i class="fas fa-trash-alt"></i></button>
        </div>
    `).join('');

    document.querySelectorAll('.btn-delete-etudiant').forEach(btn => {
        btn.addEventListener('click', async e => {
            const id = parseInt(btn.dataset.id);
            if (await showConfirm('Supprimer', 'Supprimer cet étudiant ?')) deleteStudent(id);
        });
    });
}

function addStudent(firstName, lastName, className) {
    const newStudent = { id: nextId++, firstName, lastName, class: className };
    students.push(newStudent);
    saveStudents();
    renderStudents();
    return true;
}

async function addStudentFromModal() {
    const firstName = document.getElementById('prenomInput').value.trim();
    const lastName = document.getElementById('nomInput').value.trim();
    const className = document.getElementById('classeModalSelect').value;
    if (!firstName || !lastName || !className) {
        showToast('Tous les champs sont requis', 'warning');
        return;
    }
    // Vérifier que la classe existe
    const classNames = classes.map(c => c.nom || c.libelle || c);
    if (!classNames.includes(className)) {
        showToast('Classe invalide', 'danger');
        await loadClasses(); // recharger au cas où
        return;
    }
    addStudent(firstName, lastName, className);
    const modal = bootstrap.Modal.getInstance(document.getElementById('ajoutEtudiantModal'));
    if (modal) modal.hide();
    document.getElementById('prenomInput').value = '';
    document.getElementById('nomInput').value = '';
    showToast('Étudiant ajouté', 'success');
}

function deleteStudent(id) {
    students = students.filter(s => s.id !== id);
    saveStudents();
    renderStudents();
    showToast('Étudiant supprimé', 'success');
}

// ==================== IMPORT EXCEL ====================
function importExcel(file) {
    const reader = new FileReader();
    reader.onload = function(e) {
        const workbook = XLSX.read(new Uint8Array(e.target.result), { type: 'array' });
        const sheet = workbook.Sheets[workbook.SheetNames[0]];
        const rows = XLSX.utils.sheet_to_json(sheet, { header: 1, defval: '' });
        if (rows.length < 2) {
            showToast('Le fichier ne contient pas de données', 'warning');
            return;
        }
        const classNames = classes.map(c => c.nom || c.libelle || c);
        let imported = 0, skipped = 0;
        for (let i = 1; i < rows.length; i++) {
            const row = rows[i];
            if (row.length < 3) continue;
            const nom = row[0] ? String(row[0]).trim() : '';
            const prenom = row[1] ? String(row[1]).trim() : '';
            const classe = row[2] ? String(row[2]).trim() : '';
            if (nom && prenom && classe) {
                if (classNames.includes(classe)) {
                    addStudent(prenom, nom, classe);
                    imported++;
                } else {
                    skipped++;
                }
            }
        }
        if (imported) showToast(`${imported} étudiant(s) importé(s). ${skipped} ignoré(s) (classe inconnue).`, 'success');
        else if (skipped) showToast(`${skipped} étudiant(s) ignorés (classe inconnue).`, 'warning');
        else showToast('Aucun étudiant valide trouvé', 'info');
    };
    reader.readAsArrayBuffer(file);
}

// ==================== UTILITAIRES ====================
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, m => ({ '&': '&amp;', '<': '&lt;', '>': '&gt;' }[m]));
}

// ==================== INITIALISATION ====================
document.addEventListener('DOMContentLoaded', async () => {
    await loadClasses();
    loadStudents();
    renderStudents();

    document.getElementById('openModalBtn').onclick = () => new bootstrap.Modal(document.getElementById('ajoutEtudiantModal')).show();
    document.getElementById('confirmAjoutBtn').onclick = addStudentFromModal;
    document.getElementById('filtreClasse').onchange = renderStudents;

    const importBtn = document.getElementById('importBtn');
    const fileInput = document.getElementById('fileInput');
    importBtn.onclick = () => fileInput.click();
    fileInput.onchange = e => {
        if (e.target.files.length) {
            importExcel(e.target.files[0]);
            fileInput.value = '';
        }
    };
});