// static/js/etudiant-emploi.js
const API_BASE = 'http://localhost:8081/api';
const token = localStorage.getItem('adsis_token');

// Jours de la semaine (lundi à vendredi)
const jours = ['Lundi', 'Mardi', 'Mercredi', 'Jeudi', 'Vendredi'];
// Créneaux horaires (à adapter selon votre logique)
const creneaux = ['08:00-09:30', '09:45-11:15', '11:30-13:00', '14:00-15:30', '15:45-17:15'];

async function loadEmploiDuTemps() {
    try {
        const response = await fetch(`${API_BASE}/etudiant/emploi-du-temps/semaine`, {
            headers: { 'Authorization': `Bearer ${token}` }
        });
        if (!response.ok) throw new Error('Erreur chargement emploi du temps');
        const data = await response.json(); // Format attendu: [{ jour, heureDebut, heureFin, matiere, salle, enseignant }]
        
        // Construction d'une matrice jour x créneau
        const matrix = {};
        jours.forEach(jour => { matrix[jour] = {}; });
        data.forEach(cours => {
            const jour = cours.jour;
            const horaire = `${cours.heureDebut}-${cours.heureFin}`;
            if (!matrix[jour]) matrix[jour] = {};
            matrix[jour][horaire] = cours;
        });

        const tbody = document.getElementById('edt-body');
        tbody.innerHTML = '';
        
        creneaux.forEach(creneau => {
            const row = document.createElement('tr');
            // Première colonne : horaire
            const tdTime = document.createElement('td');
            tdTime.textContent = creneau;
            tdTime.classList.add('fw-bold', 'bg-light');
            row.appendChild(tdTime);
            
            // Colonnes pour chaque jour
            jours.forEach(jour => {
                const td = document.createElement('td');
                const cours = matrix[jour]?.[creneau];
                if (cours) {
                    td.innerHTML = `
                        <div class="cours-item">
                            <strong>${cours.matiere}</strong><br>
                            <small>${cours.salle} - ${cours.enseignant}</small>
                        </div>
                    `;
                    td.classList.add('bg-info', 'bg-opacity-10');
                } else {
                    td.innerHTML = '—';
                    td.classList.add('text-muted');
                }
                row.appendChild(td);
            });
            tbody.appendChild(row);
        });
    } catch (error) {
        console.error(error);
        document.getElementById('edt-body').innerHTML = '<tr><td colspan="6" class="text-center text-danger">Erreur de chargement</td></tr>';
    }
}

loadEmploiDuTemps();