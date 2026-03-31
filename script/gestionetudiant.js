// ==================== CONFIGURATION API ====================
const API_BASE_URL = 'http://localhost:8081/api';

// ==================== VARIABLES GLOBALES ====================
let specialites = [];     // Liste des classes (spécialités)
let enseignants = [];     // Liste des enseignants (non utilisée ici)
let matieres = [];        // Liste des matières (non utilisée ici)
let students = [];        // Liste des étudiants (stockée en localStorage)
let nextId = 1;

// ==================== TOAST & CONFIRM ====================
function showToast(message, type = 'info') {
    const config = {
        success: { icon: 'bi-check-circle-fill', label: 'Succès', bg: '#1a7f5a', delay: 3500 },
        danger: { icon: 'bi-x-circle-fill', label: 'Erreur', bg: '#c0392b', delay: 6000 },
        warning: { icon: 'bi-exclamation-triangle-fill', label: 'Attention', bg: '#d68910', delay: 5000 },
        info: { icon: 'bi-info-circle-fill', label: 'Info', bg: '#1a6fa8', delay: 4000 },
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
            document.getElementById('confirmOkBtn').replaceWith(document.getElementById('confirmOkBtn').cloneNode(true));
            document.getElementById('confirmCancelBtn').replaceWith(document.getElementById('confirmCancelBtn').cloneNode(true));
            modal.hide();
            resolve(result);
        };
        document.getElementById('confirmOkBtn').addEventListener('click', () => done(true), { once: true });
        document.getElementById('confirmCancelBtn').addEventListener('click', () => done(false), { once: true });
        modalEl.addEventListener('hidden.bs.modal', () => done(false), { once: true });
        modal.show();
    });
}

// ==================== APPELS API ====================
async function fetchSpecialites() {
    try {
        const response = await fetch(`${API_BASE_URL}/specialites`);
        if (response.ok) {
            specialites = await response.json();
            populateClassSelects();        // met à jour les <select>
            showToast('Classes chargées avec succès', 'success');
        } else {
            showToast('Erreur lors du chargement des classes', 'danger');
        }
    } catch (error) {
        console.error('Erreur réseau (spécialités) :', error);
        showToast('Impossible de contacter le serveur', 'danger');
    }
}

// Ces deux fonctions sont optionnelles, on les garde pour une éventuelle utilisation future
async function fetchEnseignants() {
    try {
        const response = await fetch(`${API_BASE_URL}/enseignants/actifs`);
        if (response.ok) {
            enseignants = await response.json();
        }
    } catch (error) {
        console.error('Erreur réseau (enseignants) :', error);
    }
}

async function fetchMatieres() {
    try {
        const response = await fetch(`${API_BASE_URL}/cours-ue`);
        if (response.ok) {
            matieres = await response.json();
        }
    } catch (error) {
        console.error('Erreur réseau (matières) :', error);
    }
}

// ==================== GESTION DES ÉTUDIANTS ====================
function loadStudents() {
    const saved = localStorage.getItem('students');
    if (saved) {
        students = JSON.parse(saved);
        nextId = students.length ? Math.max(...students.map(s => s.id)) + 1 : 1;
    }
}

function saveStudents() {
    localStorage.setItem('students', JSON.stringify(students));
}

// Remplir les <select> avec les spécialités récupérées
function populateClassSelects() {
    const classeSelect = document.getElementById('classeSelect');
    const filtreClasse = document.getElementById('filtreClasse');

    if (!classeSelect || !filtreClasse) return;

    // Garder l'option "Toutes les classes" pour le filtre
    filtreClasse.innerHTML = '<option value="all">Toutes les classes</option>';
    classeSelect.innerHTML = '';

    specialites.forEach(spec => {
        const nomClasse = spec.nom || spec.libelle || spec; // adapter selon la structure de l'API
        const option1 = document.createElement('option');
        option1.value = nomClasse;
        option1.textContent = nomClasse;
        classeSelect.appendChild(option1);

        const option2 = document.createElement('option');
        option2.value = nomClasse;
        option2.textContent = nomClasse;
        filtreClasse.appendChild(option2);
    });

    // Si aucune spécialité, afficher un message
    if (specialites.length === 0) {
        classeSelect.innerHTML = '<option value="">Aucune classe disponible</option>';
        filtreClasse.innerHTML = '<option value="all">Toutes les classes</option><option value="">Aucune classe</option>';
    }
}

// Afficher les étudiants selon le filtre
function renderStudents() {
    const filterValue = document.getElementById('filtreClasse').value;
    let filtered = students;
    if (filterValue !== 'all') {
        filtered = students.filter(s => s.class === filterValue);
    }

    const container = document.getElementById('studentsList');
    const totalSpan = document.getElementById('totalEtudiants');
    totalSpan.textContent = filtered.length;

    if (filtered.length === 0) {
        container.innerHTML = '<div class="no-data"><i class="fas fa-user-graduate me-2"></i>Aucun étudiant dans cette classe.</div>';
        return;
    }

    container.innerHTML = filtered.map(student => `
        <div class="etudiant-item" data-id="${student.id}">
            <div class="etudiant-info">
                <span class="etudiant-nom">${escapeHtml(student.firstName)} ${escapeHtml(student.lastName)}</span>
                <span class="etudiant-classe">${escapeHtml(student.class)}</span>
            </div>
            <button class="btn-delete-etudiant" data-id="${student.id}"><i class="fas fa-trash-alt"></i></button>
        </div>
    `).join('');

    // Attacher les événements de suppression
    document.querySelectorAll('.btn-delete-etudiant').forEach(btn => {
        btn.addEventListener('click', async (e) => {
            const id = parseInt(btn.dataset.id);
            const confirm = await showConfirm('Supprimer', 'Voulez-vous vraiment supprimer cet étudiant ?');
            if (confirm) deleteStudent(id);
        });
    });
}

// Ajouter un étudiant
async function addStudent() {
    const firstName = document.getElementById('prenomInput').value.trim();
    const lastName = document.getElementById('nomInput').value.trim();
    const classValue = document.getElementById('classeSelect').value;

    if (!firstName || !lastName || !classValue) {
        showToast('Veuillez remplir tous les champs', 'warning');
        return;
    }

    // Vérifier que la classe existe dans la liste des spécialités
    const classeExiste = specialites.some(spec => (spec.nom || spec.libelle || spec) === classValue);
    if (!classeExiste) {
        showToast('La classe sélectionnée n’existe plus. Rafraîchissez la page.', 'danger');
        await fetchSpecialites(); // recharger les classes
        return;
    }

    const newStudent = {
        id: nextId++,
        firstName: firstName,
        lastName: lastName,
        class: classValue
    };
    students.push(newStudent);
    saveStudents();
    renderStudents();

    // Réinitialiser le formulaire
    document.getElementById('prenomInput').value = '';
    document.getElementById('nomInput').value = '';
    document.getElementById('classeSelect').selectedIndex = 0;

    showToast('Étudiant ajouté avec succès', 'success');
}

// Supprimer un étudiant
function deleteStudent(id) {
    students = students.filter(s => s.id !== id);
    saveStudents();
    renderStudents();
    showToast('Étudiant supprimé', 'success');
}

// Exporter la liste filtrée en PDF
function exportToPDF() {
    const filterValue = document.getElementById('filtreClasse').value;
    const title = filterValue === 'all' ? 'Tous les étudiants' : `Étudiants de ${filterValue}`;

    const element = document.createElement('div');
    element.style.padding = '20px';
    element.style.backgroundColor = 'white';
    element.style.fontFamily = 'Arial, sans-serif';

    element.innerHTML = `
        <div style="text-align: center; margin-bottom: 20px;">
            <h2>${escapeHtml(title)}</h2>
            <p>Généré le ${new Date().toLocaleDateString('fr-FR')}</p>
        </div>
        <table style="width: 100%; border-collapse: collapse;">
            <thead>
                <tr style="background-color: #4e73df; color: white;">
                    <th style="padding: 8px; border: 1px solid #ddd;">Prénom</th>
                    <th style="padding: 8px; border: 1px solid #ddd;">Nom</th>
                    <th style="padding: 8px; border: 1px solid #ddd;">Classe</th>
                </tr>
            </thead>
            <tbody>
    `;

    const filtered = filterValue === 'all' ? students : students.filter(s => s.class === filterValue);
    filtered.forEach(s => {
        element.innerHTML += `
            <tr>
                <td style="padding: 8px; border: 1px solid #ddd;">${escapeHtml(s.firstName)}</td>
                <td style="padding: 8px; border: 1px solid #ddd;">${escapeHtml(s.lastName)}</td>
                <td style="padding: 8px; border: 1px solid #ddd;">${escapeHtml(s.class)}</td>
            </tr>
        `;
    });
    element.innerHTML += `</tbody></table>`;

    const opt = {
        margin: 0.5,
        filename: `liste_etudiants_${filterValue.replace(/ /g, '_')}.pdf`,
        image: { type: 'jpeg', quality: 0.98 },
        html2canvas: { scale: 2 },
        jsPDF: { unit: 'in', format: 'a4', orientation: 'portrait' }
    };
    html2pdf().set(opt).from(element).save();
}

// Petite fonction utilitaire pour échapper les caractères HTML
function escapeHtml(str) {
    if (!str) return '';
    return str.replace(/[&<>]/g, function(m) {
        if (m === '&') return '&amp;';
        if (m === '<') return '&lt;';
        if (m === '>') return '&gt;';
        return m;
    });
}

// ==================== INITIALISATION ====================
document.addEventListener('DOMContentLoaded', async () => {
    // Charger les spécialités depuis l'API
    await fetchSpecialites();
    // Optionnel : charger enseignants et matières si besoin
    // await fetchEnseignants();
    // await fetchMatieres();

    // Charger les étudiants depuis localStorage
    loadStudents();
    renderStudents();

    // Attacher les événements
    document.getElementById('ajouterBtn').addEventListener('click', addStudent);
    document.getElementById('filtreClasse').addEventListener('change', renderStudents);
    document.getElementById('exportPdfBtn').addEventListener('click', exportToPDF);
});