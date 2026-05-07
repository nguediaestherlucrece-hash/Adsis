// static/js/etudiant-dashboard.js
const API_BASE = 'http://localhost:8081/api';
const token = localStorage.getItem('adsis_token');

// Chargement des prochains cours
async function loadProchainsCours() {
    try {
        const response = await fetch(`${API_BASE}/etudiant/cours/a-venir`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur chargement cours');
        const cours = await response.json();
        const container = document.getElementById('cours-list');
        if (cours.length === 0) {
            container.innerHTML = '<li class="list-group-item">Aucun cours à venir</li>';
            return;
        }
        container.innerHTML = cours.map(c => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                    <strong>${c.matiere}</strong><br>
                    <small>${new Date(c.date).toLocaleDateString('fr-FR')} - ${c.heureDebut} à ${c.heureFin}</small>
                </div>
                <span class="badge bg-primary rounded-pill">${c.salle}</span>
            </li>
        `).join('');
    } catch (error) {
        console.error(error);
        document.getElementById('cours-list').innerHTML = '<li class="list-group-item text-danger">Erreur de chargement</li>';
    }
}

// Chargement des dernières absences
async function loadAbsencesRecentes() {
    try {
        const response = await fetch(`${API_BASE}/etudiant/absences?limite=5`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur chargement absences');
        const absences = await response.json();
        const container = document.getElementById('absences-list');
        if (absences.length === 0) {
            container.innerHTML = '<li class="list-group-item">Aucune absence enregistrée</li>';
            return;
        }
        container.innerHTML = absences.map(a => `
            <li class="list-group-item d-flex justify-content-between align-items-center">
                <div>
                    <strong>${a.matiere}</strong><br>
                    <small>${new Date(a.date).toLocaleDateString('fr-FR')}</small>
                </div>
                <span class="badge ${a.justifiee ? 'bg-success' : 'bg-danger'} rounded-pill">
                    ${a.justifiee ? 'Justifiée' : 'Non justifiée'}
                </span>
            </li>
        `).join('');
    } catch (error) {
        console.error(error);
        document.getElementById('absences-list').innerHTML = '<li class="list-group-item text-danger">Erreur de chargement</li>';
    }
}

loadProchainsCours();
loadAbsencesRecentes();