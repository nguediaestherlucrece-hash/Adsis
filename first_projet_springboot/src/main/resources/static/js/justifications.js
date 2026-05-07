// static/js/etudiant-justifications.js
const API_BASE = 'http://localhost:8081/api';
const token = localStorage.getItem('adsis_token');

// Récupérer l'ID d'absence passé en paramètre (optionnel)
const urlParams = new URLSearchParams(window.location.search);
const preselectedAbsenceId = urlParams.get('id');

async function loadAbsencesNonJustifiees() {
    try {
        const response = await fetch(`${API_BASE}/etudiant/absences/non-justifiees`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur chargement absences');
        const absences = await response.json();
        const select = document.getElementById('absence-select');
        
        if (absences.length === 0) {
            select.innerHTML = '<option value="">Aucune absence à justifier</option>';
            select.disabled = true;
            document.getElementById('justification-form').querySelector('button[type="submit"]').disabled = true;
            return;
        }
        
        select.innerHTML = '<option value="">Sélectionnez une absence</option>' +
            absences.map(a => `<option value="${a.id}" ${preselectedAbsenceId == a.id ? 'selected' : ''}>${a.matiere} - ${new Date(a.date).toLocaleDateString('fr-FR')}</option>`).join('');
        
        if (preselectedAbsenceId && absences.find(a => a.id == preselectedAbsenceId)) {
            // On a déjà présélectionné
        }
    } catch (error) {
        console.error(error);
        document.getElementById('absence-select').innerHTML = '<option>Erreur de chargement</option>';
    }
}

// Envoi du formulaire
document.getElementById('justification-form').addEventListener('submit', async (e) => {
    e.preventDefault();
    const absenceId = document.getElementById('absence-select').value;
    const motif = document.getElementById('motif').value;
    const messageDiv = document.getElementById('message');
    
    if (!absenceId) {
        messageDiv.innerHTML = '<div class="alert alert-warning">Veuillez sélectionner une absence.</div>';
        return;
    }
    
    try {
        const response = await fetch(`${API_BASE}/etudiant/justifications`, {
            method: 'POST',
            headers: {
                'Authorization': `Bearer ${token}`,
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ absenceId, motif })
        });
        
        if (response.ok) {
            messageDiv.innerHTML = '<div class="alert alert-success">Justification envoyée avec succès. Un responsable la validera.</div>';
            document.getElementById('justification-form').reset();
            loadAbsencesNonJustifiees(); // Recharge la liste
        } else {
            const error = await response.text();
            messageDiv.innerHTML = `<div class="alert alert-danger">Erreur : ${error}</div>`;
        }
    } catch (error) {
        console.error(error);
        messageDiv.innerHTML = '<div class="alert alert-danger">Erreur réseau. Veuillez réessayer.</div>';
    }
});

loadAbsencesNonJustifiees();