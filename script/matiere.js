// script/matiere.js

let matieres = [];
let specialites = [];
let enseignants = [];
let seances = [];
let isEditing = false;
let currentMatiereId = null;

// Configuration API
const API_BASE_URL = 'http://localhost:8081/api';

// Références DOM
const matieresContainer = document.getElementById('matieresContainer');
const specialiteSelect = document.getElementById('specialiteSelect');
const enseignantSelect = document.getElementById('enseignantSelect');
const ajouterBtn = document.getElementById('ajouterBtn');
const codeInput = document.getElementById('codeInput');
const nomInput = document.getElementById('nomInput');


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


// Fonctions API
async function fetchSpecialites() {
    try {
        const response = await fetch(`${API_BASE_URL}/specialites`);
        if (response.ok) {
            specialites = await response.json();
            updateSpecialiteSelect();
        }
    } catch (error) { console.error("Erreur réseau (spécialités):", error); }
}

async function fetchEnseignants() {
    try {
        const response = await fetch(`${API_BASE_URL}/enseignants`);
        if (response.ok) {
            enseignants = await response.json();
            updateEnseignantSelect();
        }
    } catch (error) { console.error("Erreur réseau (enseignants):", error); }
}

async function fetchSeances() {
    try {
        const response = await fetch(`${API_BASE_URL}/seances`);
        if (response.ok) {
            seances = await response.json();
        }
    } catch (error) { console.error("Erreur réseau (séances):", error); }
}

async function fetchMatieres() {
    try {
        const response = await fetch(`${API_BASE_URL}/cours-ue`);
        if (response.ok) {
            matieres = await response.json();
            renderMatieres();
        }
    } catch (error) { console.error("Erreur réseau (matières):", error); }
}

async function createMatiere(dto) {
    try {
        const response = await fetch(`${API_BASE_URL}/cours-ue`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        
        if (response.ok) {
            await fetchMatieres();
            resetForm();
            showToast("Matière ajoutée avec succès !", "success");
        } else {
            const errText = await response.text();
            showToast("Erreur lors de l'ajout : " + errText, "danger");
        }
    } catch (error) { showToast("Erreur réseau.", "danger"); }
}

async function updateMatiereApi(id, dto) {
    try {
        const response = await fetch(`${API_BASE_URL}/cours-ue/${id}`, {
            method: 'PUT',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(dto)
        });
        
        if (response.ok) {
            await fetchMatieres();
            resetForm();
            showToast("Matière modifiée avec succès !", "success");
        } else {
            showToast("Erreur lors de la modification.", "danger");
        }
    } catch (error) { showToast("Erreur réseau.", "danger"); }
}

async function deleteMatiereApi(id) {
    const confirmed = await showConfirm("Supprimer cette matière ?", "Cette action est irréversible.");
    if (!confirmed) return;
    
    try {
        const response = await fetch(`${API_BASE_URL}/cours-ue/${id}`, { method: 'DELETE' });
        
        if (response.ok || response.status === 204) {
            await fetchMatieres();
            showToast("Matière supprimée.", "success");
        } else {
            showToast("Erreur lors de la suppression.", "danger");
        }
    } catch (error) { showToast("Erreur réseau.", "danger"); }
}

// Mises à jour de l'UI
function updateSpecialiteSelect() {
    specialiteSelect.innerHTML = '<option value="">-- Spécialité --</option>';
    specialites.forEach(s => {
        const option = document.createElement('option');
        option.value = s.id;
        option.textContent = s.nom || s.nomSpecialite || s.idSpecialite;
        specialiteSelect.appendChild(option);
    });
}

function updateEnseignantSelect() {
    enseignantSelect.innerHTML = '';
    enseignants.forEach(e => {
        const option = document.createElement('option');
        option.value = e.id;
        option.textContent = `${e.nom} ${e.prenom}`;
        enseignantSelect.appendChild(option);
    });
}

function calculateDuration(heureDebut, heureFin) {
    if (!heureDebut || !heureFin) return 0;
    const [h1, m1] = heureDebut.split(':').map(Number);
    const [h2, m2] = heureFin.split(':').map(Number);
    return Math.max(0, (h2 * 60 + m2 - h1 * 60 - m1) / 60);
}

function renderMatieres() {
    const totalSpan = document.getElementById('totalMatieres');
    const totalFooter = document.getElementById('totalMatieresFooter');
    const totalHeuresSpan = document.getElementById('totalHeures');
    const moyenneSpan = document.getElementById('moyenneHeures');

    let totalHeures = 0;
    let htmlString = '';
    
    matieres.forEach(matiere => {
        // Calcul automatique du volume horaire basé sur les séances
        const seancesForMatiere = seances.filter(s => s.coursId === matiere.id);
        let matiereHeures = 0;
        seancesForMatiere.forEach(s => {
            matiereHeures += calculateDuration(s.heureDebut, s.heureFin);
        });
        totalHeures += matiereHeures;

        let enseignantsHtml = '';
        if (matiere.enseignants && matiere.enseignants.length > 0) {
            enseignantsHtml = matiere.enseignants.map(e => 
                `<span class="badge bg-info text-dark me-1"><i class="fas fa-chalkboard-teacher me-1"></i>${e.nom}</span>`
            ).join('');
        } else {
            enseignantsHtml = `<span class="badge bg-light text-muted">Aucun</span>`;
        }
        
        const specName = matiere.specialiteNom || 'Non définie';

        htmlString += `
            <div class="matiere-item d-flex align-items-center flex-wrap gap-2">
                <span class="matiere-code">${matiere.codeUE}</span>
                <span class="matiere-nom fw-bold">${matiere.intitule}</span>
                <span class="badge bg-secondary">${specName}</span>
                ${enseignantsHtml}
                <span class="matiere-heures ms-auto">${matiereHeures % 1 === 0 ? matiereHeures : matiereHeures.toFixed(1)}h</span>
                <button class="btn btn-sm btn-outline-primary ms-2" title="Modifier" onclick="editerMatiere(${matiere.id})"><i class="fas fa-edit"></i></button>
                <button class="btn btn-sm btn-outline-danger ms-1" title="Supprimer" onclick="supprimerMatiere(${matiere.id})"><i class="fas fa-trash"></i></button>
            </div>
        `;
    });
    
    if(matieres.length === 0) {
        htmlString = `<div class="p-3 text-center text-muted">Aucune matière trouvée.</div>`;
    }
    
    const moyenne = matieres.length > 0 ? (totalHeures / matieres.length).toFixed(1) : 0;

    totalSpan.textContent = matieres.length;
    totalFooter.textContent = matieres.length;
    totalHeuresSpan.textContent = (totalHeures % 1 === 0 ? totalHeures : totalHeures.toFixed(1)) + 'h';
    moyenneSpan.textContent = moyenne + 'h';
    
    matieresContainer.innerHTML = htmlString;
}

// Actions utilisateur
function submitForm() {
    const code = codeInput.value.trim();
    const nom = nomInput.value.trim();
    const specialiteId = specialiteSelect.value ? parseInt(specialiteSelect.value) : null;
    
    // Récupérer les IDs des enseignants sélectionnés
    const enseignantIds = Array.from(enseignantSelect.selectedOptions).map(opt => parseInt(opt.value));

    if (!code || !nom || !specialiteId) {
        showToast("Veuillez remplir au moins le code, l'intitulé et la spécialité.", "warning");
        return;
    }

    const dto = {
        idCours: "C-" + code + "-" + Date.now().toString().slice(-4),
        codeUE: code,
        intitule: nom,
        volumeHoraire: 0,
        specialiteId: specialiteId,
        enseignantIds: enseignantIds
    };

    if (isEditing && currentMatiereId) {
        updateMatiereApi(currentMatiereId, dto);
    } else {
        createMatiere(dto);
    }
}

function editerMatiere(id) {
    const matiere = matieres.find(m => m.id === id);
    if (!matiere) return;
    
    isEditing = true;
    currentMatiereId = id;
    
    codeInput.value = matiere.codeUE;
    nomInput.value = matiere.intitule;
    specialiteSelect.value = matiere.specialiteId || '';
    
    // Sélectionner les enseignants
    const teacherIds = matiere.enseignants ? matiere.enseignants.map(e => e.id.toString()) : [];
    Array.from(enseignantSelect.options).forEach(opt => {
        opt.selected = teacherIds.includes(opt.value);
    });
    
    ajouterBtn.textContent = 'Modifier';
    ajouterBtn.classList.remove('btn-primary');
    ajouterBtn.classList.add('btn-warning');
}

function resetForm() {
    isEditing = false;
    currentMatiereId = null;
    
    codeInput.value = '';
    nomInput.value = '';
    specialiteSelect.value = '';
    Array.from(enseignantSelect.options).forEach(opt => opt.selected = false);
    
    ajouterBtn.textContent = 'Ajouter';
    ajouterBtn.classList.remove('btn-warning');
    ajouterBtn.classList.add('btn-primary');
}

// Initialisation globale pour les onclick
window.supprimerMatiere = deleteMatiereApi;
window.editerMatiere = editerMatiere;

document.addEventListener('DOMContentLoaded', async () => {
    ajouterBtn.addEventListener('click', submitForm);
    
    // Charger les données initiales de façon séquentielle
    await fetchSpecialites();
    await fetchEnseignants();
    await fetchSeances();
    await fetchMatieres();
});