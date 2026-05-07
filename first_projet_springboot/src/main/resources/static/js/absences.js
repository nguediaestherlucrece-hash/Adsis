// static/js/etudiant-absences.js
const API_BASE = 'http://localhost:8081/api';
const token = localStorage.getItem('adsis_token');

async function loadAbsences() {
    try {
        const response = await fetch(`${API_BASE}/etudiant/absences/toutes`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur chargement absences');
        const absences = await response.json();
        const tbody = document.getElementById('absences-body');
        
        if (absences.length === 0) {
            tbody.innerHTML = '<tr><td colspan="4" class="text-center">Aucune absence enregistrée</td></tr>';
            return;
        }
        
        tbody.innerHTML = absences.map(a => `
            <tr>
                <td>${new Date(a.date).toLocaleDateString('fr-FR')}</td>
                <td>${a.matiere}</td>
                <td>
                    <span class="badge ${a.justifiee ? 'bg-success' : 'bg-warning text-dark'}">
                        ${a.justifiee ? 'Justifiée' : 'En attente / Non justifiée'}
                    </span>
                </td>
                <td>
                    ${a.justificatif ? 
                        `<a href="${API_BASE}/etudiant/justificatifs/${a.id}" target="_blank" class="btn btn-sm btn-outline-info">Voir le justificatif</a>` : 
                        `<button class="btn btn-sm btn-primary justify-btn" data-absence-id="${a.id}">Justifier</button>`
                    }
                </td>
            </tr>
        `).join('');
        
        // Attacher les événements aux boutons "Justifier"
        document.querySelectorAll('.justify-btn').forEach(btn => {
            btn.addEventListener('click', (e) => {
                const absenceId = btn.getAttribute('data-absence-id');
                window.location.href = `/etudiant/justifications?id=${absenceId}`;
            });
        });
    } catch (error) {
        console.error(error);
        document.getElementById('absences-body').innerHTML = '<tr><td colspan="4" class="text-danger text-center">Erreur de chargement</td></tr>';
    }
}

loadAbsences();