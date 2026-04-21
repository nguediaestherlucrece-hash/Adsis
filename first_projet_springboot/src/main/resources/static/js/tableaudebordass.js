import { requireRole } from './auth.js';

// Guard : réservé aux assistants pédagogiques et surveillants
if (!requireRole(['ASSISTANT_PEDAGOGIQUE', 'SURVEILLANT'])) { throw new Error('Unauthorized'); }

// Afficher la date du jour dans l'en-tête
const dateElement = document.getElementById('currentDate');
if (dateElement) {
    const options = { weekday: 'long', year: 'numeric', month: 'long', day: 'numeric' };
    const today = new Date().toLocaleDateString('fr-FR', options);
    dateElement.textContent = today;
}

